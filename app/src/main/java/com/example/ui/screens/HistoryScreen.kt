package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoryItem
import com.example.services.UrlUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    history: List<HistoryItem>,
    onOpenUrl: (String) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearHistory: (range: String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredHistory = history.filter { item ->
        searchQuery.isEmpty() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.url.contains(searchQuery, ignoreCase = true)
    }

    val groupedHistory = remember(filteredHistory) {
        filteredHistory.groupBy { formatDateGroup(it.visitedAt) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("history_screen_root")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Riwayat Kunjungan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { showClearDialog = true },
                enabled = history.isNotEmpty(),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("clear_history_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hapus Riwayat", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari riwayat...", fontSize = 14.sp) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("history_search_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // History Content
        if (groupedHistory.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Riwayat masih kosong",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.weight(1f)
            ) {
                groupedHistory.forEach { (dateGroup, items) ->
                    item {
                        Text(
                            text = dateGroup,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }

                    items(items) { historyItem ->
                        Card(
                            onClick = { onOpenUrl(historyItem.url) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("history_item_card_${historyItem.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = historyItem.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = UrlUtils.getDomain(historyItem.url),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = formatTime(historyItem.visitedAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        onDeleteHistoryItem(historyItem.id)
                                        Toast.makeText(context, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("delete_history_item_${historyItem.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus Item",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear History Dialog with Range selection (1 jam, Hari ini, Semua)
    if (showClearDialog) {
        var selectedRange by remember { mutableStateOf("all") } // "1hour", "today", "all"

        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Hapus Riwayat Kunjungan") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Pilih rentang waktu riwayat yang ingin dihapus:")
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedRange == "1hour",
                            onClick = { selectedRange = "1hour" },
                            modifier = Modifier.testTag("range_1hour_radio")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1 jam terakhir")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedRange == "today",
                            onClick = { selectedRange = "today" },
                            modifier = Modifier.testTag("range_today_radio")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hari ini")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedRange == "all",
                            onClick = { selectedRange = "all" },
                            modifier = Modifier.testTag("range_all_radio")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Semua riwayat")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory(selectedRange)
                        showClearDialog = false
                        Toast.makeText(context, "Riwayat telah dihapus", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }, modifier = Modifier.testTag("cancel_clear_history_button")) {
                    Text("Batal")
                }
            },
            modifier = Modifier.testTag("clear_history_dialog")
        )
    }
}

private fun formatDateGroup(timeMs: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timeMs
    return when {
        diff < 86400_000L -> "Hari ini"
        diff < 172800_000L -> "Kemarin"
        else -> SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date(timeMs))
    }
}

private fun formatTime(timeMs: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeMs))
}
