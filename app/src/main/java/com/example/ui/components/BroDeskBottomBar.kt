package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .testTag("brodesk_bottom_navigation_bar")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp)
        ) {
            // 1. Back Button
            IconButton(
                onClick = onGoBack,
                enabled = canGoBack,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("bottom_nav_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = if (canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // 2. Forward Button
            IconButton(
                onClick = onGoForward,
                enabled = canGoForward,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("bottom_nav_forward")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Maju",
                    tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // 3. Home Button
            IconButton(
                onClick = onGoHome,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("bottom_nav_home")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Beranda",
                    tint = if (currentRoute == "browser") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(19.dp)
                )
            }

            // 4. Tab Manager Button [ 1 ]
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clickable(onClick = onOpenTabs)
                    .testTag("bottom_nav_tabs")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .border(
                            width = 1.5.dp,
                            color = if (currentRoute == "tab") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Text(
                        text = tabCount.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentRoute == "tab") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 5. Share Button
            IconButton(
                onClick = onSharePage,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("bottom_nav_share")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Bagikan",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            // 6. Settings Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("bottom_nav_settings")
            ) {
                Icon(
                    imageVector = if (currentRoute == "settings") Icons.Default.Settings else Icons.Outlined.Settings,
                    contentDescription = "Pengaturan",
                    tint = if (currentRoute == "settings") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
