package com.example.data.model

object SeedData {
    val defaultShortcuts = listOf(
        ShortcutItem(id = "sc_google", title = "Google", url = "https://google.com", order = 0),
        ShortcutItem(id = "sc_youtube", title = "YouTube", url = "https://youtube.com", order = 1),
        ShortcutItem(id = "sc_wikipedia", title = "Wikipedia", url = "https://id.wikipedia.org", order = 2),
        ShortcutItem(id = "sc_gmail", title = "Gmail", url = "https://mail.google.com", order = 3),
        ShortcutItem(id = "sc_drive", title = "Google Drive", url = "https://drive.google.com", order = 4),
        ShortcutItem(id = "sc_classroom", title = "Google Classroom", url = "https://classroom.google.com", order = 5),
        ShortcutItem(id = "sc_canva", title = "Canva", url = "https://canva.com", order = 6),
        ShortcutItem(id = "sc_github", title = "GitHub", url = "https://github.com", order = 7)
    )

    val defaultBookmarks = listOf(
        BookmarkItem(id = "bm_docs", title = "Google Docs", url = "https://docs.google.com"),
        BookmarkItem(id = "bm_kemdikbud", title = "Kemdikbud", url = "https://kemdikbud.go.id"),
        BookmarkItem(id = "bm_detik", title = "Detik", url = "https://detik.com")
    )

    val defaultSettings = AppSettings(
        defaultDesktopMode = true,
        browserIdentity = "Chrome Windows",
        desktopWidth = 1280,
        theme = "system",
        cursorSensitivity = 1.0f,
        scrollSpeed = 1.0f,
        cursorSize = "Sedang",
        longPressDurationMs = 500L,
        vibrationEnabled = true,
        defaultLongPressMode = "Klik kanan",
        searchEngine = "Google",
        blockPopups = true
    )
}
