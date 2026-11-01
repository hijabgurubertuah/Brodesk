package com.example.data.model

import java.util.UUID

data class TabItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Tab Baru",
    val url: String = "",
    val iconUrl: String? = null,
    val isDesktopMode: Boolean = true,
    val order: Int = 0,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class BookmarkItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val folderId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class BookmarkFolder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val order: Int = 0
)

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis()
)

data class ShortcutItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val order: Int = 0
)

data class AppSettings(
    val defaultDesktopMode: Boolean = true,
    val browserIdentity: String = "Chrome Windows",
    val desktopWidth: Int = 1280,
    val theme: String = "system",
    val cursorSensitivity: Float = 1.0f,
    val scrollSpeed: Float = 1.0f,
    val cursorSize: String = "Sedang",
    val longPressDurationMs: Long = 500L,
    val vibrationEnabled: Boolean = true,
    val defaultLongPressMode: String = "Klik kanan",
    val searchEngine: String = "Google",
    val blockPopups: Boolean = true
)

data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val filename: String,
    val url: String,
    val mimeType: String = "",
    val contentLength: Long = 0L,
    val downloadedAt: Long = System.currentTimeMillis(),
    val status: String = "SELESAI", // SELESAI, GAGAL, BERJALAN
    val localPath: String = ""
)
