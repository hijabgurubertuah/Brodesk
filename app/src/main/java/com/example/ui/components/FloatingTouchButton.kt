package com.example.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun FloatingTouchButton(
    currentMode: String, // "Klik kanan" or "Kursor"
    onToggleMode: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(16f) }
    var offsetY by remember { mutableFloatStateOf(400f) }

    FloatingActionButton(
        onClick = onToggleMode,
        shape = CircleShape,
        containerColor = if (currentMode == "Kursor") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        contentColor = if (currentMode == "Kursor") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(52.dp)
            .testTag("floating_touch_toggle_button")
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        if (currentMode == "Kursor") {
            Icon(
                imageVector = Icons.Default.Mouse,
                contentDescription = "Mode Kursor Aktif"
            )
        } else {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = "Mode Klik Kanan Aktif"
            )
        }
    }
}
