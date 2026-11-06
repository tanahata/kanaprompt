package jp.kanaflick.offsetprobe;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class ProbeActivity extends Activity {
    private static final String TAG = "CursorOffsetProbe";
    private static final int START_OFFSET = 100;
    private ProbeEditText editor;
    private TextView status;
    private int connections;
    private int extractions;
    private int selections;
    private String lastExtraction = "not called";
    private String lastSelection = "not called";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (12 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        TextView instructions = new TextView(this);
        instructions.setText("Test only: use KanaPrompt left/right cursor keys.\n"
                + "Cursor 101 + right => 103; cursor 103 + left => 101.");
        root.addView(instructions);

        editor = new ProbeEditText(this);
        editor.setSingleLine(false);
        editor.setMinLines(3);
        StringBuilder synthetic = new StringBuilder();
        for (int i = 0; i < START_OFFSET; i++) synthetic.append('a');
        synthetic.append("あ😀い");
        editor.setText(synthetic.toString());
        editor.setSelection(101);
        root.addView(editor, new LinearLayout.LayoutParams(-1, -2));

        Button before = new Button(this);
        before.setText("Set cursor to 101 (before emoji)");
        before.setOnClickListener(v -> chooseCursor(101));
        root.addView(before);
        Button after = new Button(this);
        after.setText("Set cursor to 103 (after emoji)");
        after.setOnClickListener(v -> chooseCursor(103));
        root.addView(after);

        status = new TextView(this);
        root.addView(status);
        setContentView(root);
        chooseCursor(101);
    }

    private void chooseCursor(int position) {
        editor.requestFocus();
        editor.setSelection(position);
        statusUpdate("preset cursor=" + position);
        editor.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    private void statusUpdate(String event) {
        Log.i(TAG, event); // Numeric metadata only, never input text.
        runOnUiThread(() -> {
            if (status == null || editor == null) return;
            status.setText("connections=" + connections
                + " extractions=" + extractions + " setSelection=" + selections
                + "\nactual selection=" + editor.getSelectionStart()
                + "\nlast get: " + lastExtraction
                + "\nlast set: " + lastSelection
                + "\nevent: " + event);
        });
    }

    private final class ProbeEditText extends EditText {
        ProbeEditText(Context context) { super(context); }

        @Override public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            InputConnection target = super.onCreateInputConnection(outAttrs);
            if (target == null) return null;
            connections++;
            statusUpdate("onCreateInputConnection connection=" + connections);
            return new InputConnectionWrapper(target, true) {
                @Override public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
                    extractions++;
                    ExtractedText original = super.getExtractedText(request, flags);
                    if (original == null || original.text == null) {
                        lastExtraction = "raw=null";
                        statusUpdate("getExtractedText null");
                        return original;
                    }
                    int absoluteStart = original.startOffset + original.selectionStart;
                    int absoluteEnd = original.startOffset + original.selectionEnd;
                    if (original.startOffset != 0 || original.text.length() < START_OFFSET
                            || absoluteStart < START_OFFSET || absoluteEnd < START_OFFSET
                            || absoluteStart > original.text.length()
                            || absoluteEnd > original.text.length()) {
                        lastExtraction = "fallback: rawOffset=" + original.startOffset
                                + ", rawSelection=" + original.selectionStart
                                + ", rawLength=" + original.text.length();
                        statusUpdate("getExtractedText fallback");
                        return original;
                    }

                    ExtractedText sliced = new ExtractedText();
                    sliced.text = original.text.subSequence(START_OFFSET, original.text.length());
                    sliced.startOffset = START_OFFSET;
                    sliced.selectionStart = absoluteStart - START_OFFSET;
                    sliced.selectionEnd = absoluteEnd - START_OFFSET;
                    sliced.flags = original.flags;
                    sliced.partialStartOffset = -1;
                    sliced.partialEndOffset = -1;
                    sliced.hint = original.hint;
                    lastExtraction = "returned startOffset=100, relativeStart="
                            + sliced.selectionStart + ", length=" + sliced.text.length();
                    statusUpdate("getExtractedText modified");
                    return sliced;
                }

                @Override public boolean setSelection(int start, int end) {
                    selections++;
                    boolean accepted = super.setSelection(start, end);
                    editor.post(() -> {
                        lastSelection = "request=" + start + ".." + end
                                + ", accepted=" + accepted
                                + ", actual=" + editor.getSelectionStart() + ".." + editor.getSelectionEnd();
                        statusUpdate("setSelection returned");
                    });
                    return accepted;
                }
            };
        }
    }
}
