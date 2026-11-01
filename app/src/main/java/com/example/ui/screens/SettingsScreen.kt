package com.example.ui.screens

import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.BookmarkFolder
import com.example.data.model.BookmarkItem
import com.example.data.model.HistoryItem
import com.example.ui.components.ConfirmDialog
import java.io.InputStream
import java.io.OutputStream

@Composable
fun SettingsScreen(
    settings: AppSettings,
    bookmarks: List<BookmarkItem>,
    folders: List<BookmarkFolder>,
    history: List<HistoryItem>,
    onOpenUrl: (String) -> Unit,
    onOpenInNewTab: (String) -> Unit,
    onAddBookmark: (url: String, title: String, folderId: String?) -> Boolean,
    onUpdateBookmark: (id: String, title: String, url: String, folderId: String?) -> Unit,
    onDeleteBookmark: (id: String) -> Unit,
    onAddFolder: (name: String) -> BookmarkFolder,
    onDeleteFolder: (id: String) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearHistory: (range: String) -> Unit,
    onUpdateSettings: (AppSettings) -> Unit,
    onClearHistoryAll: () -> Unit,
    onExportBackup: () -> String,
    onImportBackup: (String) -> Boolean
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) }

    var showConfirmRestoreDialog by remember { mutableStateOf(false) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }

    // Backup Save Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val json = onExportBackup()
                context.contentResolver.openOutputStream(uri)?.use { stream: OutputStream ->
                    stream.write(json.toByteArray())
                }
                Toast.makeText(context, "Cadangan berhasil disimpan", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal menyimpan cadangan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Restore File Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val json = inputStream?.bufferedReader()?.use { it.readText() }
                if (!json.isNullOrBlank()) {
                    pendingRestoreJson = json
                    showConfirmRestoreDialog = true
                } else {
                    Toast.makeText(context, "Berkas cadangan tidak valid", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membaca berkas cadangan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen_root")
    ) {
        // Top Section Selector (Pengaturan, Bookmark, Riwayat)
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (selectedSection) {
                    1 -> "Bookmark"
                    2 -> "Riwayat Kunjungan"
                    else -> "Pengaturan"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val tabs = listOf("Pengaturan", "Bookmark", "Riwayat")
                tabs.forEachIndexed { index, title ->
                    SegmentedButton(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                        icon = {
                            when (index) {
                                1 -> Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                                2 -> Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                else -> Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        modifier = Modifier.testTag("settings_tab_$index")
                    ) {
                        Text(title, fontSize = 12.sp)
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedSection) {
                1 -> {
                    // Bookmark Screen
                    BookmarkScreen(
                        bookmarks = bookmarks,
                        folders = folders,
                        onOpenUrl = onOpenUrl,
                        onOpenInNewTab = onOpenInNewTab,
                        onAddBookmark = onAddBookmark,
                        onUpdateBookmark = onUpdateBookmark,
                        onDeleteBookmark = onDeleteBookmark,
                        onAddFolder = onAddFolder,
                        onDeleteFolder = onDeleteFolder
                    )
                }

                2 -> {
                    // History Screen
                    HistoryScreen(
                        history = history,
                        onOpenUrl = onOpenUrl,
                        onDeleteHistoryItem = onDeleteHistoryItem,
                        onClearHistory = onClearHistory
                    )
                }

                else -> {
                    // System Settings Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 80.dp)
                    ) {
                        // SECTION 1: TAMPILAN
                        SettingsCategoryCard(title = "Tampilan", icon = Icons.Default.DesktopWindows) {
                            SettingsSwitchItem(
                                title = "Mode Desktop Bawaan",
                                subtitle = "Selalu muat versi komputer saat membuka situs",
                                checked = settings.defaultDesktopMode,
                                onCheckedChange = { onUpdateSettings(settings.copy(defaultDesktopMode = it)) },
                                tag = "setting_default_desktop_switch"
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            Text("Identitas Browser (User Agent)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            val uaOptions = listOf("Chrome Windows", "Chrome macOS", "Firefox Linux")
                            uaOptions.forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = settings.browserIdentity == option,
                                        onClick = { onUpdateSettings(settings.copy(browserIdentity = option)) },
                                        modifier = Modifier.testTag("ua_radio_$option")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(option, fontSize = 14.sp)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            Text("Lebar Tampilan Desktop", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                val widths = listOf(1024, 1280, 1440)
                                widths.forEachIndexed { index, w ->
                                    SegmentedButton(
                                        selected = settings.desktopWidth == w,
                                        onClick = { onUpdateSettings(settings.copy(desktopWidth = w)) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = widths.size),
                                        modifier = Modifier.testTag("width_seg_$w")
                                    ) {
                                        Text("${w}px", fontSize = 12.sp)
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            Text("Tema Aplikasi", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            val themeOptions = listOf("system" to "Ikuti sistem", "light" to "Terang", "dark" to "Gelap")
                            themeOptions.forEach { (key, label) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = settings.theme == key,
                                        onClick = { onUpdateSettings(settings.copy(theme = key)) },
                                        modifier = Modifier.testTag("theme_radio_$key")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(label, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // SECTION 2: SENTUHAN & KURSOR
                        SettingsCategoryCard(title = "Sentuhan & Kursor Virtual", icon = Icons.Default.Mouse) {
                            Text("Mode Tekan Lama Bawaan", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                val modes = listOf("Klik kanan", "Kursor")
                                modes.forEachIndexed { index, mode ->
                                    SegmentedButton(
                                        selected = settings.defaultLongPressMode == mode,
                                        onClick = { onUpdateSettings(settings.copy(defaultLongPressMode = mode)) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                                        modifier = Modifier.testTag("mode_seg_$mode")
                                    ) {
                                        Text(mode, fontSize = 12.sp)
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            Text("Sensitivitas Kursor (${String.format("%.1f", settings.cursorSensitivity)}x)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = settings.cursorSensitivity,
                                onValueChange = { onUpdateSettings(settings.copy(cursorSensitivity = it)) },
                                valueRange = 0.5f..2.0f,
                                steps = 15,
                                modifier = Modifier.testTag("cursor_sensitivity_slider")
                            )

                            Text("Kecepatan Gulir (${String.format("%.1f", settings.scrollSpeed)}x)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = settings.scrollSpeed,
                                onValueChange = { onUpdateSettings(settings.copy(scrollSpeed = it)) },
                                valueRange = 0.5f..2.0f,
                                steps = 15,
                                modifier = Modifier.testTag("scroll_speed_slider")
                            )

                            Text("Ukuran Kursor", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                val sizes = listOf("Kecil", "Sedang", "Besar")
                                sizes.forEachIndexed { index, s ->
                                    SegmentedButton(
                                        selected = settings.cursorSize == s,
                                        onClick = { onUpdateSettings(settings.copy(cursorSize = s)) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = sizes.size),
                                        modifier = Modifier.testTag("cursor_size_$s")
                                    ) {
                                        Text(s, fontSize = 12.sp)
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            Text("Durasi Tekan Lama (${settings.longPressDurationMs} ms)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = settings.longPressDurationMs.toFloat(),
                                onValueChange = { onUpdateSettings(settings.copy(longPressDurationMs = it.toLong())) },
                                valueRange = 300f..900f,
                                steps = 11,
                                modifier = Modifier.testTag("longpress_duration_slider")
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            SettingsSwitchItem(
                                title = "Umpan Balik Getar",
                                subtitle = "Getar halus saat klik kanan dan mengganti mode",
                                checked = settings.vibrationEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(vibrationEnabled = it)) },
                                tag = "vibration_switch"
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // SECTION 3: PENCARIAN
                        SettingsCategoryCard(title = "Pencarian", icon = Icons.Default.Search) {
                            Text("Mesin Pencari Bawaan", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            val engines = listOf("Google", "Bing", "DuckDuckGo")
                            engines.forEach { engine ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = settings.searchEngine == engine,
                                        onClick = { onUpdateSettings(settings.copy(searchEngine = engine)) },
                                        modifier = Modifier.testTag("engine_radio_$engine")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(engine, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // SECTION 4: PRIVASI & PEMBERSIHAN
                        SettingsCategoryCard(title = "Privasi", icon = Icons.Default.Security) {
                            SettingsSwitchItem(
                                title = "Blokir Pop-up",
                                subtitle = "Otomatis mencegah pembukaan jendela iklan pop-up",
                                checked = settings.blockPopups,
                                onCheckedChange = { onUpdateSettings(settings.copy(blockPopups = it)) },
                                tag = "block_popups_switch"
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            OutlinedButton(
                                onClick = {
                                    try {
                                        WebView(context).clearCache(true)
                                        Toast.makeText(context, "Cache berhasil dibersihkan", Toast.LENGTH_SHORT).show()
                                    } catch (_: Exception) {}
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("clear_cache_button")
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hapus Cache Web")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    try {
                                        CookieManager.getInstance().removeAllCookies(null)
                                        Toast.makeText(context, "Cookie berhasil dibersihkan", Toast.LENGTH_SHORT).show()
                                    } catch (_: Exception) {}
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("clear_cookies_button")
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hapus Cookie")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { showClearHistoryConfirm = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_clear_history_button")
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hapus Seluruh Riwayat")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // SECTION 5: DATA CADANGAN & PULIHKAN
                        SettingsCategoryCard(title = "Cadangkan & Pulihkan Data", icon = Icons.Default.Save) {
                            Text(
                                text = "Simpan atau kembalikan daftar bookmark, pintasan, dan pengaturan aplikasi ke dalam berkas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { exportLauncher.launch("BroDesk_Backup.json") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_backup_button")
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cadangkan Data")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { importLauncher.launch("application/json") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("import_backup_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pulihkan Data")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // SECTION 6: TENTANG APLIKASI
                        SettingsCategoryCard(title = "Tentang Aplikasi", icon = Icons.Default.Info) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "BroDesk",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Versi 1.0.0",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear History Confirmation
    if (showClearHistoryConfirm) {
        ConfirmDialog(
            title = "Hapus Seluruh Riwayat?",
            message = "Apakah Anda yakin ingin menghapus seluruh riwayat kunjungan?",
            confirmText = "Hapus Semua",
            onConfirm = {
                onClearHistoryAll()
                showClearHistoryConfirm = false
                Toast.makeText(context, "Seluruh riwayat berhasil dihapus", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showClearHistoryConfirm = false }
        )
    }

    // Confirm Data Restore
    if (showConfirmRestoreDialog && pendingRestoreJson != null) {
        ConfirmDialog(
            title = "Pulihkan Data Cadangan?",
            message = "Data saat ini (bookmark, pintasan, pengaturan) akan diganti dengan data dari berkas cadangan. Lanjutkan?",
            confirmText = "Pulihkan",
            onConfirm = {
                val ok = onImportBackup(pendingRestoreJson!!)
                showConfirmRestoreDialog = false
                pendingRestoreJson = null
                if (ok) {
                    Toast.makeText(context, "Data berhasil dipulihkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal memulihkan data: Format berkas tidak cocok", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = {
                showConfirmRestoreDialog = false
                pendingRestoreJson = null
            }
        )
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag)
        )
    }
}
