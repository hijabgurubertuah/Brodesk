package com.example.ui.components

import android.webkit.WebView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
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
    onLongPressRightClick: (Float, Float) -> Unit,
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

    val cursorScale = when (cursorSizeStr) {
        "Kecil" -> 0.8f
        "Besar" -> 1.4f
        else -> 1.0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("virtual_touchpad_overlay")
            .pointerInput(sensitivity, scrollSpeed, longPressDurationMs) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Handle 2-finger scroll or pinch zoom
                    if (zoom != 1.0f && webView != null) {
                        val newScale = (webView.scale * zoom).coerceIn(0.2f, 4.0f)
                        webView.zoomBy(zoom)
                    } else if (pan != Offset.Zero && webView != null) {
                        webView.scrollBy(
                            (-pan.x * scrollSpeed).toInt(),
                            (-pan.y * scrollSpeed).toInt()
                        )
                    }
                }
            }
            .pointerInput(sensitivity, longPressDurationMs) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val changes = event.changes

                        if (changes.size == 1) {
                            val change = changes.first()
                            if (change.pressed && !change.previousPressed) {
                                // Touch Down
                                touchDownTime = System.currentTimeMillis()
                                hasMovedSinceDown = false

                                // Cancel previous long press job
                                longPressJob?.cancel()
                                longPressJob = scope.launch {
                                    delay(longPressDurationMs)
                                    if (!hasMovedSinceDown) {
                                        // Trigger Right Click or Mouse Drag Start
                                        onVibrateMedium()
                                        isMouseDown = true
                                        webView?.let {
                                            JavaScriptEngine.dispatchMouseEvent(it, "mousedown", cursorX, cursorY)
                                            JavaScriptEngine.dispatchMouseEvent(it, "contextmenu", cursorX, cursorY)
                                        }
                                        onLongPressRightClick(cursorX, cursorY)
                                    }
                                }
                            } else if (change.pressed && change.previousPressed) {
                                // Touch Move - relative movement
                                val delta = change.position - change.previousPosition
                                if (abs(delta.x) > 1f || abs(delta.y) > 1f) {
                                    hasMovedSinceDown = true
                                    longPressJob?.cancel()

                                    cursorX = (cursorX + delta.x * sensitivity).coerceIn(0f, size.width.toFloat())
                                    cursorY = (cursorY + delta.y * sensitivity).coerceIn(0f, size.height.toFloat())

                                    // Edge scrolling detection
                                    webView?.let { wv ->
                                        if (cursorY <= 20f) {
                                            wv.scrollBy(0, -15)
                                        } else if (cursorY >= size.height - 20f) {
                                            wv.scrollBy(0, 15)
                                        }
                                        JavaScriptEngine.dispatchMouseEvent(wv, "mousemove", cursorX, cursorY)
                                    }
                                }
                            } else if (!change.pressed && change.previousPressed) {
                                // Touch Up
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
                        }
                    }
                }
            }
    ) {
        // Draw virtual cursor arrow (white arrow with dark border)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawVirtualCursor(
                x = cursorX,
                y = cursorY,
                scale = cursorScale,
                isDown = isMouseDown
            )
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
