package wily.legacy125.client.screen;

/** Pure focus movement used by controller screens and their headless simulation tests. */
public final class FocusNavigator {
    private FocusNavigator() {
    }

    public static int move(int current, int amount, boolean[] focusable) {
        if (focusable.length == 0) return -1;
        int direction = amount >= 0 ? 1 : -1;
        int candidate = floorMod(current + amount, focusable.length);
        for (int checked = 0; checked < focusable.length; checked++) {
            if (focusable[candidate]) return candidate;
            candidate = floorMod(candidate + direction, focusable.length);
        }
        return -1;
    }

    private static int floorMod(int value, int divisor) {
        int result = value % divisor;
        return result < 0 ? result + divisor : result;
    }
}
