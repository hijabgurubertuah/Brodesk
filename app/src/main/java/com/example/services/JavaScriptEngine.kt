package com.example.services

import android.webkit.WebView

object JavaScriptEngine {

    fun injectDesktopViewport(webView: WebView, width: Int) {
        val js = """
            (function() {
                var meta = document.querySelector('meta[name="viewport"]');
                if (!meta) {
                    meta = document.createElement('meta');
                    meta.name = 'viewport';
                    document.head.appendChild(meta);
                }
                meta.content = 'width=$width, initial-scale=0.5, maximum-scale=3.0, user-scalable=yes';
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun dispatchMouseEvent(webView: WebView, eventType: String, clientX: Float, clientY: Float) {
        val js = """
            (function() {
                var x = $clientX;
                var y = $clientY;
                var el = document.elementFromPoint(x, y) || document.body;
                if (!el) return;

                var evt = new MouseEvent('$eventType', {
                    bubbles: true,
                    cancelable: true,
                    view: window,
                    detail: 1,
                    screenX: x,
                    screenY: y,
                    clientX: x,
                    clientY: y,
                    ctrlKey: false,
                    altKey: false,
                    shiftKey: false,
                    metaKey: false,
                    button: ${if (eventType == "contextmenu") 2 else 0},
                    buttons: ${if (eventType == "mousedown") 1 else 0},
                    relatedTarget: null
                });
                el.dispatchEvent(evt);

                if ('$eventType' === 'click') {
                    if (typeof el.focus === 'function') el.focus();
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun dispatchKeyboardEvent(
        webView: WebView,
        key: String,
        code: String,
        ctrlKey: Boolean = false,
        altKey: Boolean = false,
        shiftKey: Boolean = false
    ) {
        val js = """
            (function() {
                var active = document.activeElement || document.body;
                
                // Special handling for shortcuts
                if (${ctrlKey && key.lowercase() == "a"}) {
                    if (active && (active.tagName === 'INPUT' || active.tagName === 'TEXTAREA')) {
                        active.select();
                    } else {
                        window.getSelection().selectAllChildren(document.body);
                    }
                    return;
                }
                if (${ctrlKey && key.lowercase() == "c"}) {
                    document.execCommand('copy');
                    return;
                }
                if (${ctrlKey && key.lowercase() == "v"}) {
                    document.execCommand('paste');
                    return;
                }
                if (${ctrlKey && key.lowercase() == "x"}) {
                    document.execCommand('cut');
                    return;
                }
                if (${ctrlKey && key.lowercase() == "z"}) {
                    document.execCommand('undo');
                    return;
                }
                if ('$key' === 'F5') {
                    window.location.reload();
                    return;
                }

                var down = new KeyboardEvent('keydown', {
                    key: '$key',
                    code: '$code',
                    bubbles: true,
                    cancelable: true,
                    ctrlKey: $ctrlKey,
                    altKey: $altKey,
                    shiftKey: $shiftKey
                });
                var up = new KeyboardEvent('keyup', {
                    key: '$key',
                    code: '$code',
                    bubbles: true,
                    cancelable: true,
                    ctrlKey: $ctrlKey,
                    altKey: $altKey,
                    shiftKey: $shiftKey
                });
                
                active.dispatchEvent(down);
                active.dispatchEvent(up);
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }
}
