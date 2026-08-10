package wily.legacy125;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PromptStripLayout125Test {
    @Test
    void lastPromptEndsExactlyAtRightEdge() {
        int rightEdge = 854;
        int gap = 5;
        int[] widths = {24, 28, 31, 43};

        int start = PromptStripLayout125.rightAlignedStart(rightEdge, gap, widths);

        assertEquals(rightEdge,
                start + PromptStripLayout125.totalWidth(gap, widths));
    }

    @Test
    void promptWidthIncludesOnlyBadgeTextSeparation() {
        assertEquals(29, PromptStripLayout125.promptWidth(9, 18));
    }
}
