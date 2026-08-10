package wily.legacy125.client.screen;

import net.minecraft.src.Slot;
import wily.legacy125.input.PadButton;

import java.util.List;

/** Geometry navigation shared by built-in and completely unknown mod containers. */
public final class SlotNavigator {
    private SlotNavigator() {
    }

    public static int neighbor(List slots, int from, PadButton direction) {
        if (slots == null || slots.isEmpty()) return -1;
        if (from < 0 || from >= slots.size()) return firstVisible(slots);
        Slot origin = (Slot) slots.get(from);
        int best = from;
        int bestScore = Integer.MAX_VALUE;
        for (int i = 0; i < slots.size(); i++) {
            if (i == from) continue;
            Slot candidate = (Slot) slots.get(i);
            if (!visible(candidate)) continue;
            int dx = candidate.xDisplayPosition - origin.xDisplayPosition;
            int dy = candidate.yDisplayPosition - origin.yDisplayPosition;
            if (direction == PadButton.DPAD_LEFT && dx >= 0) continue;
            if (direction == PadButton.DPAD_RIGHT && dx <= 0) continue;
            if (direction == PadButton.DPAD_UP && dy >= 0) continue;
            if (direction == PadButton.DPAD_DOWN && dy <= 0) continue;
            int primary = direction == PadButton.DPAD_LEFT || direction == PadButton.DPAD_RIGHT
                    ? Math.abs(dx) : Math.abs(dy);
            int secondary = direction == PadButton.DPAD_LEFT || direction == PadButton.DPAD_RIGHT
                    ? Math.abs(dy) : Math.abs(dx);
            int score = primary * 4 + secondary * 7;
            if (score < bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }

    public static int firstVisible(List slots) {
        if (slots == null) return -1;
        for (int i = 0; i < slots.size(); i++) {
            if (visible((Slot) slots.get(i))) return i;
        }
        return -1;
    }

    private static boolean visible(Slot slot) {
        return slot.xDisplayPosition > -100 && slot.yDisplayPosition > -100;
    }
}
