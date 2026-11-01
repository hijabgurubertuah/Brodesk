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
                    (document.head || document.documentElement).appendChild(meta);
                }
                meta.content = 'width=$width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes';

                // Prevent mobile auto-zoom on inputs & Monaco code editor in Google Apps Script
                var styleId = 'brodesk-prevent-autozoom-css';
                if (!document.getElementById(styleId)) {
                    var style = document.createElement('style');
                    style.id = styleId;
                    style.textContent = 'input, textarea, select, [contenteditable], .monaco-editor textarea, .inputarea, .monaco-mouse-cursor-text { font-size: 16px !important; touch-action: manipulation !important; } html, body { touch-action: pan-x pan-y !important; -webkit-text-size-adjust: 100% !important; text-size-adjust: 100% !important; }';
                    (document.head || document.documentElement).appendChild(style);
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun disableAutoZoomOnInputs(webView: WebView) {
        val js = """
            (function() {
                var styleId = 'brodesk-prevent-autozoom-css';
                if (!document.getElementById(styleId)) {
                    var style = document.createElement('style');
                    style.id = styleId;
                    style.textContent = 'input, textarea, select, [contenteditable], .monaco-editor textarea, .inputarea, .monaco-mouse-cursor-text { font-size: 16px !important; touch-action: manipulation !important; } html, body { touch-action: pan-x pan-y !important; -webkit-text-size-adjust: 100% !important; text-size-adjust: 100% !important; }';
                    (document.head || document.documentElement).appendChild(style);
                }

                if (!window.__brodeskFocusZoomGuarded) {
                    window.__brodeskFocusZoomGuarded = true;
                    window.addEventListener('focusin', function(e) {
                        var meta = document.querySelector('meta[name="viewport"]');
                        if (meta) {
                            var content = meta.getAttribute('content') || '';
                            if (!content.includes('maximum-scale')) {
                                meta.setAttribute('content', content + ', maximum-scale=5.0');
                            }
                        }
                    }, true);
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun dispatchContextMenu(webView: WebView, clientX: Float, clientY: Float) {
        val js = """
            (function() {
                var dpr = window.devicePixelRatio || 1;
                var x = $clientX / dpr;
                var y = $clientY / dpr;
                var el = document.elementFromPoint(x, y) || document.activeElement || document.body;
                if (!el) return;

                if (typeof el.focus === 'function') {
                    try { el.focus(); } catch(e){}
                }

                var init = {
                    bubbles: true,
                    cancelable: true,
                    view: window,
                    detail: 1,
                    screenX: x,
                    screenY: y,
                    clientX: x,
                    clientY: y,
                    button: 2,
                    buttons: 2,
                    which: 3,
                    pointerId: 1,
                    pointerType: 'mouse',
                    isPrimary: true
                };

                try { el.dispatchEvent(new PointerEvent('pointerdown', init)); } catch(e){}
                try { el.dispatchEvent(new MouseEvent('mousedown', init)); } catch(e){}
                try { el.dispatchEvent(new PointerEvent('contextmenu', init)); } catch(e){}
                try { el.dispatchEvent(new MouseEvent('contextmenu', init)); } catch(e){}
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun dispatchMouseClick(webView: WebView, clientX: Float, clientY: Float) {
        val js = """
            (function() {
                var dpr = window.devicePixelRatio || 1;
                var x = $clientX / dpr;
                var y = $clientY / dpr;
                var el = document.elementFromPoint(x, y) || document.body;
                if (!el) return;

                function isElementVisible(elem) {
                    if (!elem) return false;
                    var style = window.getComputedStyle(elem);
                    return style.display !== 'none' && style.visibility !== 'hidden' && style.opacity !== '0' && (elem.offsetWidth > 0 || elem.offsetHeight > 0 || elem.getClientRects().length > 0);
                }

                // Check for ACTUALLY VISIBLE context menus or dropdowns (Monaco, web apps, standard menus)
                var candidateMenus = document.querySelectorAll('.monaco-menu-container, .context-view, [role="menu"], .dropdown-menu, .context-menu');
                var visibleMenus = [];
                for (var i = 0; i < candidateMenus.length; i++) {
                    if (isElementVisible(candidateMenus[i])) {
                        visibleMenus.push(candidateMenus[i]);
                    }
                }

                var isInsideMenu = false;
                if (visibleMenus.length > 0) {
                    for (var j = 0; j < visibleMenus.length; j++) {
                        if (visibleMenus[j].contains(el)) {
                            isInsideMenu = true;
                            break;
                        }
                    }
                }

                // If a menu is VISIBLE and tap is OUTSIDE the menu, dismiss it immediately
                if (visibleMenus.length > 0 && !isInsideMenu) {
                    var dismissMdown = new MouseEvent('mousedown', {
                        bubbles: true, cancelable: true, view: window, clientX: x, clientY: y, button: 0, buttons: 1
                    });
                    document.body.dispatchEvent(dismissMdown);
                    document.dispatchEvent(dismissMdown);

                    var dismissClick = new MouseEvent('click', {
                        bubbles: true, cancelable: true, view: window, clientX: x, clientY: y, button: 0, buttons: 1
                    });
                    document.body.dispatchEvent(dismissClick);
                    document.dispatchEvent(dismissClick);

                    var escEvt = new KeyboardEvent('keydown', {
                        key: 'Escape', code: 'Escape', keyCode: 27, which: 27, bubbles: true, cancelable: true
                    });
                    document.dispatchEvent(escEvt);
                    return;
                }

                var init = {
                    bubbles: true,
                    cancelable: true,
                    view: window,
                    detail: 1,
                    screenX: x,
                    screenY: y,
                    clientX: x,
                    clientY: y,
                    button: 0,
                    buttons: 1,
                    which: 1,
                    pointerId: 1,
                    pointerType: 'mouse',
                    isPrimary: true
                };

                try { el.dispatchEvent(new PointerEvent('pointerdown', init)); } catch(e){}
                try { el.dispatchEvent(new MouseEvent('mousedown', init)); } catch(e){}
                try { el.dispatchEvent(new PointerEvent('pointerup', init)); } catch(e){}
                try { el.dispatchEvent(new MouseEvent('mouseup', init)); } catch(e){}
                try { el.dispatchEvent(new MouseEvent('click', init)); } catch(e){}

                if (typeof el.focus === 'function') {
                    try { el.focus(); } catch(e){}
                }

                // Trigger click on closest clickable target (menuitem, button, link, action-item)
                var clickable = el.closest('[role="menuitem"], .action-item, .action-label, button, a, input, select, textarea, [role="button"]');
                if (clickable && typeof clickable.click === 'function') {
                    clickable.click();
                } else if (typeof el.click === 'function') {
                    el.click();
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun dispatchMouseEvent(webView: WebView, eventType: String, clientX: Float, clientY: Float) {
        val js = """
            (function() {
                var dpr = window.devicePixelRatio || 1;
                var x = $clientX / dpr;
                var y = $clientY / dpr;
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
                    buttons: ${if (eventType == "mousedown") 1 else if (eventType == "contextmenu") 2 else 0},
                    relatedTarget: null
                });
                el.dispatchEvent(evt);

                if ('$eventType' === 'click') {
                    if (typeof el.focus === 'function') el.focus();
                    var clickable = el.closest('[role="menuitem"], .action-item, a, button, [role="button"]');
                    if (clickable && typeof clickable.click === 'function') {
                        clickable.click();
                    } else if (typeof el.click === 'function') {
                        el.click();
                    }
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun executeSelectAll(webView: WebView, clientX: Float = 0f, clientY: Float = 0f) {
        val js = """
            (function() {
                function findMonaco(win) {
                    try {
                        if (win.monaco && win.monaco.editor) {
                            var eds = win.monaco.editor.getEditors();
                            if (eds && eds.length > 0) {
                                var ed = eds.find(function(e) { return e.hasTextFocus(); }) || eds[0];
                                return { win: win, ed: ed };
                            }
                        }
                        for (var i = 0; i < win.frames.length; i++) {
                            var res = findMonaco(win.frames[i]);
                            if (res) return res;
                        }
                    } catch(e) {}
                    return null;
                }

                // 1. Monaco Editor (Google Apps Script / VS Code Web)
                var m = findMonaco(window);
                if (m && m.ed && m.ed.getModel()) {
                    var range = m.ed.getModel().getFullModelRange();
                    m.ed.setSelection(range);
                    m.ed.focus();
                    return;
                }

                // 2. Focused or Point element
                var cx = $clientX;
                var cy = $clientY;
                var pointEl = (cx > 0 && cy > 0) ? document.elementFromPoint(cx, cy) : null;
                var target = document.activeElement || pointEl || document.body;

                var monacoInput = document.querySelector('textarea.inputarea');
                if (monacoInput) {
                    monacoInput.focus();
                    target = monacoInput;
                }

                // 3. Dispatch Ctrl+A KeyEvents
                var down = new KeyboardEvent('keydown', {
                    key: 'a',
                    code: 'KeyA',
                    keyCode: 65,
                    which: 65,
                    ctrlKey: true,
                    metaKey: true,
                    bubbles: true,
                    cancelable: true
                });
                var press = new KeyboardEvent('keypress', {
                    key: 'a',
                    code: 'KeyA',
                    keyCode: 65,
                    which: 65,
                    ctrlKey: true,
                    metaKey: true,
                    bubbles: true,
                    cancelable: true
                });
                var up = new KeyboardEvent('keyup', {
                    key: 'a',
                    code: 'KeyA',
                    keyCode: 65,
                    which: 65,
                    ctrlKey: true,
                    metaKey: true,
                    bubbles: true,
                    cancelable: true
                });

                target.dispatchEvent(down);
                target.dispatchEvent(press);
                target.dispatchEvent(up);

                // 4. Input / Textarea select()
                if (target && typeof target.select === 'function') {
                    target.select();
                    return;
                }

                // 5. execCommand selectAll
                try {
                    document.execCommand('selectAll', false, null);
                } catch(e) {}

                // 6. Selection API
                try {
                    if (window.getSelection) {
                        var sel = window.getSelection();
                        var range = document.createRange();
                        range.selectNodeContents(target);
                        sel.removeAllRanges();
                        sel.addRange(range);
                    }
                } catch(e) {}
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun executeCopy(webView: WebView, onCopied: ((String) -> Unit)? = null) {
        val js = """
            (function() {
                function findMonaco(win) {
                    try {
                        if (win.monaco && win.monaco.editor) {
                            var eds = win.monaco.editor.getEditors();
                            if (eds && eds.length > 0) {
                                var ed = eds.find(function(e) { return e.hasTextFocus(); }) || eds[0];
                                return { win: win, ed: ed };
                            }
                        }
                        for (var i = 0; i < win.frames.length; i++) {
                            var res = findMonaco(win.frames[i]);
                            if (res) return res;
                        }
                    } catch(e) {}
                    return null;
                }

                var text = '';
                var m = findMonaco(window);
                if (m && m.ed && m.ed.getModel()) {
                    var sel = m.ed.getSelection();
                    text = m.ed.getModel().getValueInRange(sel);
                }

                var target = document.activeElement || document.body;
                if (!text && window.getSelection) {
                    text = window.getSelection().toString();
                }
                if (!text && target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA')) {
                    var start = target.selectionStart || 0;
                    var end = target.selectionEnd || 0;
                    text = (target.value || '').substring(start, end);
                }

                var down = new KeyboardEvent('keydown', {
                    key: 'c',
                    code: 'KeyC',
                    keyCode: 67,
                    which: 67,
                    ctrlKey: true,
                    bubbles: true,
                    cancelable: true
                });
                target.dispatchEvent(down);
                try {
                    document.execCommand('copy');
                } catch(e) {}
                var up = new KeyboardEvent('keyup', {
                    key: 'c',
                    code: 'KeyC',
                    keyCode: 67,
                    which: 67,
                    ctrlKey: true,
                    bubbles: true,
                    cancelable: true
                });
                target.dispatchEvent(up);

                return text;
            })();
        """.trimIndent()

        webView.evaluateJavascript(js) { result ->
            var extracted = result
            if (extracted != null && extracted.startsWith("\"") && extracted.endsWith("\"") && extracted.length >= 2) {
                extracted = try {
                    org.json.JSONTokener(extracted).nextValue().toString()
                } catch (e: Exception) {
                    extracted.substring(1, extracted.length - 1)
                }
            }
            if (!extracted.isNullOrBlank() && extracted != "null") {
                onCopied?.invoke(extracted)
            }
        }
    }

    fun executePaste(webView: WebView, textToPaste: String, clientX: Float = 0f, clientY: Float = 0f) {
        val escapedText = org.json.JSONObject.quote(textToPaste)
        val js = """
            (function() {
                var text = $escapedText;
                function findMonaco(win) {
                    try {
                        if (win.monaco && win.monaco.editor) {
                            var eds = win.monaco.editor.getEditors();
                            if (eds && eds.length > 0) {
                                var ed = eds.find(function(e) { return e.hasTextFocus(); }) || eds[0];
                                return { win: win, ed: ed };
                            }
                        }
                        for (var i = 0; i < win.frames.length; i++) {
                            var res = findMonaco(win.frames[i]);
                            if (res) return res;
                        }
                    } catch(e) {}
                    return null;
                }

                // 1. Monaco Editor (Google Apps Script)
                var m = findMonaco(window);
                if (m && m.ed) {
                    var sel = m.ed.getSelection();
                    m.ed.executeEdits('brodesk-paste', [{
                        range: sel,
                        text: text,
                        forceMoveMarkers: true
                    }]);
                    m.ed.focus();
                    return;
                }

                var cx = $clientX;
                var cy = $clientY;
                var pointEl = (cx > 0 && cy > 0) ? document.elementFromPoint(cx, cy) : null;
                var target = document.activeElement || pointEl || document.body;

                // 2. Input / Textarea
                if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA')) {
                    var start = target.selectionStart || 0;
                    var end = target.selectionEnd || 0;
                    var val = target.value || '';
                    target.value = val.substring(0, start) + text + val.substring(end);
                    target.selectionStart = target.selectionEnd = start + text.length;
                    target.dispatchEvent(new Event('input', { bubbles: true }));
                    target.dispatchEvent(new Event('change', { bubbles: true }));
                    return;
                }

                // 3. ContentEditable
                if (target && target.isContentEditable) {
                    document.execCommand('insertText', false, text);
                    return;
                }

                // 4. ExecCommand insertText
                var inserted = false;
                try {
                    inserted = document.execCommand('insertText', false, text);
                } catch(e) {}

                if (!inserted) {
                    try {
                        document.execCommand('paste');
                    } catch(e) {}
                }

                // 5. Dispatch KeyboardEvent
                var down = new KeyboardEvent('keydown', {
                    key: 'v',
                    code: 'KeyV',
                    keyCode: 86,
                    which: 86,
                    ctrlKey: true,
                    bubbles: true,
                    cancelable: true
                });
                target.dispatchEvent(down);
                target.dispatchEvent(new KeyboardEvent('keyup', {
                    key: 'v',
                    code: 'KeyV',
                    keyCode: 86,
                    which: 86,
                    ctrlKey: true,
                    bubbles: true,
                    cancelable: true
                }));
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun executeDelete(webView: WebView, clientX: Float = 0f, clientY: Float = 0f) {
        val js = """
            (function() {
                function findMonaco(win) {
                    try {
                        if (win.monaco && win.monaco.editor) {
                            var eds = win.monaco.editor.getEditors();
                            if (eds && eds.length > 0) {
                                var ed = eds.find(function(e) { return e.hasTextFocus(); }) || eds[0];
                                return { win: win, ed: ed };
                            }
                        }
                        for (var i = 0; i < win.frames.length; i++) {
                            var res = findMonaco(win.frames[i]);
                            if (res) return res;
                        }
                    } catch(e) {}
                    return null;
                }

                // 1. Monaco Editor (Google Apps Script)
                var m = findMonaco(window);
                if (m && m.ed) {
                    var sel = m.ed.getSelection();
                    m.ed.executeEdits('brodesk-delete', [{
                        range: sel,
                        text: '',
                        forceMoveMarkers: true
                    }]);
                    m.ed.focus();
                    return;
                }

                var cx = $clientX;
                var cy = $clientY;
                var pointEl = (cx > 0 && cy > 0) ? document.elementFromPoint(cx, cy) : null;
                var target = document.activeElement || pointEl || document.body;

                // 2. Input / Textarea
                if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA')) {
                    var start = target.selectionStart || 0;
                    var end = target.selectionEnd || 0;
                    var val = target.value || '';
                    if (start !== end) {
                        target.value = val.substring(0, start) + val.substring(end);
                        target.selectionStart = target.selectionEnd = start;
                    } else if (start > 0) {
                        target.value = val.substring(0, start - 1) + val.substring(start);
                        target.selectionStart = target.selectionEnd = start - 1;
                    }
                    target.dispatchEvent(new Event('input', { bubbles: true }));
                    target.dispatchEvent(new Event('change', { bubbles: true }));
                    return;
                }

                // 3. Selection API
                if (window.getSelection && !window.getSelection().isCollapsed) {
                    window.getSelection().deleteFromDocument();
                }

                // 4. ContentEditable / execCommand
                try {
                    document.execCommand('delete');
                } catch(e) {}

                // 5. Dispatch Delete and Backspace keyboard events
                var delDown = new KeyboardEvent('keydown', {
                    key: 'Delete',
                    code: 'Delete',
                    keyCode: 46,
                    which: 46,
                    bubbles: true,
                    cancelable: true
                });
                target.dispatchEvent(delDown);
                target.dispatchEvent(new KeyboardEvent('keyup', {
                    key: 'Delete',
                    code: 'Delete',
                    keyCode: 46,
                    which: 46,
                    bubbles: true,
                    cancelable: true
                }));

                var bsDown = new KeyboardEvent('keydown', {
                    key: 'Backspace',
                    code: 'Backspace',
                    keyCode: 8,
                    which: 8,
                    bubbles: true,
                    cancelable: true
                });
                target.dispatchEvent(bsDown);
                target.dispatchEvent(new KeyboardEvent('keyup', {
                    key: 'Backspace',
                    code: 'Backspace',
                    keyCode: 8,
                    which: 8,
                    bubbles: true,
                    cancelable: true
                }));
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun zoomIn(webView: WebView) {
        val nativeZoomed = webView.zoomIn()
        if (!nativeZoomed) {
            val js = """
                (function() {
                    var cur = parseFloat(document.body.style.zoom || '1.0');
                    document.body.style.zoom = (cur + 0.15).toFixed(2);
                })();
            """.trimIndent()
            webView.evaluateJavascript(js, null)
        }
    }

    fun zoomOut(webView: WebView) {
        val nativeZoomed = webView.zoomOut()
        if (!nativeZoomed) {
            val js = """
                (function() {
                    var cur = parseFloat(document.body.style.zoom || '1.0');
                    if (cur > 0.35) {
                        document.body.style.zoom = (cur - 0.15).toFixed(2);
                    }
                })();
            """.trimIndent()
            webView.evaluateJavascript(js, null)
        }
    }

    fun dispatchKeyboardEvent(
        webView: WebView,
        key: String,
        code: String,
        ctrlKey: Boolean = false,
        altKey: Boolean = false,
        shiftKey: Boolean = false
    ) {
        if (ctrlKey && key.lowercase() == "a") {
            executeSelectAll(webView)
            return
        }
        if (ctrlKey && key.lowercase() == "c") {
            executeCopy(webView)
            return
        }
        if (key == "Delete" || key == "Backspace") {
            executeDelete(webView)
            return
        }

        val js = """
            (function() {
                var active = document.activeElement || document.body;
                
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
