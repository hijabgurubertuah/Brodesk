package com.example.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.AppSettings
import com.example.data.model.BookmarkItem
import com.example.data.model.ShortcutItem
import com.example.data.model.TabItem
import com.example.services.JavaScriptEngine
import com.example.services.UrlUtils
import com.example.ui.components.BroDeskTopBar
import com.example.ui.components.ContextMenuDialog
import com.example.ui.components.ContextMenuTarget
import com.example.ui.components.DesktopKeyboardBar
import com.example.ui.components.FindInPageBar
import com.example.ui.components.FloatingTouchButton
import com.example.ui.components.ShortcutEditDialog
import com.example.ui.components.TouchpadOverlay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrowserScreen(
    activeTab: TabItem,
    settings: AppSettings,
    shortcuts: List<ShortcutItem>,
    bookmarks: List<BookmarkItem>,
    onWebNavStateChanged: (canGoBack: Boolean, canGoForward: Boolean, webView: WebView?) -> Unit = { _, _, _ -> },
    onUpdateTab: (url: String, title: String, iconUrl: String?, isDesktopMode: Boolean?) -> Unit,
    onAddHistory: (url: String, title: String) -> Unit,
    onAddBookmark: (url: String, title: String) -> Unit,
    onNewTab: (url: String) -> Unit,
    onOpenDownloads: () -> Unit,
    onAddShortcut: (title: String, url: String) -> Unit,
    onDeleteShortcut: (id: String) -> Unit,
    onAddDownload: (filename: String, url: String, mimeType: String, size: Long, path: String) -> Unit,
    onVibrateShort: () -> Unit,
    onVibrateMedium: () -> Unit
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentUrl by remember(activeTab.id) { mutableStateOf(activeTab.url) }
    var currentTitle by remember(activeTab.id) { mutableStateOf(activeTab.title) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isErrorState by remember { mutableStateOf(false) }

    // Mode state
    var touchMode by remember { mutableStateOf(settings.defaultLongPressMode) }
    var isFullScreen by remember { mutableStateOf(false) }
    var showKeyboardBar by remember { mutableStateOf(false) }
    var showFindInPage by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var findMatchIndex by remember { mutableIntStateOf(0) }
    var findMatchCount by remember { mutableIntStateOf(0) }

    // Context Menu state
    var contextMenuTarget by remember { mutableStateOf<ContextMenuTarget?>(null) }
    var showShortcutDialog by remember { mutableStateOf(false) }

    // File chooser launcher
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data?.data
        if (data != null) {
            filePathCallback?.onReceiveValue(arrayOf(data))
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    val isCurrentBookmarked = bookmarks.any { it.url.equals(currentUrl, ignoreCase = true) }

    fun navigateTo(rawInput: String) {
        val targetUrl = UrlUtils.parseInputToUrl(rawInput, settings.searchEngine)
        if (targetUrl.isNotEmpty()) {
            isErrorState = false
            currentUrl = targetUrl
            onUpdateTab(targetUrl, targetUrl, null, null)
            webViewRef?.loadUrl(targetUrl)
        }
    }

    fun shareUrl(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan tautan"))
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("BroDesk Tautan", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Teks disalin ke papan klip", Toast.LENGTH_SHORT).show()
    }

    fun downloadFile(url: String, mimeType: String = "", filename: String = "") {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val actualFilename = if (filename.isNotBlank()) filename else URLUtil.guessFileName(url, null, mimeType)
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(actualFilename)
                setDescription("Mengunduh lewat BroDesk")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, actualFilename)
                setMimeType(mimeType)
            }
            downloadManager.enqueue(request)
            onAddDownload(actualFilename, url, mimeType, 0L, Environment.getExternalStorageDirectory().path + "/Download/" + actualFilename)
            Toast.makeText(context, "Unduhan dimulai: $actualFilename", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memulai unduhan", Toast.LENGTH_SHORT).show()
        }
    }

    // Update parent web nav state
    LaunchedEffect(canGoBack, canGoForward, webViewRef) {
        onWebNavStateChanged(canGoBack, canGoForward, webViewRef)
    }

    var lastLoadedTabId by remember { mutableStateOf(activeTab.id) }
    LaunchedEffect(activeTab.id) {
        if (activeTab.id != lastLoadedTabId) {
            lastLoadedTabId = activeTab.id
            currentUrl = activeTab.url
            currentTitle = activeTab.title
            if (activeTab.url.isNotBlank()) {
                webViewRef?.loadUrl(activeTab.url)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("browser_screen_root")
    ) {
        // Top Bar
        if (!isFullScreen) {
            BroDeskTopBar(
                currentUrl = currentUrl,
                isLoading = isLoading,
                progress = progress,
                isBookmarked = isCurrentBookmarked,
                isDesktopMode = activeTab.isDesktopMode,
                isFullScreen = isFullScreen,
                showKeyboardBar = showKeyboardBar,
                onNavigateUrl = { navigateTo(it) },
                onReload = { webViewRef?.reload() },
                onStop = { webViewRef?.stopLoading() },
                onToggleBookmark = {
                    if (currentUrl.isNotBlank()) {
                        if (isCurrentBookmarked) {
                            Toast.makeText(context, "Sudah ada di bookmark", Toast.LENGTH_SHORT).show()
                        } else {
                            onAddBookmark(currentUrl, currentTitle)
                            Toast.makeText(context, "Bookmark disimpan", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onNewTab = { onNewTab("") },
                onFindInPage = { showFindInPage = true },
                onToggleDesktopMode = {
                    val newMode = !activeTab.isDesktopMode
                    onUpdateTab(currentUrl, currentTitle, null, newMode)
                    Toast.makeText(context, if (newMode) "Mode desktop aktif" else "Mode HP aktif", Toast.LENGTH_SHORT).show()
                },
                onLoadMobileVersion = {
                    onUpdateTab(currentUrl, currentTitle, null, false)
                    Toast.makeText(context, "Memuat versi HP", Toast.LENGTH_SHORT).show()
                },
                onToggleFullScreen = { isFullScreen = !isFullScreen },
                onToggleKeyboardBar = { showKeyboardBar = !showKeyboardBar },
                onOpenDownloads = onOpenDownloads,
                onShareLink = { if (currentUrl.isNotEmpty()) shareUrl(currentUrl) },
                onCopyLink = { if (currentUrl.isNotEmpty()) copyToClipboard(currentUrl) }
            )

            // Find in page bar
            FindInPageBar(
                isVisible = showFindInPage,
                query = findQuery,
                onQueryChange = { q ->
                    findQuery = q
                    if (q.isNotEmpty()) {
                        webViewRef?.findAllAsync(q)
                    } else {
                        webViewRef?.clearMatches()
                    }
                },
                matchIndex = findMatchIndex,
                matchCount = findMatchCount,
                onNext = { webViewRef?.findNext(true) },
                onPrev = { webViewRef?.findNext(false) },
                onClose = {
                    showFindInPage = false
                    webViewRef?.clearMatches()
                }
            )
        }

        // Content Area (WebView or Start Screen)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (currentUrl.isBlank()) {
                // Home / Start Screen
                StartScreenContent(
                    shortcuts = shortcuts,
                    bookmarks = bookmarks,
                    searchEngine = settings.searchEngine,
                    onSearch = { navigateTo(it) },
                    onOpenUrl = { navigateTo(it) },
                    onAddShortcut = { showShortcutDialog = true },
                    onDeleteShortcut = onDeleteShortcut
                )
            } else if (isErrorState) {
                // Clean Error Screen
                CleanErrorScreen(
                    onRetry = {
                        isErrorState = false
                        webViewRef?.reload()
                    }
                )
            } else {
                // Native WebView
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            try {
                                setLayerType(android.view.View.LAYER_TYPE_NONE, null)
                            } catch (_: Exception) {}

                            val ws = this.settings
                            ws.javaScriptEnabled = true
                            ws.domStorageEnabled = true
                            ws.databaseEnabled = true
                            ws.useWideViewPort = true
                            ws.loadWithOverviewMode = true
                            ws.setSupportZoom(true)
                            ws.builtInZoomControls = true
                            ws.displayZoomControls = false
                            ws.allowFileAccess = true
                            ws.allowContentAccess = true
                            ws.mediaPlaybackRequiresUserGesture = false
                            ws.setSupportMultipleWindows(false)
                            ws.javaScriptCanOpenWindowsAutomatically = true
                            ws.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                            CookieManager.getInstance().setAcceptCookie(true)
                            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                            setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                                findMatchIndex = activeMatchOrdinal
                                findMatchCount = numberOfMatches
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val uri = request?.url ?: return false
                                    val scheme = uri.scheme?.lowercase() ?: ""
                                    if (scheme == "http" || scheme == "https") {
                                        return false
                                    }
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        view?.context?.startActivity(intent)
                                    } catch (_: Exception) {}
                                    return true
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    url?.let { currentUrl = it }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    url?.let {
                                        currentUrl = it
                                        val title = view?.title ?: it
                                        currentTitle = title
                                        onUpdateTab(it, title, null, null)
                                        onAddHistory(it, title)
                                    }
                                    canGoBack = view?.canGoBack() ?: false
                                    canGoForward = view?.canGoForward() ?: false

                                    if (activeTab.isDesktopMode) {
                                        JavaScriptEngine.injectDesktopViewport(this@apply, settings.desktopWidth)
                                    }
                                    JavaScriptEngine.disableAutoZoomOnInputs(this@apply)
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        isErrorState = true
                                        isLoading = false
                                    }
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    progress = newProgress.toFloat()
                                    if (newProgress == 100) isLoading = false
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    title?.let {
                                        currentTitle = it
                                        onUpdateTab(currentUrl, it, null, null)
                                    }
                                }

                                override fun onShowFileChooser(
                                    webView: WebView?,
                                    filePathCallbackParam: ValueCallback<Array<Uri>>?,
                                    fileChooserParams: FileChooserParams?
                                ): Boolean {
                                    filePathCallback?.onReceiveValue(null)
                                    filePathCallback = filePathCallbackParam
                                    val intent = fileChooserParams?.createIntent()
                                        ?: Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
                                    filePickerLauncher.launch(intent)
                                    return true
                                }

                                override fun onGeolocationPermissionsShowPrompt(
                                    origin: String?,
                                    callback: GeolocationPermissions.Callback?
                                ) {
                                    callback?.invoke(origin, true, false)
                                }

                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    request?.grant(request.resources)
                                }
                            }

                            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                                val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)
                                downloadFile(url, mimetype, filename)
                            }

                            setOnLongClickListener {
                                if (touchMode == "Klik kanan") {
                                    val result = hitTestResult
                                    val extra = result.extra
                                    when (result.type) {
                                        WebView.HitTestResult.SRC_ANCHOR_TYPE, WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                                            if (extra != null) {
                                                onVibrateMedium()
                                                contextMenuTarget = ContextMenuTarget.LinkTarget(extra)
                                                return@setOnLongClickListener true
                                            }
                                        }
                                        WebView.HitTestResult.IMAGE_TYPE -> {
                                            if (extra != null) {
                                                onVibrateMedium()
                                                contextMenuTarget = ContextMenuTarget.ImageTarget(extra)
                                                return@setOnLongClickListener true
                                            }
                                        }
                                        else -> {
                                            onVibrateMedium()
                                            contextMenuTarget = ContextMenuTarget.PageTarget(currentUrl, canGoBack)
                                            return@setOnLongClickListener true
                                        }
                                    }
                                }
                                false
                            }

                            webViewRef = this
                            if (currentUrl.isNotBlank()) {
                                loadUrl(currentUrl)
                            }
                        }
                    },
                    update = { view ->
                        webViewRef = view
                        val targetUA = UrlUtils.getUserAgent(settings.browserIdentity, activeTab.isDesktopMode)
                        if (view.settings.userAgentString != targetUA) {
                            view.settings.userAgentString = targetUA
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Virtual Touchpad Layer
                TouchpadOverlay(
                    isEnabled = touchMode == "Kursor",
                    webView = webViewRef,
                    sensitivity = settings.cursorSensitivity,
                    scrollSpeed = settings.scrollSpeed,
                    cursorSizeStr = settings.cursorSize,
                    longPressDurationMs = settings.longPressDurationMs,
                    onVibrateShort = onVibrateShort,
                    onVibrateMedium = onVibrateMedium
                )
            }

            // Floating Touch Mode Button
            if (currentUrl.isNotBlank() && !isErrorState) {
                FloatingTouchButton(
                    currentMode = touchMode,
                    onToggleMode = {
                        val newMode = if (touchMode == "Kursor") "Klik kanan" else "Kursor"
                        touchMode = newMode
                        onVibrateShort()
                        Toast.makeText(context, "Mode: $newMode", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Desktop Keyboard Bar
        DesktopKeyboardBar(
            isVisible = showKeyboardBar,
            onKeyPress = { key, code, ctrl, alt, shift ->
                webViewRef?.let { wv ->
                    JavaScriptEngine.dispatchKeyboardEvent(wv, key, code, ctrl, alt, shift)
                }
            }
        )
    }

    // Native Context Menu Dialog
    contextMenuTarget?.let { target ->
        ContextMenuDialog(
            target = target,
            onDismiss = { contextMenuTarget = null },
            onOpenUrl = { navigateTo(it) },
            onOpenInNewTab = { onNewTab(it) },
            onCopyText = { copyToClipboard(it) },
            onShareUrl = { shareUrl(it) },
            onSaveImage = { downloadFile(it) },
            onSearchWeb = { navigateTo(it) },
            onReloadPage = { webViewRef?.reload() },
            onGoBackPage = { webViewRef?.goBack() },
            onAddBookmark = {
                onAddBookmark(currentUrl, currentTitle)
                Toast.makeText(context, "Bookmark disimpan", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Add Shortcut Dialog
    if (showShortcutDialog) {
        ShortcutEditDialog(
            onSave = { title, url ->
                onAddShortcut(title, url)
                showShortcutDialog = false
                Toast.makeText(context, "Pintasan ditambahkan", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showShortcutDialog = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StartScreenContent(
    shortcuts: List<ShortcutItem>,
    bookmarks: List<BookmarkItem>,
    searchEngine: String,
    onSearch: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onAddShortcut: () -> Unit,
    onDeleteShortcut: (String) -> Unit
) {
    var queryText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("start_screen_container")
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Big App Header Logo
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = "BroDesk Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "BroDesk",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large Search Bar (Guaranteed Google Search on Enter/Go)
        OutlinedTextField(
            value = queryText,
            onValueChange = { queryText = it },
            placeholder = { Text("Cari di $searchEngine atau ketik URL...", fontSize = 15.sp) },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.primary)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (queryText.isNotBlank()) {
                        onSearch(queryText)
                    }
                }
            ),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_search_input")
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Shortcuts Grid
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Pintasan Favorit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onAddShortcut, modifier = Modifier.testTag("add_shortcut_icon_button")) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pintasan", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            items(shortcuts) { sc ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { onOpenUrl(sc.url) },
                            onLongClick = {
                                onDeleteShortcut(sc.id)
                                Toast.makeText(context, "Pintasan dihapus", Toast.LENGTH_SHORT).show()
                            }
                        )
                        .testTag("shortcut_item_${sc.id}")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = sc.title.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sc.title,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Bookmarks Row
        Text(
            text = "Bookmark Terbaru",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (bookmarks.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Belum ada bookmark tersimpan. Ketuk ikon bintang saat membuka situs favorit Anda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(bookmarks.take(10)) { bm ->
                    Card(
                        onClick = { onOpenUrl(bm.url) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .width(140.dp)
                            .testTag("recent_bookmark_card_${bm.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = bm.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = UrlUtils.getDomain(bm.url),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CleanErrorScreen(onRetry: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("clean_error_screen")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Halaman Tidak Dapat Dimuat",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Periksa koneksi internet Anda atau alamat web yang dimasukkan.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.testTag("error_retry_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}

private object URLUtil {
    fun guessFileName(url: String, contentDisposition: String?, mimeType: String?): String {
        return android.webkit.URLUtil.guessFileName(url, contentDisposition, mimeType)
    }
}
