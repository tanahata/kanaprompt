package jp.kanaflick.ime

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
            else -> qwertyRows
        } else if (numeric) numberRows else kanaRows

    private val qwertyRows: List<List<String>>
        get() {
            fun letters(value: String) = value.map { if (shift != 0) it.uppercase() else it.toString() }
            return listOf(
                letters("qwertyuiop"),
                letters("asdfghjkl"),
                listOf(if (shift == 2) "⇪" else "⇧") + letters("zxcvbnm") + "⌫",
                listOf("あa", ",", "?123", "空白", ".", "↵")
            )
        }

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

    fun isShiftActive(key: String): Boolean = key in listOf("⇧", "⇪") && shift != 0

    private fun weights(row: Int): List<Float> {
        val keys = rows[row]
        return when {
            alphabetic && row == 2 -> listOf(1.5f) + List(7) { 1f } + 1.5f
            alphabetic && row == 3 -> listOf(1.5f, 1f, 1f, 4f, 1f, 1.5f)
            else -> List(keys.size) { 1f }
        }
    }

    /** Normalized bounds shared by drawing and touch hit testing. */
    fun keyStart(row: Int, column: Int): Float {
        val weights = weights(row)
        // Gboard US home row: nine 1/10-width keys, inset by half a key.
        if (alphabetic && symbols == 0 && row == 1) return (0.5f + column) / 10f
        return weights.take(column).sum() / weights.sum()
    }

    fun keyWidth(row: Int, column: Int): Float {
        val weights = weights(row)
        if (alphabetic && symbols == 0 && row == 1) return 0.1f
        return weights[column] / weights.sum()
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
