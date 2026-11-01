package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookmarkFolder
import com.example.data.model.BookmarkItem
import com.example.services.UrlUtils
import com.example.ui.components.BookmarkEditDialog
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.FolderEditDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkScreen(
    bookmarks: List<BookmarkItem>,
    folders: List<BookmarkFolder>,
    onOpenUrl: (String) -> Unit,
    onOpenInNewTab: (String) -> Unit,
    onAddBookmark: (url: String, title: String, folderId: String?) -> Boolean,
    onUpdateBookmark: (id: String, title: String, url: String, folderId: String?) -> Unit,
    onDeleteBookmark: (id: String) -> Unit,
    onAddFolder: (name: String) -> BookmarkFolder,
    onDeleteFolder: (id: String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFolderId by remember { mutableStateOf<String?>(null) }

    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var editingBookmark by remember { mutableStateOf<BookmarkItem?>(null) }
    var bookmarkToDelete by remember { mutableStateOf<BookmarkItem?>(null) }

    var showAddFolderDialog by remember { mutableStateOf(false) }
    var folderToDelete by remember { mutableStateOf<BookmarkFolder?>(null) }

    val filteredBookmarks = bookmarks.filter { bm ->
        val matchesFolder = selectedFolderId == null || bm.folderId == selectedFolderId
        val matchesQuery = searchQuery.isEmpty() ||
                bm.title.contains(searchQuery, ignoreCase = true) ||
                bm.url.contains(searchQuery, ignoreCase = true)
        matchesFolder && matchesQuery
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("bookmark_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Bookmark",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { showAddFolderDialog = true },
                    modifier = Modifier.testTag("add_folder_button")
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Tambah Folder", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari bookmark...", fontSize = 14.sp) },
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
                    .testTag("bookmark_search_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Folder Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFolderId == null,
                        onClick = { selectedFolderId = null },
                        label = { Text("Semua (${bookmarks.size})") },
                        modifier = Modifier.testTag("folder_chip_all")
                    )
                }
                items(folders) { folder ->
                    val count = bookmarks.count { it.folderId == folder.id }
                    FilterChip(
                        selected = selectedFolderId == folder.id,
                        onClick = { selectedFolderId = folder.id },
                        label = { Text("${folder.name} ($count)") },
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            IconButton(
                                onClick = { folderToDelete = folder },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Folder", modifier = Modifier.size(14.dp))
                            }
                        },
                        modifier = Modifier.testTag("folder_chip_${folder.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bookmarks List
            if (filteredBookmarks.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak ada bookmark ditemukan",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredBookmarks) { bm ->
                        var showItemMenu by remember { mutableStateOf(false) }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onOpenUrl(bm.url) },
                                    onLongClick = { showItemMenu = true }
                                )
                                .testTag("bookmark_item_card_${bm.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bm.title,
                                        style = MaterialTheme.typography.titleMedium,
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

                                Box {
                                    IconButton(
                                        onClick = { showItemMenu = true },
                                        modifier = Modifier.testTag("bookmark_item_menu_${bm.id}")
                                    ) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Opsi")
                                    }

                                    DropdownMenu(
                                        expanded = showItemMenu,
                                        onDismissRequest = { showItemMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Buka di tab baru") },
                                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null) },
                                            onClick = {
                                                showItemMenu = false
                                                onOpenInNewTab(bm.url)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Ubah") },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                            onClick = {
                                                showItemMenu = false
                                                editingBookmark = bm
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Hapus", color = MaterialTheme.colorScheme.error) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                showItemMenu = false
                                                bookmarkToDelete = bm
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Bookmark FAB
        FloatingActionButton(
            onClick = { showAddBookmarkDialog = true },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_bookmark_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Bookmark")
        }
    }

    // Add Bookmark Dialog
    if (showAddBookmarkDialog) {
        BookmarkEditDialog(
            initialTitle = "",
            initialUrl = "",
            initialFolderId = selectedFolderId,
            folders = folders,
            onSave = { title, url, folderId ->
                val success = onAddBookmark(url, title, folderId)
                if (success) {
                    showAddBookmarkDialog = false
                    Toast.makeText(context, "Bookmark disimpan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Bookmark sudah ada", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showAddBookmarkDialog = false }
        )
    }

    // Edit Bookmark Dialog
    editingBookmark?.let { bm ->
        BookmarkEditDialog(
            initialTitle = bm.title,
            initialUrl = bm.url,
            initialFolderId = bm.folderId,
            folders = folders,
            onSave = { title, url, folderId ->
                onUpdateBookmark(bm.id, title, url, folderId)
                editingBookmark = null
                Toast.makeText(context, "Bookmark diperbarui", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { editingBookmark = null }
        )
    }

    // Delete Bookmark Confirm
    bookmarkToDelete?.let { bm ->
        ConfirmDialog(
            title = "Hapus Bookmark?",
            message = "Apakah Anda yakin ingin menghapus '${bm.title}'?",
            confirmText = "Hapus",
            onConfirm = {
                onDeleteBookmark(bm.id)
                bookmarkToDelete = null
                Toast.makeText(context, "Bookmark dihapus", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { bookmarkToDelete = null }
        )
    }

    // Add Folder Dialog
    if (showAddFolderDialog) {
        FolderEditDialog(
            onSave = { name ->
                onAddFolder(name)
                showAddFolderDialog = false
                Toast.makeText(context, "Folder ditambahkan", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAddFolderDialog = false }
        )
    }

    // Delete Folder Confirm
    folderToDelete?.let { folder ->
        ConfirmDialog(
            title = "Hapus Folder?",
            message = "Apakah Anda yakin ingin menghapus folder '${folder.name}'? Bookmark di dalamnya tidak akan terhapus.",
            confirmText = "Hapus",
            onConfirm = {
                onDeleteFolder(folder.id)
                if (selectedFolderId == folder.id) selectedFolderId = null
                folderToDelete = null
                Toast.makeText(context, "Folder dihapus", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { folderToDelete = null }
        )
    }
}
