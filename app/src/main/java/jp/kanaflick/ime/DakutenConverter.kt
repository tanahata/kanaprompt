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
}
