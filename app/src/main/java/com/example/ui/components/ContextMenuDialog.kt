package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

sealed class ContextMenuTarget {
    data class LinkTarget(val url: String) : ContextMenuTarget()
    data class ImageTarget(val imageUrl: String, val linkUrl: String? = null) : ContextMenuTarget()
    data class TextTarget(val text: String) : ContextMenuTarget()
    data class PageTarget(val currentUrl: String, val canGoBack: Boolean) : ContextMenuTarget()
}

@Composable
fun ContextMenuDialog(
    target: ContextMenuTarget,
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenInNewTab: (String) -> Unit,
    onCopyText: (String) -> Unit,
    onShareUrl: (String) -> Unit,
    onSaveImage: (String) -> Unit,
    onSearchWeb: (String) -> Unit,
    onReloadPage: () -> Unit,
    onGoBackPage: () -> Unit,
    onAddBookmark: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("context_menu_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when (target) {
                    is ContextMenuTarget.LinkTarget -> {
                        Text(
                            text = target.url,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        ContextMenuItem(
                            icon = Icons.AutoMirrored.Filled.OpenInNew,
                            label = "Buka",
                            tag = "ctx_open_link"
                        ) {
                            onOpenUrl(target.url)
                            onDismiss()
                        }
                        ContextMenuItem(
                            icon = Icons.AutoMirrored.Filled.OpenInNew,
                            label = "Buka di tab baru",
                            tag = "ctx_open_link_new_tab"
                        ) {
                            onOpenInNewTab(target.url)
                            onDismiss()
                        }
                        ContextMenuItem(
                            icon = Icons.Default.ContentCopy,
                            label = "Salin tautan",
                            tag = "ctx_copy_link"
                        ) {
                            onCopyText(target.url)
                            onDismiss()
                        }
                        ContextMenuItem(
                            icon = Icons.Default.Share,
                            label = "Bagikan tautan",
                            tag = "ctx_share_link"
                        ) {
                            onShareUrl(target.url)
                            onDismiss()
                        }
                    }

                    is ContextMenuTarget.ImageTarget -> {
                        Text(
                            text = "Gambar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        ContextMenuItem(
                            icon = Icons.Default.Download,
                            label = "Simpan gambar",
                            tag = "ctx_save_image"
                        ) {
                            onSaveImage(target.imageUrl)
                            onDismiss()
                        }
                        ContextMenuItem(
                            icon = Icons.Default.ContentCopy,
                            label = "Salin alamat gambar",
                            tag = "ctx_copy_image_url"
                        ) {
                            onCopyText(target.imageUrl)
                            onDismiss()
                        }
                        ContextMenuItem(
                            icon = Icons.Default.Image,
                            label = "Buka gambar di tab baru",
                            tag = "ctx_open_image_new_tab"
                        ) {
                            onOpenInNewTab(target.imageUrl)
                            onDismiss()
                        }
                        if (target.linkUrl != null) {
                            ContextMenuItem(
                                icon = Icons.Default.Link,
                                label = "Buka tautan gambar",
                                tag = "ctx_open_image_link"
                            ) {
                                onOpenUrl(target.linkUrl)
                                onDismiss()
                            }
                        }
                    }

                    is ContextMenuTarget.TextTarget -> {
                        Text(
                            text = "\"${target.text}\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        ContextMenuItem(
                            icon = Icons.Default.ContentCopy,
                            label = "Salin",
                            tag = "ctx_copy_text"
                        ) {
                            onCopyText(target.text)
                            onDismiss()
                        }
                        ContextMenuItem(
                            icon = Icons.Default.Search,
                            label = "Cari di web",
                            tag = "ctx_search_web"
                        ) {
                            onSearchWeb(target.text)
                            onDismiss()
                        }
                    }

                    is ContextMenuTarget.PageTarget -> {
                        Text(
                            text = "Halaman",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        ContextMenuItem(
                            icon = Icons.Default.Refresh,
                            label = "Muat ulang",
                            tag = "ctx_reload_page"
                        ) {
                            onReloadPage()
                            onDismiss()
                        }
                        if (target.canGoBack) {
                            ContextMenuItem(
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                label = "Kembali",
                                tag = "ctx_back_page"
                            ) {
                                onGoBackPage()
                                onDismiss()
                            }
                        }
                        ContextMenuItem(
                            icon = Icons.Default.Star,
                            label = "Simpan bookmark",
                            tag = "ctx_bookmark_page"
                        ) {
                            onAddBookmark()
                            onDismiss()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
