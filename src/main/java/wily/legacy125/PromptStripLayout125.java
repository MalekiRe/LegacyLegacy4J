package wily.legacy125;

/** Pure layout math for pixel-testable, right-anchored controller prompts. */
final class PromptStripLayout125 {
    private static final int BADGE_TEXT_GAP = 2;

    private PromptStripLayout125() {
    }

    static int promptWidth(int badgeWidth, int textWidth) {
        return badgeWidth + BADGE_TEXT_GAP + textWidth;
    }

    static int totalWidth(int gap, int... promptWidths) {
        int width = 0;
        for (int promptWidth : promptWidths) width += promptWidth;
        if (promptWidths.length > 1) width += gap * (promptWidths.length - 1);
        return width;
    }

    static int rightAlignedStart(int rightEdge, int gap, int... promptWidths) {
        return rightEdge - totalWidth(gap, promptWidths);
    }
}
