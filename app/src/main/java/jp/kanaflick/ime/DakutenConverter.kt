package jp.kanaflick.ime

/** Cycles a single, already committed kana character. */
object DakutenConverter {
    private val cycles = listOf(
        "あぁ", "いぃ", "うぅゔ", "えぇ", "おぉ",
        "かが", "きぎ", "くぐ", "けげ", "こご",
        "さざ", "しじ", "すず", "せぜ", "そぞ",
        "ただ", "ちぢ", "つっづ", "てで", "とど",
        "はばぱ", "ひびぴ", "ふぶぷ", "へべぺ", "ほぼぽ",
        "やゃ", "ゆゅ", "よょ", "わゎ"
    )
    private val next: Map<String, String> = buildMap {
        for (cycle in cycles) {
            val chars = cycle.map { it.toString() }
            for (i in chars.indices) put(chars[i], chars[(i + 1) % chars.size])
        }
    }

    fun convert(char: String): String? = next[char]

    // Direct variants are independent of the legacy tap-cycle order.
    private val voiced = "がぎぐげござじずぜぞだぢづでどばびぶべぼゔ"
    private val semiVoiced = "ぱぴぷぺぽ"
    private val small = "ぁぃぅぇぉっゃゅょゎ"

    fun direct(char: String, action: String): String? {
        val cycle = cycles.firstOrNull { char.length == 1 && char[0] in it } ?: return null
        val targets = when (action) {
            "濁点" -> voiced
            "半濁点" -> semiVoiced
            "小文字" -> small
            else -> return null
        }
        return cycle.firstOrNull { it in targets }?.toString()
    }

    /** Center/left/up/right/down; unavailable variants stay blank. */
    fun options(char: String): List<String> = listOf(
        "", direct(char, "濁点") ?: "", direct(char, "小文字") ?: "",
        direct(char, "半濁点") ?: "", ""
    )
}
