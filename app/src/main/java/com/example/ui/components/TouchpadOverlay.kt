package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
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
    onVibrateMedium: () -> Unit,
    onTriggerRightClick: (() -> Unit)? = null
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
    var touchDownPos by remember { mutableStateOf(Offset.Zero) }
    var hasMovedSinceDown by remember { mutableStateOf(false) }

    val touchSlopPx = with(density) { 16.dp.toPx() }

    // Screen Lock feature: when locked, page CANNOT swipe or scroll at all
    var isScreenLocked by remember { mutableStateOf(true) }

    val cursorScale = when (cursorSizeStr) {
        "Kecil" -> 0.8f
        "Besar" -> 1.4f
        else -> 1.0f
    }

    val toolbarWidthPx = with(density) { 340.dp.toPx() }
    val toolbarHeightPx = with(density) { 64.dp.toPx() }

    var isLeftPressed by remember { mutableStateOf(false) }
    var isRightPressed by remember { mutableStateOf(false) }

    fun triggerLeftClick() {
        onVibrateShort()
        isLeftPressed = true
        scope.launch {
            delay(200)
            isLeftPressed = false
        }
        webView?.let { wv ->
            wv.post {
                wv.requestFocus()
                wv.requestFocusFromTouch()
                val now = SystemClock.uptimeMillis()
                val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, cursorX, cursorY, 0).apply {
                    source = InputDevice.SOURCE_TOUCHSCREEN
                }
                val up = MotionEvent.obtain(now, now + 35, MotionEvent.ACTION_UP, cursorX, cursorY, 0).apply {
                    source = InputDevice.SOURCE_TOUCHSCREEN
                }
                wv.dispatchTouchEvent(down)
                wv.dispatchTouchEvent(up)
                down.recycle()
                up.recycle()
            }
            JavaScriptEngine.dispatchMouseClick(wv, cursorX, cursorY)
        }
    }

    fun triggerRightClick() {
        onVibrateMedium()
        isMouseDown = true
        isRightPressed = true
        scope.launch {
            delay(200)
            isRightPressed = false
        }
        webView?.let { wv ->
            wv.post {
                wv.requestFocus()
                wv.requestFocusFromTouch()
                JavaScriptEngine.dispatchContextMenu(wv, cursorX, cursorY)
                onTriggerRightClick?.invoke()
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("virtual_touchpad_overlay")
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()

        val btnDiameterDp = 34.dp
        val btnDiameterPx = with(density) { btnDiameterDp.toPx() }

        // Circular button positions that travel with the cursor:
        // Left button: directly to the left of the cursor tip
        val leftBtnXPx = (cursorX - btnDiameterPx - with(density) { 10.dp.toPx() }).coerceIn(
            with(density) { 4.dp.toPx() },
            screenWidthPx - btnDiameterPx - with(density) { 4.dp.toPx() }
        )
        // Right button: directly to the right of the cursor arrow
        val rightBtnXPx = (cursorX + with(density) { 26.dp.toPx() }).coerceIn(
            with(density) { 4.dp.toPx() },
            screenWidthPx - btnDiameterPx - with(density) { 4.dp.toPx() }
        )
        val cursorBtnYPx = (cursorY - with(density) { 8.dp.toPx() }).coerceIn(
            with(density) { 4.dp.toPx() },
            screenHeightPx - btnDiameterPx - with(density) { 4.dp.toPx() }
        )

        var isInteractingWithToolbar by remember { mutableStateOf(false) }
        var isInteractingWithCursorButton by remember { mutableStateOf(false) }

        // 1. Touchpad gesture detection layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(sensitivity, scrollSpeed, isScreenLocked, longPressDurationMs, cursorX, cursorY) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val changes = event.changes

                            if (changes.size == 1) {
                                val change = changes[0]
                                val pos = change.position

                                val inToolbarArea = pos.x >= (size.width - toolbarWidthPx) && pos.y <= toolbarHeightPx
                                val inLeftBtn = pos.x in leftBtnXPx..(leftBtnXPx + btnDiameterPx) &&
                                                pos.y in cursorBtnYPx..(cursorBtnYPx + btnDiameterPx)
                                val inRightBtn = pos.x in rightBtnXPx..(rightBtnXPx + btnDiameterPx) &&
                                                 pos.y in cursorBtnYPx..(cursorBtnYPx + btnDiameterPx)

                                if (change.pressed && !change.previousPressed) {
                                    if (inToolbarArea) {
                                        isInteractingWithToolbar = true
                                        continue
                                    }
                                    if (inLeftBtn) {
                                        isInteractingWithCursorButton = true
                                        triggerLeftClick()
                                        change.consume()
                                        continue
                                    }
                                    if (inRightBtn) {
                                        isInteractingWithCursorButton = true
                                        triggerRightClick()
                                        change.consume()
                                        continue
                                    }

                                    isInteractingWithToolbar = false
                                    isInteractingWithCursorButton = false

                                    // Touch Down on touchpad
                                    touchDownTime = System.currentTimeMillis()
                                    touchDownPos = change.position
                                    hasMovedSinceDown = false

                                    longPressJob?.cancel()
                                    longPressJob = scope.launch {
                                        delay(longPressDurationMs)
                                        if (!hasMovedSinceDown) {
                                            // Trigger Desktop Right Click on long press
                                            triggerRightClick()
                                        }
                                    }
                                } else if (change.pressed && change.previousPressed) {
                                    if (isInteractingWithToolbar || isInteractingWithCursorButton) continue

                                    // Check if user has moved past the touch slop
                                    val distFromDown = (change.position - touchDownPos).getDistance()
                                    if (distFromDown > touchSlopPx) {
                                        hasMovedSinceDown = true
                                        longPressJob?.cancel()
                                    }

                                    // Touch Move with 1 finger -> ONLY MOVE CURSOR, NEVER SWIPE PAGE!
                                    val delta = change.position - change.previousPosition
                                    if (abs(delta.x) > 0.1f || abs(delta.y) > 0.1f) {
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
                                    if (isInteractingWithCursorButton) {
                                        isInteractingWithCursorButton = false
                                        change.consume()
                                        continue
                                    }

                                    // Touch Up with 1 finger on touchpad
                                    longPressJob?.cancel()
                                    val pressDuration = System.currentTimeMillis() - touchDownTime

                                    if (!hasMovedSinceDown && pressDuration < longPressDurationMs) {
                                        val now = System.currentTimeMillis()
                                        if (now - lastTapTime < 300L) {
                                            // Double Click
                                            triggerLeftClick()
                                            triggerLeftClick()
                                            webView?.let { wv ->
                                                JavaScriptEngine.dispatchMouseEvent(wv, "dblclick", cursorX, cursorY)
                                            }
                                        } else {
                                            // Single Tap on Touchpad -> Left Click
                                            triggerLeftClick()
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

        // 3. Traveling Left Button (Kiri: Bulat Murni, Tanpa Tulisan, Berubah Warna Saat Ditekan)
        Surface(
            shape = CircleShape,
            color = if (isLeftPressed) Color(0xFF38BDF8) else Color(0xFF0F172A).copy(alpha = 0.92f),
            border = BorderStroke(
                width = 2.dp,
                color = if (isLeftPressed) Color.White else Color(0xFF38BDF8)
            ),
            shadowElevation = if (isLeftPressed) 8.dp else 4.dp,
            modifier = Modifier
                .offset { IntOffset(leftBtnXPx.toInt(), cursorBtnYPx.toInt()) }
                .size(btnDiameterDp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 18.dp, color = Color(0xFF38BDF8))
                ) {
                    triggerLeftClick()
                }
                .testTag("cursor_button_left_click")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isLeftPressed) 12.dp else 10.dp)
                        .background(
                            color = if (isLeftPressed) Color.White else Color(0xFF38BDF8),
                            shape = CircleShape
                        )
                )
            }
        }

        // 4. Traveling Right Button (Kanan: Bulat Murni, Tanpa Tulisan, Berubah Warna Saat Ditekan)
        Surface(
            shape = CircleShape,
            color = if (isRightPressed) Color(0xFFF5A524) else Color(0xFF0F172A).copy(alpha = 0.92f),
            border = BorderStroke(
                width = 2.dp,
                color = if (isRightPressed) Color.White else Color(0xFFF5A524)
            ),
            shadowElevation = if (isRightPressed) 8.dp else 4.dp,
            modifier = Modifier
                .offset { IntOffset(rightBtnXPx.toInt(), cursorBtnYPx.toInt()) }
                .size(btnDiameterDp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 18.dp, color = Color(0xFFF5A524))
                ) {
                    triggerRightClick()
                }
                .testTag("cursor_button_right_click")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isRightPressed) 12.dp else 10.dp)
                        .background(
                            color = if (isRightPressed) Color.White else Color(0xFFF5A524),
                            shape = CircleShape
                        )
                )
            }
        }

        // 5. Floating Mouse Action Toolbar (Select All, Copy, Paste, Delete, Zoom In, Zoom Out, Screen Lock)
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
                // Select All (Ctrl+A)
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

                // Copy (Ctrl+C)
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

                // Paste (Ctrl+V)
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

                // Delete (Hapus seleksi / teks)
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

                // Zoom In (+)
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

                // Zoom Out (-)
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

                // Lock / Unlock Screen
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
