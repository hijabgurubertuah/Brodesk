package com.example.ui.components

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("virtual_touchpad_overlay")
            .pointerInput(sensitivity, scrollSpeed, isScreenLocked, longPressDurationMs) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val changes = event.changes

                        if (changes.size == 1) {
                            val change = changes[0]
                            if (change.pressed && !change.previousPressed) {
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
    ) {
        // Draw virtual cursor arrow (crisp white arrow with dark border)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawVirtualCursor(
                x = cursorX,
                y = cursorY,
                scale = cursorScale,
                isDown = isMouseDown
            )
        }

        // Floating Screen Lock Button (Kunci Layar agar layar tidak terswipe)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isScreenLocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .clickable {
                    isScreenLocked = !isScreenLocked
                    onVibrateShort()
                }
                .testTag("touchpad_screen_lock_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (isScreenLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = if (isScreenLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isScreenLocked) "Layar Terkunci" else "Gulir Aktif",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isScreenLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
