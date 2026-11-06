import jp.kanaflick.ime.CursorMovement;

public class CursorMovementCheck {
    private static void equal(Integer actual, Integer expected) {
        if (!java.util.Objects.equals(actual, expected))
            throw new AssertionError("Expected " + expected + ", got " + actual);
    }

    public static void main(String[] args) {
        CursorMovement cursor = CursorMovement.INSTANCE;
        equal(cursor.nextPosition("かな", 1, 0, -1), 0);
        equal(cursor.nextPosition("かな", 1, 0, 1), 2);
        equal(cursor.nextPosition("かな", 0, 0, -1), null);
        equal(cursor.nextPosition("かな", 2, 0, 1), null);

        // A partial extraction begins after the editor's first 100 characters.
        equal(cursor.nextPosition("あ😀い", 1, 100, 1), 103);
        equal(cursor.nextPosition("あ😀い", 3, 100, -1), 101);
        equal(cursor.nextPosition("あ😀い", 0, 100, 1), 101);
        equal(cursor.nextPosition("あ😀い", 4, 100, -1), 103);
        equal(cursor.nextPosition("あ😀い", 0, 100, -1), null);
        equal(cursor.nextPosition("あ😀い", 4, 100, 1), null);

        // Reject incomplete or invalid extraction positions rather than throwing.
        equal(cursor.nextPosition("", 0, 0, 1), null);
        equal(cursor.nextPosition("あ", -1, 10, 1), null);
        equal(cursor.nextPosition("あ", 2, 10, -1), null);
        equal(cursor.nextPosition("あ", 0, -1, 1), null);
        equal(cursor.nextPosition("あ", 0, 10, 0), null);
        System.out.println("PASS: cursor movement with extraction offsets, emoji, and boundaries");
    }
}
