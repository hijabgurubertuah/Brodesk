package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.BookmarkFolder

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Ya",
    dismissText: String = "Batal",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag("dialog_confirm_button")) {
                Text(confirmText, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_dismiss_button")) {
                Text(dismissText)
            }
        },
        modifier = Modifier.testTag("confirm_dialog")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkEditDialog(
    initialTitle: String,
    initialUrl: String,
    initialFolderId: String?,
    folders: List<BookmarkFolder>,
    onSave: (title: String, url: String, folderId: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var url by remember { mutableStateOf(initialUrl) }
    var selectedFolderId by remember { mutableStateOf(initialFolderId) }
    var expandedFolderMenu by remember { mutableStateOf(false) }

    val selectedFolderName = folders.find { it.id == selectedFolderId }?.name ?: "Tanpa Folder"

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(if (initialTitle.isEmpty()) "Tambah Bookmark" else "Ubah Bookmark") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bookmark_title_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Alamat (URL)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bookmark_url_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedFolderMenu,
                    onExpandedChange = { expandedFolderMenu = !expandedFolderMenu }
                ) {
                    OutlinedTextField(
                        value = selectedFolderName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Folder") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFolderMenu) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                            .testTag("bookmark_folder_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFolderMenu,
                        onDismissRequest = { expandedFolderMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tanpa Folder") },
                            onClick = {
                                selectedFolderId = null
                                expandedFolderMenu = false
                            }
                        )
                        folders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder.name) },
                                onClick = {
                                    selectedFolderId = folder.id
                                    expandedFolderMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (url.isNotBlank()) {
                        onSave(title, url, selectedFolderId)
                    }
                },
                modifier = Modifier.testTag("save_bookmark_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_bookmark_button")) {
                Text("Batal")
            }
        },
        modifier = Modifier.testTag("bookmark_edit_dialog")
    )
}

@Composable
fun FolderEditDialog(
    initialName: String = "",
    onSave: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(if (initialName.isEmpty()) "Folder Baru" else "Ubah Folder") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Folder") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("folder_name_input")
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim())
                    }
                },
                modifier = Modifier.testTag("save_folder_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_folder_button")) {
                Text("Batal")
            }
        },
        modifier = Modifier.testTag("folder_edit_dialog")
    )
}

@Composable
fun ShortcutEditDialog(
    initialTitle: String = "",
    initialUrl: String = "",
    onSave: (title: String, url: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var url by remember { mutableStateOf(initialUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Tambah Pintasan Situs") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Pintasan") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shortcut_title_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Alamat (URL)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shortcut_url_input")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && url.isNotBlank()) {
                        onSave(title.trim(), url.trim())
                    }
                },
                modifier = Modifier.testTag("save_shortcut_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_shortcut_button")) {
                Text("Batal")
            }
        },
        modifier = Modifier.testTag("shortcut_edit_dialog")
    )
}
