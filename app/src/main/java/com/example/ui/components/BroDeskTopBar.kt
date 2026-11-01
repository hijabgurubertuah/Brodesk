package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BroDeskTopBar(
    currentUrl: String,
    isLoading: Boolean,
    progress: Float,
    isBookmarked: Boolean,
    isDesktopMode: Boolean,
    isFullScreen: Boolean,
    showKeyboardBar: Boolean,
    onNavigateUrl: (String) -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onToggleBookmark: () -> Unit,
    onNewTab: () -> Unit,
    onFindInPage: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onLoadMobileVersion: () -> Unit,
    onToggleFullScreen: () -> Unit,
    onToggleKeyboardBar: () -> Unit,
    onOpenDownloads: () -> Unit,
    onShareLink: () -> Unit,
    onCopyLink: () -> Unit
) {
    var urlText by remember(currentUrl) { mutableStateOf(currentUrl) }
    var isEditing by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val isHttps = currentUrl.startsWith("https://")
    val isHttp = currentUrl.startsWith("http://")

    Column {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("brodesk_top_bar")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Address Field with Lock/Search Icon
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    placeholder = { Text("Ketik URL atau kata kunci", fontSize = 14.sp) },
                    singleLine = true,
                    leadingIcon = {
                        if (isEditing) {
                            Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.primary)
                        } else if (isHttps) {
                            Icon(Icons.Default.Lock, contentDescription = "Aman (HTTPS)", tint = Color(0xFF10B981))
                        } else if (isHttp) {
                            Icon(Icons.Default.Warning, contentDescription = "Tidak Aman (HTTP)", tint = Color(0xFFF59E0B))
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    trailingIcon = {
                        if (isEditing && urlText.isNotEmpty()) {
                            IconButton(onClick = { urlText = "" }, modifier = Modifier.testTag("clear_url_button")) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus teks")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            isEditing = false
                            onNavigateUrl(urlText)
                        }
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .onFocusChanged { isEditing = it.isFocused }
                        .testTag("url_address_input")
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Reload or Stop
                IconButton(
                    onClick = { if (isLoading) onStop() else onReload() },
                    modifier = Modifier.testTag("reload_stop_button")
                ) {
                    if (isLoading) {
                        Icon(Icons.Default.Close, contentDescription = "Henti")
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat ulang")
                    }
                }

                // Bookmark Star
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("bookmark_star_button")
                ) {
                    if (isBookmarked) {
                        Icon(Icons.Default.Star, contentDescription = "Hapus Bookmark", tint = MaterialTheme.colorScheme.secondary)
                    } else {
                        Icon(Icons.Default.StarBorder, contentDescription = "Tambah Bookmark")
                    }
                }

                // 3-Dots Overflow Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("overflow_menu_button")
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu Utama")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.testTag("overflow_dropdown_menu")
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tab baru") },
                            leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onNewTab()
                            },
                            modifier = Modifier.testTag("menu_new_tab")
                        )
                        DropdownMenuItem(
                            text = { Text("Cari di halaman") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onFindInPage()
                            },
                            modifier = Modifier.testTag("menu_find_in_page")
                        )
                        DropdownMenuItem(
                            text = { Text(if (isDesktopMode) "Mode desktop (Aktif)" else "Mode desktop") },
                            leadingIcon = { Icon(Icons.Default.DesktopWindows, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onToggleDesktopMode()
                            },
                            modifier = Modifier.testTag("menu_desktop_mode")
                        )
                        DropdownMenuItem(
                            text = { Text("Muat versi HP") },
                            leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onLoadMobileVersion()
                            },
                            modifier = Modifier.testTag("menu_mobile_version")
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(if (isFullScreen) "Keluar layar penuh" else "Layar penuh") },
                            leadingIcon = { Icon(Icons.Default.Fullscreen, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onToggleFullScreen()
                            },
                            modifier = Modifier.testTag("menu_fullscreen")
                        )
                        DropdownMenuItem(
                            text = { Text(if (showKeyboardBar) "Sembunyikan keyboard desktop" else "Papan tombol desktop") },
                            leadingIcon = { Icon(Icons.Default.Keyboard, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onToggleKeyboardBar()
                            },
                            modifier = Modifier.testTag("menu_desktop_keyboard")
                        )
                        DropdownMenuItem(
                            text = { Text("Unduhan") },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenDownloads()
                            },
                            modifier = Modifier.testTag("menu_downloads")
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Bagikan tautan") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onShareLink()
                            },
                            modifier = Modifier.testTag("menu_share_link")
                        )
                        DropdownMenuItem(
                            text = { Text("Salin tautan") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onCopyLink()
                            },
                            modifier = Modifier.testTag("menu_copy_link")
                        )
                    }
                }
            }
        }

        // Thin loading progress bar
        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .testTag("loading_progress_bar")
            )
        }
    }
}
