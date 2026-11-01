package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.services.JavaScriptEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun TouchpadOverlay(
    isEnabled: Boolean,
    webView: WebView?,
    sensitivity: Float,
    scrollSpeed: Float,
    cursorSizeStr: String,
    longPressDurationMs: Long,
    onVibrateShort: () -> Unit,
    onVibrateMedium: () -> Unit
) {
    if (!isEnabled) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val clipboardManager = remember(context) {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    val scope = rememberCoroutineScope()
    var cursorX by remember { mutableFloatStateOf(400f) }
    var cursorY by remember { mutableFloatStateOf(600f) }

    var isMouseDown by remember { mutableStateOf(false) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var longPressJob by remember { mutableStateOf<Job?>(null) }
    var touchDownTime by remember { mutableLongStateOf(0L) }
    var hasMovedSinceDown by remember { mutableStateOf(false) }

    // Screen Lock feature: when locked, page CANNOT swipe or scroll at all
    var isScreenLocked by remember { mutableStateOf(true) }

    val cursorScale = when (cursorSizeStr) {
        "Kecil" -> 0.8f
        "Besar" -> 1.4f
        else -> 1.0f
    }

    val toolbarWidthPx = with(density) { 340.dp.toPx() }
    val toolbarHeightPx = with(density) { 64.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("virtual_touchpad_overlay")
    ) {
        // 1. Touchpad gesture detection layer
        var isInteractingWithToolbar by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(sensitivity, scrollSpeed, isScreenLocked, longPressDurationMs) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val changes = event.changes

                            if (changes.size == 1) {
                                val change = changes[0]
                                val inToolbarArea = change.position.x >= (size.width - toolbarWidthPx) && change.position.y <= toolbarHeightPx

                                if (change.pressed && !change.previousPressed) {
                                    if (inToolbarArea) {
                                        isInteractingWithToolbar = true
                                        continue
                                    }
                                    isInteractingWithToolbar = false

                                    // Touch Down with 1 finger
                                    touchDownTime = System.currentTimeMillis()
                                    hasMovedSinceDown = false

                                    longPressJob?.cancel()
                                    longPressJob = scope.launch {
                                        delay(longPressDurationMs)
                                        if (!hasMovedSinceDown) {
                                            // Trigger Desktop Right Click without reloading or popping modal
                                            onVibrateMedium()
                                            isMouseDown = true
                                            webView?.let { wv ->
                                                JavaScriptEngine.dispatchMouseEvent(wv, "mousedown", cursorX, cursorY)
                                                JavaScriptEngine.dispatchMouseEvent(wv, "contextmenu", cursorX, cursorY)
                                            }
                                        }
                                    }
                                } else if (change.pressed && change.previousPressed) {
                                    if (isInteractingWithToolbar) continue

                                    // Touch Move with 1 finger -> ONLY MOVE CURSOR, NEVER SWIPE PAGE!
                                    val delta = change.position - change.previousPosition
                                    if (abs(delta.x) > 1f || abs(delta.y) > 1f) {
                                        hasMovedSinceDown = true
                                        longPressJob?.cancel()

                                        cursorX = (cursorX + delta.x * sensitivity).coerceIn(0f, size.width.toFloat())
                                        cursorY = (cursorY + delta.y * sensitivity).coerceIn(0f, size.height.toFloat())

                                        // Edge scrolling ONLY if screen is NOT locked
                                        if (!isScreenLocked) {
                                            webView?.let { wv ->
                                                if (cursorY <= 20f) {
                                                    wv.scrollBy(0, -12)
                                                } else if (cursorY >= size.height - 20f) {
                                                    wv.scrollBy(0, 12)
                                                }
                                            }
                                        }

                                        webView?.let { wv ->
                                            JavaScriptEngine.dispatchMouseEvent(wv, "mousemove", cursorX, cursorY)
                                        }
                                    }
                                } else if (!change.pressed && change.previousPressed) {
                                    if (isInteractingWithToolbar) {
                                        isInteractingWithToolbar = false
                                        continue
                                    }

                                    // Touch Up with 1 finger
                                    longPressJob?.cancel()
                                    val pressDuration = System.currentTimeMillis() - touchDownTime

                                    if (!hasMovedSinceDown && pressDuration < longPressDurationMs) {
                                        val now = System.currentTimeMillis()
                                        if (now - lastTapTime < 300L) {
                                            // Double Click
                                            onVibrateShort()
                                            webView?.let { wv ->
                                                JavaScriptEngine.dispatchMouseEvent(wv, "dblclick", cursorX, cursorY)
                                            }
                                        } else {
                                            // Single Click
                                            onVibrateShort()
                                            webView?.let { wv ->
                                                JavaScriptEngine.dispatchMouseEvent(wv, "mousedown", cursorX, cursorY)
                                                JavaScriptEngine.dispatchMouseEvent(wv, "mouseup", cursorX, cursorY)
                                                JavaScriptEngine.dispatchMouseEvent(wv, "click", cursorX, cursorY)
                                            }
                                        }
                                        lastTapTime = now
                                    }

                                    if (isMouseDown) {
                                        isMouseDown = false
                                        webView?.let { wv ->
                                            JavaScriptEngine.dispatchMouseEvent(wv, "mouseup", cursorX, cursorY)
                                        }
                                    }
                                }
                                change.consume()
                            } else if (changes.size >= 2) {
                                // 2 or more fingers: page scrolling (only if screen is NOT locked)
                                longPressJob?.cancel()
                                if (!isScreenLocked && webView != null) {
                                    val change1 = changes[0]
                                    val change2 = changes[1]
                                    if (change1.pressed && change1.previousPressed && change2.pressed && change2.previousPressed) {
                                        val delta1 = change1.position - change1.previousPosition
                                        val delta2 = change2.position - change2.previousPosition
                                        val avgPanX = (delta1.x + delta2.x) / 2f
                                        val avgPanY = (delta1.y + delta2.y) / 2f

                                        webView.scrollBy(
                                            (-avgPanX * scrollSpeed).toInt(),
                                            (-avgPanY * scrollSpeed).toInt()
                                        )
                                    }
                                }
                                changes.forEach { it.consume() }
                            }
                        }
                    }
                }
        )

        // 2. Draw virtual cursor arrow (crisp white arrow with dark border)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawVirtualCursor(
                x = cursorX,
                y = cursorY,
                scale = cursorScale,
                isDown = isMouseDown
            )
        }

        // 3. Floating Mouse Action Toolbar (Select All, Copy, Paste, Screen Lock) - "Icon Saja"
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
                .testTag("mouse_action_toolbar")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                // Select All (Ctrl+A) - Icon Saja
                IconButton(
                    onClick = {
                        onVibrateShort()
                        webView?.let { wv ->
                            JavaScriptEngine.executeSelectAll(wv, cursorX, cursorY)
                        }
                        Toast.makeText(context, "Pilih Semua (Ctrl+A)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("mouse_action_select_all")
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = "Pilih Semua (Ctrl+A)",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Copy (Ctrl+C) - Icon Saja
                IconButton(
                    onClick = {
                        onVibrateShort()
                        webView?.let { wv ->
                            JavaScriptEngine.executeCopy(wv) { text ->
                                if (text.isNotBlank()) {
                                    val clip = ClipData.newPlainText("BroDesk", text)
                                    clipboardManager.setPrimaryClip(clip)
                                }
                            }
                        }
                        Toast.makeText(context, "Salin (Ctrl+C)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("mouse_action_copy")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Salin (Ctrl+C)",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Paste (Ctrl+V) - Icon Saja
                IconButton(
                    onClick = {
                        onVibrateShort()
                        val clipItem = clipboardManager.primaryClip?.getItemAt(0)
                        val textToPaste = clipItem?.text?.toString() ?: ""
                        webView?.let { wv ->
                            JavaScriptEngine.executePaste(wv, textToPaste, cursorX, cursorY)
                        }
                        Toast.makeText(
                            context,
                            if (textToPaste.isNotEmpty()) "Tempel (Ctrl+V)" else "Papan klip kosong",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("mouse_action_paste")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Tempel (Ctrl+V)",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Delete (Hapus seleksi / teks) - Icon Saja
                IconButton(
                    onClick = {
                        onVibrateShort()
                        webView?.let { wv ->
                            JavaScriptEngine.executeDelete(wv, cursorX, cursorY)
                        }
                        Toast.makeText(context, "Hapus (Delete)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("mouse_action_delete")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus (Delete)",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Subtle vertical divider between edit tools and zoom
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )

                // Zoom In (+) - Icon Saja
                IconButton(
                    onClick = {
                        onVibrateShort()
                        webView?.let { wv ->
                            JavaScriptEngine.zoomIn(wv)
                        }
                        Toast.makeText(context, "Perbesar (+)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("mouse_action_zoom_in")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Perbesar (+)",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Zoom Out (-) - Icon Saja
                IconButton(
                    onClick = {
                        onVibrateShort()
                        webView?.let { wv ->
                            JavaScriptEngine.zoomOut(wv)
                        }
                        Toast.makeText(context, "Perkecil (-)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("mouse_action_zoom_out")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Perkecil (-)",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Subtle vertical divider between zoom and lock
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )

                // Lock / Unlock Screen - Icon Saja
                IconButton(
                    onClick = {
                        isScreenLocked = !isScreenLocked
                        onVibrateShort()
                        Toast.makeText(
                            context,
                            if (isScreenLocked) "Layar Terkunci" else "Gulir Aktif",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("touchpad_screen_lock_button")
                ) {
                    Icon(
                        imageVector = if (isScreenLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (isScreenLocked) "Layar Terkunci" else "Gulir Aktif",
                        tint = if (isScreenLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawVirtualCursor(
    x: Float,
    y: Float,
    scale: Float,
    isDown: Boolean
) {
    val path = Path().apply {
        moveTo(x, y)
        lineTo(x, y + 24f * scale)
        lineTo(x + 6f * scale, y + 18f * scale)
        lineTo(x + 12f * scale, y + 28f * scale)
        lineTo(x + 15f * scale, y + 26f * scale)
        lineTo(x + 9f * scale, y + 16f * scale)
        lineTo(x + 18f * scale, y + 16f * scale)
        close()
    }

    // Outer border
    drawPath(
        path = path,
        color = Color(0xFF0F172A),
        style = Stroke(width = 3f * scale)
    )

    // Inner fill
    drawPath(
        path = path,
        color = if (isDown) Color(0xFFF5A524) else Color.White
    )
}
