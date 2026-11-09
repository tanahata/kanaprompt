package jp.kanaprompt.ime

/** Yamabuki palette. Appearance follows the system; no settings are stored. */
data class KeyboardTheme(
    val background: Int,
    val key: Int,
    val utility: Int,
    val text: Int,
    val accent: Int,
    val pressed: Int,
    val pressedText: Int,
    val outline: Int
) {
    companion object {
        fun forNight(night: Boolean): KeyboardTheme = if (night) {
            KeyboardTheme(0xFF201D18.toInt(), 0xFF39342D.toInt(), 0xFF514532.toInt(),
                0xFFF7F0E7.toInt(), 0xFFF5C76E.toInt(), 0xFFF5C76E.toInt(),
                0xFF29231B.toInt(), 0xFFB0A28C.toInt())
        } else {
            KeyboardTheme(0xFFFFFBF4.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFE8B3.toInt(),
                0xFF29231B.toInt(), 0xFF8A5900.toInt(), 0xFF8A5900.toInt(),
                0xFFFFFFFF.toInt(), 0xFF796F60.toInt())
        }
    }
}
