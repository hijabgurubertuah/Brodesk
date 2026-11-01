package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DesktopKeyboardBar(
    isVisible: Boolean,
    onKeyPress: (key: String, code: String, ctrl: Boolean, alt: Boolean, shift: Boolean) -> Unit
) {
    if (!isVisible) return

    var isCtrlActive by remember { mutableStateOf(false) }
    var isAltActive by remember { mutableStateOf(false) }
    var isShiftActive by remember { mutableStateOf(false) }

    fun sendKey(key: String, code: String) {
        onKeyPress(key, code, isCtrlActive, isAltActive, isShiftActive)
        // Reset sticky modifiers after key press
        isCtrlActive = false
        isAltActive = false
        isShiftActive = false
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("desktop_keyboard_bar")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Sticky Modifiers
            KeyChip(
                label = "Ctrl",
                isActive = isCtrlActive,
                onClick = { isCtrlActive = !isCtrlActive },
                tag = "key_ctrl"
            )
            KeyChip(
                label = "Alt",
                isActive = isAltActive,
                onClick = { isAltActive = !isAltActive },
                tag = "key_alt"
            )
            KeyChip(
                label = "Shift",
                isActive = isShiftActive,
                onClick = { isShiftActive = !isShiftActive },
                tag = "key_shift"
            )

            // Keys
            KeyChip(label = "Esc", onClick = { sendKey("Escape", "Escape") }, tag = "key_esc")
            KeyChip(label = "Tab", onClick = { sendKey("Tab", "Tab") }, tag = "key_tab")
            KeyChip(label = "F5", onClick = { sendKey("F5", "F5") }, tag = "key_f5")

            // Directional Arrows
            KeyChip(label = "←", onClick = { sendKey("ArrowLeft", "ArrowLeft") }, tag = "key_left")
            KeyChip(label = "↑", onClick = { sendKey("ArrowUp", "ArrowUp") }, tag = "key_up")
            KeyChip(label = "↓", onClick = { sendKey("ArrowDown", "ArrowDown") }, tag = "key_down")
            KeyChip(label = "→", onClick = { sendKey("ArrowRight", "ArrowRight") }, tag = "key_right")

            // Desktop Shortcuts
            KeyChip(label = "Ctrl+C", onClick = { onKeyPress("c", "KeyC", true, false, false) }, tag = "key_ctrl_c")
            KeyChip(label = "Ctrl+V", onClick = { onKeyPress("v", "KeyV", true, false, false) }, tag = "key_ctrl_v")
            KeyChip(label = "Ctrl+X", onClick = { onKeyPress("x", "KeyX", true, false, false) }, tag = "key_ctrl_x")
            KeyChip(label = "Ctrl+A", onClick = { onKeyPress("a", "KeyA", true, false, false) }, tag = "key_ctrl_a")
            KeyChip(label = "Ctrl+Z", onClick = { onKeyPress("z", "KeyZ", true, false, false) }, tag = "key_ctrl_z")
        }
    }
}

@Composable
private fun KeyChip(
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit,
    tag: String
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = ButtonDefaults.ContentPadding,
        modifier = Modifier
            .widthIn(min = 44.dp)
            .testTag(tag)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
