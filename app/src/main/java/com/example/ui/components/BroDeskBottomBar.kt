package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp

sealed class BottomDestination(
    val route: String,
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Browser : BottomDestination("browser", "Browser", Icons.Filled.Language, Icons.Outlined.Language)
    object Tab : BottomDestination("tab", "Tab", Icons.Filled.Layers, Icons.Outlined.Layers)
    object Bookmark : BottomDestination("bookmark", "Bookmark", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder)
    object History : BottomDestination("history", "Riwayat", Icons.Filled.History, Icons.Outlined.History)
    object Settings : BottomDestination("settings", "Pengaturan", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun BroDeskBottomBar(
    currentRoute: String,
    tabCount: Int,
    onNavigate: (String) -> Unit
) {
    val destinations = listOf(
        BottomDestination.Browser,
        BottomDestination.Tab,
        BottomDestination.Bookmark,
        BottomDestination.History,
        BottomDestination.Settings
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("brodesk_bottom_navigation_bar")
    ) {
        destinations.forEach { dest ->
            val isSelected = currentRoute == dest.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(dest.route) },
                icon = {
                    if (dest == BottomDestination.Tab) {
                        BadgedBox(
                            badge = {
                                if (tabCount > 0) {
                                    Badge {
                                        Text(text = tabCount.toString(), fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.title
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = dest.title
                        )
                    }
                },
                label = { Text(dest.title, fontSize = 11.sp) },
                modifier = Modifier.testTag("bottom_tab_${dest.route}")
            )
        }
    }
}
