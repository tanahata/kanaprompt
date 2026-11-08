package jp.kanaprompt.ime

/** Kana/numeric grids and a tap-only QWERTY grid share the same height. */
class KeyboardLayout {
    private var numeric = false
    private var alphabetic = false
    private var shift = 0 // 0: off, 1: next letter, 2: caps lock
    private var symbols = 0 // 0: letters, 1: numbers/symbols, 2: extra symbols

    val rows: List<List<String>>
        get() = if (alphabetic) when (symbols) {
            1 -> symbolRows
            2 -> extraSymbolRows
            else -> when (shift) {
                1 -> shiftedQwertyRows
                2 -> capsQwertyRows
                else -> qwertyRows
            }
        } else if (numeric) numberRows else kanaRows

    private fun createQwertyRows(uppercase: Boolean, caps: Boolean): List<List<String>> {
        fun letters(value: String) = value.map { if (uppercase) it.uppercase() else it.toString() }
        return listOf(
            letters("qwertyuiop"),
            letters("asdfghjkl"),
            listOf(if (caps) "⇪" else "⇧") + letters("zxcvbnm") + "⌫",
            listOf("あa", ",", "?123", "空白", ".", "↵")
        )
    }

    private val qwertyRows = createQwertyRows(uppercase = false, caps = false)
    private val shiftedQwertyRows = createQwertyRows(uppercase = true, caps = false)
    private val capsQwertyRows = createQwertyRows(uppercase = true, caps = true)

    // Gboard English (US), observed on the user's device on 2026-10-21.
    private val symbolRows = listOf(
        "1234567890".map { it.toString() },
        listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/"),
        listOf("=\\<", "*", "\"", "'", ":", ";", "!", "?", "⌫"),
        listOf("あa", ",", "ABC", "空白", ".", "↵")
    )

    private val extraSymbolRows = listOf(
        listOf("~", "`", "|", "•", "√", "π", "÷", "×", "§", "∆"),
        listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\"),
        listOf("?123", "%", "©", "®", "™", "✓", "[", "]", "⌫"),
        listOf("あa", "<", "ABC", "空白", ">", "↵")
    )

    val keyAreaHeightDp: Int get() = 236
    val keyGapDp: Int get() = if (alphabetic) 4 else 6

    // Keep action IDs separate from labels: あ and 1 are also input characters.
    fun displayLabel(key: String): String = when {
        key == "あa" -> if (alphabetic) "あ" else "a"
        key == "あ1" -> "1"
        key == "かな" -> "あ"
        key == "左全削除" -> "〈⌫"
        key == "空白" -> if (alphabetic) "" else "␣"
        else -> key
    }

    fun isShiftActive(key: String): Boolean = (key == "⇧" || key == "⇪") && shift != 0

    // The two weighted QWERTY rows each total 10 units.
    private val qwertyShiftWeights = floatArrayOf(1.5f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1.5f)
    private val qwertyBottomWeights = floatArrayOf(1.5f, 1f, 1f, 4f, 1f, 1.5f)
    private fun weights(row: Int): FloatArray? = if (alphabetic) when (row) {
        2 -> qwertyShiftWeights
        3 -> qwertyBottomWeights
        else -> null
    } else null

    /** Normalized bounds shared by drawing and touch hit testing. */
    fun keyStart(row: Int, column: Int): Float {
        // Gboard US home row: nine 1/10-width keys, inset by half a key.
        if (alphabetic && symbols == 0 && row == 1) return (0.5f + column) / 10f
        val weights = weights(row) ?: return column.toFloat() / rows[row].size
        var left = 0f
        for (i in 0 until column) left += weights[i]
        return left / 10f
    }

    fun keyWidth(row: Int, column: Int): Float {
        if (alphabetic && symbols == 0 && row == 1) return 0.1f
        val weights = weights(row) ?: return 1f / rows[row].size
        return weights[column] / 10f
    }

    fun keyIndex(row: Int, x: Float): Int = rows[row].indices.firstOrNull {
        x >= keyStart(row, it) && x < keyStart(row, it) + keyWidth(row, it)
    } ?: -1

    fun toggleMode() {
        numeric = !numeric
    }

    private val kanaRows = listOf(
        listOf("左全削除", "あ", "か", "さ", "⌫"),
        listOf("◀", "た", "な", "は", "▶"),
        listOf("あ1", "ま", "や", "ら", "空白"),
        listOf("あa", "゛小", "わ", "、。", "↵")
    )

    private val numberRows = listOf(
        listOf("左全削除", "1", "2", "3", "⌫"),
        listOf("◀", "4", "5", "6", "▶"),
        listOf("かな", "7", "8", "9", "空白"),
        listOf("あa", "()[]", "0", ".,-/", "↵")
    )

    // Ordered as center, left, up, right, down.
    // Kana punctuation: Android.com's Gboard guide. Numeric bottom row: device observation.
    private val symbolMaps = mapOf(
        "゛小" to listOf("゛小", "濁点", "小文字", "半濁点", ""),
        "、。" to listOf("、", "。", "？", "！", "…"),
        "()[]" to listOf("(", ")", "[", "]", ""),
        "0" to listOf("0", "〜", "…", "@", ""),
        ".,-/" to listOf(".", ",", "-", "/", "")
    )

    private val kanaMaps = mapOf(
        "あ" to listOf("あ","い","う","え","お"), "か" to listOf("か","き","く","け","こ"),
        "さ" to listOf("さ","し","す","せ","そ"), "た" to listOf("た","ち","つ","て","と"),
        "な" to listOf("な","に","ぬ","ね","の"), "は" to listOf("は","ひ","ふ","へ","ほ"),
        "ま" to listOf("ま","み","む","め","も"), "や" to listOf("や","","ゆ","","よ"),
        "ら" to listOf("ら","り","る","れ","ろ"), "わ" to listOf("わ","を","ん","ー","")
    )

    fun options(key: String): List<String>? = if (alphabetic) null else symbolMaps[key] ?: kanaMaps[key]

    /** Mode switches and placeholder keys never enter text into the editor. */
    fun press(key: String, direction: Int): String? {
        require(direction in 0..4)
        if (key == "あa") {
            alphabetic = !alphabetic
            symbols = 0
            return null
        }
        if (alphabetic && key in listOf("?123", "ABC", "=\\<")) {
            symbols = when (key) {
                "ABC" -> 0
                "=\\<" -> 2
                else -> 1
            }
            return null
        }
        if (key == "⇧" || key == "⇪") {
            shift = (shift + 1) % 3
            return null
        }
        if (key == "あ1" || key == "かな") {
            toggleMode()
            return null
        }
        if (key == "？") return null
        val value = options(key)?.get(direction) ?: key
        if (alphabetic && symbols == 0 && key.length == 1 && key[0] in 'A'..'Z' && shift == 1) {
            shift = 0
        }
        return value
    }
}
