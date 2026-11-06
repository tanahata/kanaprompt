package jp.kanaflick.ime

/** Converts a cursor move within extracted text to an absolute editor position. */
internal object CursorMovement {
    fun nextPosition(text: CharSequence, selectionStart: Int, startOffset: Int, direction: Int): Int? {
        if (startOffset < 0 || selectionStart < 0 || selectionStart > text.length) return null
        val next = when (direction) {
            -1 -> if (selectionStart > 0) {
                selectionStart - Character.charCount(Character.codePointBefore(text, selectionStart))
            } else return null
            1 -> if (selectionStart < text.length) {
                selectionStart + Character.charCount(Character.codePointAt(text, selectionStart))
            } else return null
            else -> return null
        }
        return startOffset + next
    }
}
