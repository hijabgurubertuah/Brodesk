package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BroDeskBottomBar(
    currentRoute: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onGoHome: () -> Unit,
    onOpenTabs: () -> Unit,
    onSharePage: () -> Unit,
    onOpenSettings: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("brodesk_bottom_navigation_bar")
    ) {
        // 1. Back Button
        IconButton(
            onClick = onGoBack,
            enabled = canGoBack,
            modifier = Modifier.weight(1f).testTag("bottom_nav_back")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = if (canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }

        // 2. Forward Button
        IconButton(
            onClick = onGoForward,
            enabled = canGoForward,
            modifier = Modifier.weight(1f).testTag("bottom_nav_forward")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Maju",
                tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }

        // 3. Home Button
        IconButton(
            onClick = onGoHome,
            modifier = Modifier.weight(1f).testTag("bottom_nav_home")
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Beranda",
                tint = if (currentRoute == "browser") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        // 4. Tab Manager Button [ 1 ]
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpenTabs)
                .testTag("bottom_nav_tabs")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .border(
                        width = 2.dp,
                        color = if (currentRoute == "tab") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    text = tabCount.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentRoute == "tab") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 5. Share Button
        IconButton(
            onClick = onSharePage,
            modifier = Modifier.weight(1f).testTag("bottom_nav_share")
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Bagikan"
            )
        }

        // 6. Settings Button
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.weight(1f).testTag("bottom_nav_settings")
        ) {
            Icon(
                imageVector = if (currentRoute == "settings") Icons.Default.Settings else Icons.Outlined.Settings,
                contentDescription = "Pengaturan",
                tint = if (currentRoute == "settings") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
