package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FindInPageBar(
    isVisible: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    matchIndex: Int,
    matchCount: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClose: () -> Unit
) {
    if (!isVisible) return

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("find_in_page_bar")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Cari di halaman...", fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onNext() }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("find_in_page_input")
            )

            if (query.isNotEmpty()) {
                Text(
                    text = if (matchCount > 0) "${matchIndex + 1}/$matchCount" else "0/0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            IconButton(
                onClick = onPrev,
                enabled = query.isNotEmpty() && matchCount > 0,
                modifier = Modifier.testTag("find_prev_button")
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Sebelumnya")
            }

            IconButton(
                onClick = onNext,
                enabled = query.isNotEmpty() && matchCount > 0,
                modifier = Modifier.testTag("find_next_button")
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Berikutnya")
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("find_close_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Tutup cari")
            }
        }
    }
}
