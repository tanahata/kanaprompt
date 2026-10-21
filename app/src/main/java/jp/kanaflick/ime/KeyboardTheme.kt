package jp.kanaflick.ime

/** Mint Sign palette; follows the system appearance without storing preferences. */
data class KeyboardTheme(
    val background: Int,
    val key: Int,
    val utility: Int,
    val text: Int,
    val accent: Int,
    val pressed: Int
) {
    companion object {
        fun forNight(night: Boolean): KeyboardTheme = if (night) {
            KeyboardTheme(0xFF202523.toInt(), 0xFF343D38.toInt(), 0xFF2C342F.toInt(),
                0xFFE4EEE7.toInt(), 0xFF9DDBBE.toInt(), 0xFF345949.toInt())
        } else {
            KeyboardTheme(0xFFE6E9E6.toInt(), 0xFFFCFDFB.toInt(), 0xFFD4DCD7.toInt(),
                0xFF26362F.toInt(), 0xFF206A55.toInt(), 0xFFC0E7D8.toInt())
        }
    }
}
