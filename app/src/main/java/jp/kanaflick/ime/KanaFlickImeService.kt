package jp.kanaflick.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.content.res.Configuration
import android.view.inputmethod.InputConnection
import android.view.inputmethod.EditorInfo

class KanaFlickImeService : InputMethodService() {
    override fun onCreateInputView(): View = KanaKeyboardView(this) { action ->
        val ic: InputConnection = currentInputConnection ?: return@KanaKeyboardView
        when (action) {
            "左全削除" -> {
                if (ic.getSelectedText(0).isNullOrEmpty()) {
                    // Delete through the editor to the beginning; leave text to the right intact.
                    ic.deleteSurroundingText(Int.MAX_VALUE, 0)
                }
            }
            "⌫" -> ic.deleteSurroundingTextInCodePoints(1, 0)
            "空白" -> ic.commitText(" ", 1)
            "↵" -> {
                val info = currentInputEditorInfo
                val imeAction = (info?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
                val isMultiline = (info?.inputType ?: 0) and android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0
                if (!isMultiline && imeAction != EditorInfo.IME_ACTION_NONE && imeAction != EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ic.performEditorAction(imeAction)
                } else ic.commitText("\n", 1)
            }
            "◀" -> moveCursor(ic, -1)
            "▶" -> moveCursor(ic, 1)
            "゛小" -> cyclePreviousKana(ic)
            else -> ic.commitText(action, 1)
        }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        updateNavigationTheme()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateNavigationTheme()
    }

    private fun updateNavigationTheme() {
        val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val imeWindow = window?.window ?: return
        imeWindow.navigationBarColor = KeyboardTheme.forNight(night).background
        val flags = imeWindow.decorView.systemUiVisibility
        imeWindow.decorView.systemUiVisibility = if (night) {
            flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        } else flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    private fun cyclePreviousKana(ic: InputConnection) {
        // Only transform the character immediately before the caret; do not alter a selection.
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) return
        val before = ic.getTextBeforeCursor(2, 0)?.toString() ?: return
        if (before.isEmpty()) return
        val previous = before.last().toString()
        val replacement = DakutenConverter.convert(previous) ?: return
        ic.beginBatchEdit()
        try {
            ic.deleteSurroundingTextInCodePoints(1, 0)
            ic.commitText(replacement, 1)
        } finally {
            ic.endBatchEdit()
        }
    }

    private fun moveCursor(ic: InputConnection, offset: Int) {
        val extracted = ic.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0) ?: return
        if (extracted.selectionStart != extracted.selectionEnd) return
        val pos = extracted.selectionStart
        val text = extracted.text ?: return
        val next = if (offset < 0 && pos > 0) {
            pos - Character.charCount(Character.codePointBefore(text, pos))
        } else if (offset > 0 && pos < text.length) {
            pos + Character.charCount(Character.codePointAt(text, pos))
        } else return
        ic.setSelection(next, next)
    }
}
