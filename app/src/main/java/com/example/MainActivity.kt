package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.local.BroDeskRepository
import com.example.services.VibrationService
import com.example.ui.screens.BookmarkScreen
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TabScreen
import com.example.ui.components.BroDeskBottomBar
import com.example.ui.theme.BroDeskTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: BroDeskRepository
    private lateinit var vibrationService: VibrationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = BroDeskRepository(applicationContext)
        vibrationService = VibrationService(applicationContext)

        setContent {
            val settings by repository.settings.collectAsState()

            BroDeskTheme(themeSetting = settings.theme) {
                BroDeskApp(
                    repository = repository,
                    vibrationService = vibrationService,
                    onExitApp = { finish() }
                )
            }
        }
    }
}

@Composable
fun BroDeskApp(
    repository: BroDeskRepository,
    vibrationService: VibrationService,
    onExitApp: () -> Unit
) {
    val context = LocalContext.current
    val tabs by repository.tabs.collectAsState()
    val activeTabId by repository.activeTabId.collectAsState()
    val bookmarks by repository.bookmarks.collectAsState()
    val folders by repository.folders.collectAsState()
    val history by repository.history.collectAsState()
    val shortcuts by repository.shortcuts.collectAsState()
    val settings by repository.settings.collectAsState()
    val downloads by repository.downloads.collectAsState()

    var currentRoute by remember { mutableStateOf("browser") }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    val activeTab = tabs.find { it.id == activeTabId } ?: tabs.firstOrNull() ?: repository.createNewTab()?.let { id ->
        tabs.find { it.id == id }
    } ?: return

    // Handle Back Press cleanly
    BackHandler {
        if (currentRoute != "browser") {
            currentRoute = "browser"
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2000L) {
                onExitApp()
            } else {
                lastBackPressTime = now
                Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            bottomBar = {
                if (currentRoute != "downloads") {
                    BroDeskBottomBar(
                        currentRoute = currentRoute,
                        tabCount = tabs.size,
                        onNavigate = { route -> currentRoute = route }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 430.dp) // Centered 430px max container for mobile preview
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentRoute) {
                    "browser" -> {
                        BrowserScreen(
                            activeTab = activeTab,
                            settings = settings,
                            shortcuts = shortcuts,
                            bookmarks = bookmarks,
                            onUpdateTab = { url, title, iconUrl, isDesktopMode ->
                                repository.updateTab(activeTab.id, url, title, iconUrl, isDesktopMode)
                            },
                            onAddHistory = { url, title ->
                                repository.addHistory(url, title)
                            },
                            onAddBookmark = { url, title ->
                                repository.addBookmark(url, title)
                            },
                            onNewTab = { url ->
                                val newId = repository.createNewTab(url)
                                if (newId == null) {
                                    Toast.makeText(context, "Batas 20 tab tercapai", Toast.LENGTH_SHORT).show()
                                } else {
                                    currentRoute = "browser"
                                }
                            },
                            onOpenDownloads = { currentRoute = "downloads" },
                            onAddShortcut = { title, url ->
                                repository.addShortcut(title, url)
                            },
                            onDeleteShortcut = { id ->
                                repository.deleteShortcut(id)
                            },
                            onAddDownload = { filename, url, mimeType, size, path ->
                                repository.addDownload(filename, url, mimeType, size, path)
                            },
                            onVibrateShort = { vibrationService.vibrateShort(settings.vibrationEnabled) },
                            onVibrateMedium = { vibrationService.vibrateMedium(settings.vibrationEnabled) }
                        )
                    }

                    "tab" -> {
                        TabScreen(
                            tabs = tabs,
                            activeTabId = activeTabId,
                            onSwitchTab = { id ->
                                repository.switchTab(id)
                                currentRoute = "browser"
                            },
                            onCloseTab = { id ->
                                repository.closeTab(id)
                            },
                            onCloseAllTabs = {
                                repository.closeAllTabs()
                            },
                            onNewTab = {
                                val newId = repository.createNewTab()
                                if (newId == null) {
                                    Toast.makeText(context, "Batas 20 tab tercapai", Toast.LENGTH_SHORT).show()
                                } else {
                                    currentRoute = "browser"
                                }
                            }
                        )
                    }

                    "bookmark" -> {
                        BookmarkScreen(
                            bookmarks = bookmarks,
                            folders = folders,
                            onOpenUrl = { url ->
                                repository.updateTab(activeTab.id, url, url, null, null)
                                currentRoute = "browser"
                            },
                            onOpenInNewTab = { url ->
                                val newId = repository.createNewTab(url)
                                if (newId == null) {
                                    Toast.makeText(context, "Batas 20 tab tercapai", Toast.LENGTH_SHORT).show()
                                } else {
                                    currentRoute = "browser"
                                }
                            },
                            onAddBookmark = { url, title, folderId ->
                                repository.addBookmark(url, title, folderId)
                            },
                            onUpdateBookmark = { id, title, url, folderId ->
                                repository.updateBookmark(id, title, url, folderId)
                            },
                            onDeleteBookmark = { id ->
                                repository.deleteBookmark(id)
                            },
                            onAddFolder = { name ->
                                repository.addFolder(name)
                            },
                            onDeleteFolder = { id ->
                                repository.deleteFolder(id)
                            }
                        )
                    }

                    "history" -> {
                        HistoryScreen(
                            history = history,
                            onOpenUrl = { url ->
                                repository.updateTab(activeTab.id, url, url, null, null)
                                currentRoute = "browser"
                            },
                            onDeleteHistoryItem = { id ->
                                repository.deleteHistoryItem(id)
                            },
                            onClearHistory = { range ->
                                repository.clearHistory(range)
                            }
                        )
                    }

                    "settings" -> {
                        SettingsScreen(
                            settings = settings,
                            onUpdateSettings = { newSettings ->
                                repository.updateSettings(newSettings)
                            },
                            onClearHistoryAll = {
                                repository.clearHistory("all")
                            },
                            onExportBackup = {
                                repository.exportBackupJson()
                            },
                            onImportBackup = { json ->
                                repository.importBackupJson(json)
                            }
                        )
                    }

                    "downloads" -> {
                        DownloadsScreen(
                            downloads = downloads,
                            onClose = { currentRoute = "browser" },
                            onDeleteDownload = { id ->
                                repository.deleteDownload(id)
                            }
                        )
                    }
                }
            }
        }
    }
}
