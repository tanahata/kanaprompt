import jp.kanaprompt.ime.KeyboardTheme;
import java.nio.file.Files;
import java.nio.file.Path;

/** Pure-JVM checks for Yamabuki color values and WCAG 2.2 contrast thresholds. */
public class KeyboardThemeCheck {
    static void equal(int actual, int expected, String label) {
        if (actual != expected) throw new AssertionError(label + ": expected " +
            Integer.toHexString(expected) + ", got " + Integer.toHexString(actual));
    }

    static double channel(int argb, int shift) {
        double v = ((argb >>> shift) & 0xFF) / 255.0;
        return v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    static double luminance(int color) {
        return 0.2126 * channel(color, 16) + 0.7152 * channel(color, 8) +
            0.0722 * channel(color, 0);
    }

    static void contrast(String name, int a, int b, double threshold) {
        double x = luminance(a), y = luminance(b);
        double result = (Math.max(x, y) + 0.05) / (Math.min(x, y) + 0.05);
        if (result < threshold) {
            throw new AssertionError(name + " contrast " + result + ": expected >= " + threshold);
        }
    }

    static void verify(KeyboardTheme theme, boolean night) {
        contrast("key text", theme.getText(), theme.getKey(), 4.5);
        contrast("utility text", theme.getText(), theme.getUtility(), 4.5);
        contrast("pressed text", theme.getPressedText(), theme.getPressed(), 4.5);
        contrast("accent", theme.getAccent(), theme.getBackground(), 4.5);
        contrast("outline to background", theme.getOutline(), theme.getBackground(), 3.0);
        contrast("outline to key", theme.getOutline(), theme.getKey(), 3.0);
        contrast("pressed to background", theme.getPressed(), theme.getBackground(), 3.0);
        equal(theme.getPressedText(), night ? 0xFF29231B : 0xFFFFFFFF, "pressedText");
        equal(theme.getAccent(), night ? 0xFFF5C76E : 0xFF8A5900, "accent");
        equal(theme.getBackground(), night ? 0xFF201D18 : 0xFFFFFBF4, "background");
    }

    public static void main(String[] args) throws Exception {
        verify(KeyboardTheme.Companion.forNight(false), false);
        verify(KeyboardTheme.Companion.forNight(true), true);
        // The foreground keeps the existing icon shape, but switches to dark ink.
        int brand = 0xFFD99A22;
        contrast("icon fill", 0xFF29231B, brand, 3.0);
        contrast("icon stroke", 0xFF493000, brand, 3.0);
        String colorsXml = Files.readString(Path.of("app/src/main/res/values/colors.xml"));
        if (!colorsXml.contains("#D99A22")) throw new AssertionError("launcher brand color");
        String iconXml = Files.readString(Path.of("app/src/main/res/drawable/ic_launcher_foreground.xml"));
        if (!iconXml.contains("#29231B") || !iconXml.contains("#493000")) {
            throw new AssertionError("launcher foreground palette");
        }
        System.out.println("KeyboardThemeCheck: Yamabuki palette and contrast OK");
    }
}
