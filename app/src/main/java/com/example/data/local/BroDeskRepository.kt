package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppSettings
import com.example.data.model.BookmarkFolder
import com.example.data.model.BookmarkItem
import com.example.data.model.DownloadItem
import com.example.data.model.HistoryItem
import com.example.data.model.SeedData
import com.example.data.model.ShortcutItem
import com.example.data.model.TabItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class BroDeskRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("brodesk_prefs", Context.MODE_PRIVATE)

    private val _tabs = MutableStateFlow<List<TabItem>>(emptyList())
    val tabs: StateFlow<List<TabItem>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String?>(null)
    val activeTabId: StateFlow<String?> = _activeTabId.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<BookmarkItem>>(emptyList())
    val bookmarks: StateFlow<List<BookmarkItem>> = _bookmarks.asStateFlow()

    private val _folders = MutableStateFlow<List<BookmarkFolder>>(emptyList())
    val folders: StateFlow<List<BookmarkFolder>> = _folders.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private val _shortcuts = MutableStateFlow<List<ShortcutItem>>(emptyList())
    val shortcuts: StateFlow<List<ShortcutItem>> = _shortcuts.asStateFlow()

    private val _settings = MutableStateFlow<AppSettings>(SeedData.defaultSettings)
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    init {
        loadSettings()
        loadFolders()
        loadBookmarks()
        loadShortcuts()
        loadHistory()
        loadDownloads()
        loadTabs()
    }

    // --- SETTINGS ---
    private fun loadSettings() {
        val json = prefs.getString("settings", null)
        if (json != null) {
            try {
                val obj = JSONObject(json)
                _settings.value = AppSettings(
                    defaultDesktopMode = obj.optBoolean("defaultDesktopMode", true),
                    browserIdentity = obj.optString("browserIdentity", "Chrome Windows"),
                    desktopWidth = obj.optInt("desktopWidth", 1280),
                    theme = obj.optString("theme", "system"),
                    cursorSensitivity = obj.optDouble("cursorSensitivity", 1.0).toFloat(),
                    scrollSpeed = obj.optDouble("scrollSpeed", 1.0).toFloat(),
                    cursorSize = obj.optString("cursorSize", "Sedang"),
                    longPressDurationMs = obj.optLong("longPressDurationMs", 500L),
                    vibrationEnabled = obj.optBoolean("vibrationEnabled", true),
                    defaultLongPressMode = obj.optString("defaultLongPressMode", "Klik kanan"),
                    searchEngine = obj.optString("searchEngine", "Google"),
                    blockPopups = obj.optBoolean("blockPopups", true)
                )
            } catch (e: Exception) {
                _settings.value = SeedData.defaultSettings
            }
        } else {
            _settings.value = SeedData.defaultSettings
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        try {
            val obj = JSONObject().apply {
                put("defaultDesktopMode", newSettings.defaultDesktopMode)
                put("browserIdentity", newSettings.browserIdentity)
                put("desktopWidth", newSettings.desktopWidth)
                put("theme", newSettings.theme)
                put("cursorSensitivity", newSettings.cursorSensitivity.toDouble())
                put("scrollSpeed", newSettings.scrollSpeed.toDouble())
                put("cursorSize", newSettings.cursorSize)
                put("longPressDurationMs", newSettings.longPressDurationMs)
                put("vibrationEnabled", newSettings.vibrationEnabled)
                put("defaultLongPressMode", newSettings.defaultLongPressMode)
                put("searchEngine", newSettings.searchEngine)
                put("blockPopups", newSettings.blockPopups)
            }
            prefs.edit().putString("settings", obj.toString()).apply()
        } catch (_: Exception) {}
    }

    // --- TABS ---
    private fun loadTabs() {
        val json = prefs.getString("tabs", null)
        val activeId = prefs.getString("activeTabId", null)
        val loadedList = mutableListOf<TabItem>()

        if (json != null) {
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    loadedList.add(
                        TabItem(
                            id = obj.getString("id"),
                            title = obj.optString("title", "Tab Baru"),
                            url = obj.optString("url", ""),
                            iconUrl = if (obj.has("iconUrl")) obj.getString("iconUrl") else null,
                            isDesktopMode = obj.optBoolean("isDesktopMode", _settings.value.defaultDesktopMode),
                            order = obj.optInt("order", i),
                            isActive = obj.optBoolean("isActive", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        if (loadedList.isEmpty()) {
            val initialTab = TabItem(
                id = UUID.randomUUID().toString(),
                title = "Tab Baru",
                url = "",
                isDesktopMode = _settings.value.defaultDesktopMode,
                isActive = true
            )
            loadedList.add(initialTab)
            _tabs.value = loadedList
            _activeTabId.value = initialTab.id
            saveTabs()
        } else {
            _tabs.value = loadedList
            _activeTabId.value = activeId ?: loadedList.firstOrNull { it.isActive }?.id ?: loadedList.first().id
        }
    }

    private fun saveTabs() {
        try {
            val arr = JSONArray()
            _tabs.value.forEach { tab ->
                val obj = JSONObject().apply {
                    put("id", tab.id)
                    put("title", tab.title)
                    put("url", tab.url)
                    tab.iconUrl?.let { put("iconUrl", it) }
                    put("isDesktopMode", tab.isDesktopMode)
                    put("order", tab.order)
                    put("isActive", tab.id == _activeTabId.value)
                    put("createdAt", tab.createdAt)
                }
                arr.put(obj)
            }
            prefs.edit()
                .putString("tabs", arr.toString())
                .putString("activeTabId", _activeTabId.value)
                .apply()
        } catch (_: Exception) {}
    }

    fun createNewTab(url: String = "", title: String = "Tab Baru"): String? {
        if (_tabs.value.size >= 20) {
            return null // Max 20 limit
        }
        val newTab = TabItem(
            id = UUID.randomUUID().toString(),
            title = if (url.isNotEmpty()) title else "Tab Baru",
            url = url,
            isDesktopMode = _settings.value.defaultDesktopMode,
            order = _tabs.value.size,
            isActive = true
        )
        val updatedList = _tabs.value.map { it.copy(isActive = false) } + newTab
        _tabs.value = updatedList
        _activeTabId.value = newTab.id
        saveTabs()
        return newTab.id
    }

    fun updateTab(tabId: String, url: String, title: String, iconUrl: String? = null, isDesktopMode: Boolean? = null) {
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == tabId) {
                tab.copy(
                    url = url,
                    title = if (title.isNotBlank()) title else if (url.isNotBlank()) url else "Tab Baru",
                    iconUrl = iconUrl ?: tab.iconUrl,
                    isDesktopMode = isDesktopMode ?: tab.isDesktopMode
                )
            } else tab
        }
        saveTabs()
    }

    fun switchTab(tabId: String) {
        if (_tabs.value.any { it.id == tabId }) {
            _activeTabId.value = tabId
            _tabs.value = _tabs.value.map { it.copy(isActive = it.id == tabId) }
            saveTabs()
        }
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == tabId }
        if (index != -1) {
            currentList.removeAt(index)
            if (currentList.isEmpty()) {
                val newTab = TabItem(
                    id = UUID.randomUUID().toString(),
                    title = "Tab Baru",
                    url = "",
                    isDesktopMode = _settings.value.defaultDesktopMode,
                    isActive = true
                )
                currentList.add(newTab)
                _activeTabId.value = newTab.id
            } else if (_activeTabId.value == tabId) {
                val nextTab = if (index < currentList.size) currentList[index] else currentList.last()
                _activeTabId.value = nextTab.id
            }
            _tabs.value = currentList.map { it.copy(isActive = it.id == _activeTabId.value) }
            saveTabs()
        }
    }

    fun closeAllTabs() {
        val newTab = TabItem(
            id = UUID.randomUUID().toString(),
            title = "Tab Baru",
            url = "",
            isDesktopMode = _settings.value.defaultDesktopMode,
            isActive = true
        )
        _tabs.value = listOf(newTab)
        _activeTabId.value = newTab.id
        saveTabs()
    }

    // --- BOOKMARKS & FOLDERS ---
    private fun loadFolders() {
        val json = prefs.getString("folders", null)
        val list = mutableListOf<BookmarkFolder>()
        if (json != null) {
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(BookmarkFolder(id = obj.getString("id"), name = obj.getString("name"), order = obj.optInt("order", i)))
                }
            } catch (_: Exception) {}
        }
        _folders.value = list
    }

    private fun saveFolders() {
        try {
            val arr = JSONArray()
            _folders.value.forEach { folder ->
                val obj = JSONObject().apply {
                    put("id", folder.id)
                    put("name", folder.name)
                    put("order", folder.order)
                }
                arr.put(obj)
            }
            prefs.edit().putString("folders", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun addFolder(name: String): BookmarkFolder {
        val newFolder = BookmarkFolder(name = name, order = _folders.value.size)
        _folders.value = _folders.value + newFolder
        saveFolders()
        return newFolder
    }

    fun deleteFolder(folderId: String) {
        _folders.value = _folders.value.filter { it.id != folderId }
        saveFolders()
        _bookmarks.value = _bookmarks.value.map { if (it.folderId == folderId) it.copy(folderId = null) else it }
        saveBookmarks()
    }

    private fun loadBookmarks() {
        val json = prefs.getString("bookmarks", null)
        val list = mutableListOf<BookmarkItem>()
        if (json != null) {
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        BookmarkItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            url = obj.getString("url"),
                            folderId = if (obj.has("folderId")) obj.getString("folderId") else null,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (_: Exception) {}
        } else {
            list.addAll(SeedData.defaultBookmarks)
        }
        _bookmarks.value = list
    }

    private fun saveBookmarks() {
        try {
            val arr = JSONArray()
            _bookmarks.value.forEach { bm ->
                val obj = JSONObject().apply {
                    put("id", bm.id)
                    put("title", bm.title)
                    put("url", bm.url)
                    bm.folderId?.let { put("folderId", it) }
                    put("createdAt", bm.createdAt)
                }
                arr.put(obj)
            }
            prefs.edit().putString("bookmarks", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun addBookmark(url: String, title: String, folderId: String? = null): Boolean {
        if (_bookmarks.value.any { it.url.equals(url, ignoreCase = true) }) {
            return false // Duplicate
        }
        val actualTitle = if (title.isBlank()) url else title
        val item = BookmarkItem(title = actualTitle, url = url, folderId = folderId)
        _bookmarks.value = listOf(item) + _bookmarks.value
        saveBookmarks()
        return true
    }

    fun updateBookmark(id: String, title: String, url: String, folderId: String?) {
        _bookmarks.value = _bookmarks.value.map { bm ->
            if (bm.id == id) bm.copy(title = title, url = url, folderId = folderId) else bm
        }
        saveBookmarks()
    }

    fun deleteBookmark(id: String) {
        _bookmarks.value = _bookmarks.value.filter { it.id != id }
        saveBookmarks()
    }

    // --- SHORTCUTS ---
    private fun loadShortcuts() {
        val json = prefs.getString("shortcuts", null)
        val list = mutableListOf<ShortcutItem>()
        if (json != null) {
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        ShortcutItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            url = obj.getString("url"),
                            order = obj.optInt("order", i)
                        )
                    )
                }
            } catch (_: Exception) {}
        } else {
            list.addAll(SeedData.defaultShortcuts)
        }
        _shortcuts.value = list
    }

    private fun saveShortcuts() {
        try {
            val arr = JSONArray()
            _shortcuts.value.forEach { sc ->
                val obj = JSONObject().apply {
                    put("id", sc.id)
                    put("title", sc.title)
                    put("url", sc.url)
                    put("order", sc.order)
                }
                arr.put(obj)
            }
            prefs.edit().putString("shortcuts", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun addShortcut(title: String, url: String) {
        val item = ShortcutItem(title = title, url = url, order = _shortcuts.value.size)
        _shortcuts.value = _shortcuts.value + item
        saveShortcuts()
    }

    fun deleteShortcut(id: String) {
        _shortcuts.value = _shortcuts.value.filter { it.id != id }
        saveShortcuts()
    }

    // --- HISTORY ---
    private fun loadHistory() {
        val json = prefs.getString("history", null)
        val list = mutableListOf<HistoryItem>()
        if (json != null) {
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        HistoryItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            url = obj.getString("url"),
                            visitedAt = obj.optLong("visitedAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        _history.value = list
    }

    private fun saveHistory() {
        try {
            val arr = JSONArray()
            _history.value.take(2000).forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("url", item.url)
                    put("visitedAt", item.visitedAt)
                }
                arr.put(obj)
            }
            prefs.edit().putString("history", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun addHistory(url: String, title: String) {
        if (url.isBlank() || url.startsWith("about:") || url.startsWith("chrome:")) return

        val now = System.currentTimeMillis()
        val latest = _history.value.firstOrNull()
        if (latest != null && latest.url == url && (now - latest.visitedAt) < 60_000L) {
            return // Skip deduplicated within 1 min
        }

        val actualTitle = if (title.isNotBlank()) title else url
        val item = HistoryItem(title = actualTitle, url = url, visitedAt = now)
        val updated = (listOf(item) + _history.value).take(2000)
        _history.value = updated
        saveHistory()
    }

    fun deleteHistoryItem(id: String) {
        _history.value = _history.value.filter { it.id != id }
        saveHistory()
    }

    fun clearHistory(range: String) { // "1hour", "today", "all"
        val now = System.currentTimeMillis()
        when (range) {
            "1hour" -> {
                val cutoff = now - 3600_000L
                _history.value = _history.value.filter { it.visitedAt < cutoff }
            }
            "today" -> {
                val startOfToday = now - (now % 86400_000L)
                _history.value = _history.value.filter { it.visitedAt < startOfToday }
            }
            "all" -> {
                _history.value = emptyList()
            }
        }
        saveHistory()
    }

    // --- DOWNLOADS ---
    private fun loadDownloads() {
        val json = prefs.getString("downloads", null)
        val list = mutableListOf<DownloadItem>()
        if (json != null) {
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        DownloadItem(
                            id = obj.getString("id"),
                            filename = obj.getString("filename"),
                            url = obj.getString("url"),
                            mimeType = obj.optString("mimeType", ""),
                            contentLength = obj.optLong("contentLength", 0L),
                            downloadedAt = obj.optLong("downloadedAt", System.currentTimeMillis()),
                            status = obj.optString("status", "SELESAI"),
                            localPath = obj.optString("localPath", "")
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        _downloads.value = list
    }

    private fun saveDownloads() {
        try {
            val arr = JSONArray()
            _downloads.value.forEach { dl ->
                val obj = JSONObject().apply {
                    put("id", dl.id)
                    put("filename", dl.filename)
                    put("url", dl.url)
                    put("mimeType", dl.mimeType)
                    put("contentLength", dl.contentLength)
                    put("downloadedAt", dl.downloadedAt)
                    put("status", dl.status)
                    put("localPath", dl.localPath)
                }
                arr.put(obj)
            }
            prefs.edit().putString("downloads", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun addDownload(filename: String, url: String, mimeType: String, contentLength: Long, localPath: String): DownloadItem {
        val item = DownloadItem(
            filename = filename,
            url = url,
            mimeType = mimeType,
            contentLength = contentLength,
            localPath = localPath
        )
        _downloads.value = listOf(item) + _downloads.value
        saveDownloads()
        return item
    }

    fun deleteDownload(id: String) {
        _downloads.value = _downloads.value.filter { it.id != id }
        saveDownloads()
    }

    // --- BACKUP & RESTORE ---
    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)

        val bmArray = JSONArray()
        _bookmarks.value.forEach { bm ->
            bmArray.put(JSONObject().apply {
                put("id", bm.id)
                put("title", bm.title)
                put("url", bm.url)
                bm.folderId?.let { put("folderId", it) }
                put("createdAt", bm.createdAt)
            })
        }
        root.put("bookmarks", bmArray)

        val folderArray = JSONArray()
        _folders.value.forEach { folder ->
            folderArray.put(JSONObject().apply {
                put("id", folder.id)
                put("name", folder.name)
                put("order", folder.order)
            })
        }
        root.put("folders", folderArray)

        val scArray = JSONArray()
        _shortcuts.value.forEach { sc ->
            scArray.put(JSONObject().apply {
                put("id", sc.id)
                put("title", sc.title)
                put("url", sc.url)
                put("order", sc.order)
            })
        }
        root.put("shortcuts", scArray)

        val st = _settings.value
        val stObj = JSONObject().apply {
            put("defaultDesktopMode", st.defaultDesktopMode)
            put("browserIdentity", st.browserIdentity)
            put("desktopWidth", st.desktopWidth)
            put("theme", st.theme)
            put("cursorSensitivity", st.cursorSensitivity.toDouble())
            put("scrollSpeed", st.scrollSpeed.toDouble())
            put("cursorSize", st.cursorSize)
            put("longPressDurationMs", st.longPressDurationMs)
            put("vibrationEnabled", st.vibrationEnabled)
            put("defaultLongPressMode", st.defaultLongPressMode)
            put("searchEngine", st.searchEngine)
            put("blockPopups", st.blockPopups)
        }
        root.put("settings", stObj)

        return root.toString(2)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            if (root.has("bookmarks")) {
                val bmArr = root.getJSONArray("bookmarks")
                val bms = mutableListOf<BookmarkItem>()
                for (i in 0 until bmArr.length()) {
                    val obj = bmArr.getJSONObject(i)
                    bms.add(BookmarkItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        url = obj.getString("url"),
                        folderId = if (obj.has("folderId")) obj.getString("folderId") else null,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    ))
                }
                _bookmarks.value = bms
                saveBookmarks()
            }

            if (root.has("folders")) {
                val fArr = root.getJSONArray("folders")
                val fs = mutableListOf<BookmarkFolder>()
                for (i in 0 until fArr.length()) {
                    val obj = fArr.getJSONObject(i)
                    fs.add(BookmarkFolder(id = obj.getString("id"), name = obj.getString("name"), order = obj.optInt("order", i)))
                }
                _folders.value = fs
                saveFolders()
            }

            if (root.has("shortcuts")) {
                val scArr = root.getJSONArray("shortcuts")
                val scs = mutableListOf<ShortcutItem>()
                for (i in 0 until scArr.length()) {
                    val obj = scArr.getJSONObject(i)
                    scs.add(ShortcutItem(id = obj.getString("id"), title = obj.getString("title"), url = obj.getString("url"), order = obj.optInt("order", i)))
                }
                _shortcuts.value = scs
                saveShortcuts()
            }

            if (root.has("settings")) {
                val obj = root.getJSONObject("settings")
                val newSt = AppSettings(
                    defaultDesktopMode = obj.optBoolean("defaultDesktopMode", true),
                    browserIdentity = obj.optString("browserIdentity", "Chrome Windows"),
                    desktopWidth = obj.optInt("desktopWidth", 1280),
                    theme = obj.optString("theme", "system"),
                    cursorSensitivity = obj.optDouble("cursorSensitivity", 1.0).toFloat(),
                    scrollSpeed = obj.optDouble("scrollSpeed", 1.0).toFloat(),
                    cursorSize = obj.optString("cursorSize", "Sedang"),
                    longPressDurationMs = obj.optLong("longPressDurationMs", 500L),
                    vibrationEnabled = obj.optBoolean("vibrationEnabled", true),
                    defaultLongPressMode = obj.optString("defaultLongPressMode", "Klik kanan"),
                    searchEngine = obj.optString("searchEngine", "Google"),
                    blockPopups = obj.optBoolean("blockPopups", true)
                )
                updateSettings(newSt)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
