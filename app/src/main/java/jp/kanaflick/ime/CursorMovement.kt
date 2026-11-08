package jp.kanaflick.ime

import java.text.BreakIterator
import java.util.Locale

/** Grapheme-aware moves, including emoji sequences, within extracted editor text. */
internal object CursorMovement {
    private fun breaks(text: CharSequence): BreakIterator =
        BreakIterator.getCharacterInstance(Locale.ROOT).apply { setText(text.toString()) }

    fun nextPosition(text: CharSequence, selectionStart: Int, startOffset: Int, direction: Int): Int? {
        if (startOffset < 0 || selectionStart < 0 || selectionStart > text.length) return null
        val iterator = breaks(text)
        val next = when (direction) {
            -1 -> iterator.preceding(selectionStart)
            1 -> iterator.following(selectionStart)
            else -> return null
        }
        return if (next == BreakIterator.DONE) null else startOffset + next
    }

    fun previousCharacterCodePoints(textBeforeCursor: CharSequence): Int? {
        if (textBeforeCursor.isEmpty()) return null
        val text = textBeforeCursor.toString()
        val start = breaks(text).preceding(text.length)
        return if (start == BreakIterator.DONE) null else Character.codePointCount(text, start, text.length)
    }
}
