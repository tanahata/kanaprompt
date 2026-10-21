import jp.kanaflick.ime.KeyboardLayout;
import jp.kanaflick.ime.DakutenConverter;

public class KeyboardLayoutCheck {
    static void equal(Object actual, Object expected) {
        if (!java.util.Objects.equals(actual, expected))
            throw new AssertionError("Expected " + expected + ", got " + actual);
    }
    static void checkGeometry(KeyboardLayout k) {
        for (int row = 0; row < k.getRows().size(); row++) {
            float end = k.keyStart(row, 0);
            for (int col = 0; col < k.getRows().get(row).size(); col++) {
                float start = k.keyStart(row, col), width = k.keyWidth(row, col);
                if (Math.abs(start - end) > .00001f || width <= 0) throw new AssertionError("Key bounds");
                equal(k.keyIndex(row, start + width / 2), col);
                end = start + width;
            }
            float inset = k.keyStart(row, 0);
            if (Math.abs(end - (1 - inset)) > .00001f) throw new AssertionError("Row width");
            equal(k.keyIndex(row, -0.01f), -1);
            equal(k.keyIndex(row, 1.01f), -1);
            if (inset > 0) {
                equal(k.keyIndex(row, inset / 2), -1);
                equal(k.keyIndex(row, 1 - inset / 2), -1);
            }
        }
    }

    public static void main(String[] args) {
        KeyboardLayout k = new KeyboardLayout();
        equal(k.displayLabel("あ1"), "1");
        equal(k.displayLabel("あa"), "a");
        equal(k.getKeyAreaHeightDp(), 236);
        equal(k.getKeyGapDp(), 6);
        equal(k.press("か", 0), "か");
        equal(k.press("か", 1), "き");
        equal(k.press("か", 2), "く");
        equal(k.press("か", 3), "け");
        equal(k.press("か", 4), "こ");
        String[] punctuation = {"、", "。", "？", "！", "…"};
        for (int i=0; i<5; i++) equal(k.press("、。", i), punctuation[i]);
        equal(k.press("？", 0), null);
        equal(k.getRows().get(0).get(0), "左全削除");
        equal(k.displayLabel("左全削除"), "〈⌫");
        equal(k.press("左全削除", 0), "左全削除");
        equal(k.press("あa", 0), null);
        equal(String.join("", k.getRows().get(0)), "qwertyuiop");
        equal(k.press("q", 0), "q");
        equal(k.displayLabel("あa"), "あ");
        equal(k.getKeyAreaHeightDp(), 236);
        equal(k.getKeyGapDp(), 4);
        equal(k.getRows().get(3), java.util.List.of("あa", ",", "?123", "空白", ".", "↵"));
        if (Math.abs(k.keyStart(1, 0) - .05f) > .00001f) throw new AssertionError("Home row inset");
        if (Math.abs(k.keyWidth(2, 0) - .15f) > .00001f) throw new AssertionError("Shift width");
        if (Math.abs(k.keyWidth(3, 3) - .4f) > .00001f) throw new AssertionError("Space width");
        checkGeometry(k);
        equal(k.options("q"), null);
        equal(k.press("⇧", 0), null);
        equal(String.join("", k.getRows().get(0)), "QWERTYUIOP");
        equal(k.displayLabel("空白"), "");
        equal(k.press("Q", 0), "Q");
        equal(String.join("", k.getRows().get(0)), "qwertyuiop");
        equal(k.press("⇧", 0), null);
        equal(k.press("⇧", 0), null);
        equal(k.getRows().get(2).get(0), "⇪");
        equal(k.press("A", 0), "A");
        equal(k.press("B", 0), "B");
        equal(String.join("", k.getRows().get(0)), "QWERTYUIOP");
        equal(k.press("?123", 0), null);
        equal(String.join("", k.getRows().get(0)), "1234567890");
        equal(k.getRows().get(1), java.util.List.of("@", "#", "$", "_", "&", "-", "+", "(", ")", "/"));
        checkGeometry(k);
        for (var row : k.getRows()) for (String key : row) {
            if (java.util.List.of("ABC", "あa", "=\\<", "⌫", "空白", "↵").contains(key)) continue;
            equal(k.options(key), null);
            for (int direction = 0; direction < 5; direction++) equal(k.press(key, direction), key);
        }
        equal(k.press("=\\<", 0), null);
        equal(k.getRows().get(0), java.util.List.of("~", "`", "|", "•", "√", "π", "÷", "×", "§", "∆"));
        equal(k.getRows().get(1), java.util.List.of("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\"));
        equal(k.getRows().get(2), java.util.List.of("?123", "%", "©", "®", "™", "✓", "[", "]", "⌫"));
        equal(k.getRows().get(3), java.util.List.of("あa", "<", "ABC", "空白", ">", "↵"));
        checkGeometry(k);
        for (var row : k.getRows()) for (String key : row) {
            if (java.util.List.of("ABC", "あa", "?123", "⌫", "空白", "↵").contains(key)) continue;
            for (int direction = 0; direction < 5; direction++) equal(k.press(key, direction), key);
        }
        for (int repeat = 0; repeat < 20; repeat++) {
            equal(k.press("?123", 0), null);
            equal(String.join("", k.getRows().get(0)), "1234567890");
            equal(k.press("=\\<", 0), null);
            equal(k.getRows().get(0).get(0), "~");
        }
        equal(k.press("あa", 0), null);
        equal(k.getRows().get(0).get(1), "あ");
        equal(k.press("あa", 0), null);
        equal(String.join("", k.getRows().get(0)), "QWERTYUIOP");
        equal(k.press("?123", 0), null);
        equal(k.press("ABC", 0), null);
        equal(String.join("", k.getRows().get(0)), "QWERTYUIOP");
        checkGeometry(k);
        equal(k.press("あa", 0), null);
        equal(k.getRows().get(0).get(1), "あ");
        equal(k.press("あ1", 0), null);
        equal(k.getRows().get(3).get(1), "()[]");
        equal(k.displayLabel("かな"), "あ");
        equal(k.press("1", 0), "1");
        equal(k.press("あa", 0), null);
        equal(k.getRows().get(0).size(), 10);
        equal(k.press("あa", 0), null);
        equal(k.getRows().get(0).get(1), "1");
        String[] symbols = {".", ",", "-", "/", ""};
        String[] brackets = {"(", ")", "[", "]", ""};
        String[] zero = {"0", "〜", "…", "@", ""};
        for (int i=0; i<5; i++) {
            equal(k.press(".,-/", i), symbols[i]);
            equal(k.press("()[]", i), brackets[i]);
            equal(k.press("0", i), zero[i]);
        }
        for (int n=1; n<=9; n++) equal(k.press(""+n, 0), ""+n);
        equal(k.press("かな", 0), null);
        equal(k.getRows().get(3).get(1), "゛小");
        equal(k.press("つ", 0), "つ");
        String ch = "つ";
        for (String want : new String[]{"っ","づ","つ"}) {
            ch = DakutenConverter.INSTANCE.convert(ch); equal(ch, want);
        }
        for (int i=0; i<20; i++) {
            equal(k.press("あ1", 0), null);
            equal(k.press("かな", 0), null);
        }
        equal(k.press("、。", 2), "？");
        equal(k.displayLabel("空白"), "␣");
        equal(k.press("あa", 0), null);
        equal(k.press("⇪", 0), null);
        equal(String.join("", k.getRows().get(0)), "qwertyuiop");
        checkGeometry(k);
        System.out.println("PASS: mode switching, all symbol directions, digits, kana and dakuten regression checks");
    }
}
