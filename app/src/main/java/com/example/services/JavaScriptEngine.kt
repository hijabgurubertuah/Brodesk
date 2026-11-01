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
                    buttons: ${if (eventType == "mousedown") 1 else if (eventType == "contextmenu") 2 else 0},
                    relatedTarget: null
                });
                el.dispatchEvent(evt);

                if ('$eventType' === 'click') {
                    if (typeof el.focus === 'function') el.focus();
                    if (el.tagName === 'A' || el.tagName === 'BUTTON' || el.getAttribute('role') === 'button') {
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
