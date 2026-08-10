package wily.legacy125.input;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MenuButtonLatch125Test {
    private final MenuButtonLatch125 latch = new MenuButtonLatch125();

    @Test
    void heldStartCannotReopenMenuAfterGuiCloses() {
        latch.onGuiTick();

        assertTrue(latch.suppressMenuOpen(frame(PadButton.START)));
        assertTrue(latch.suppressMenuOpen(frame(PadButton.START)));
        assertTrue(latch.suppressMenuOpen(frame()));
        assertFalse(latch.suppressMenuOpen(frame(PadButton.START)));
    }

    @Test
    void heldBackAlsoRequiresRelease() {
        latch.onGuiTick();

        assertTrue(latch.suppressMenuOpen(frame(PadButton.BACK)));
        assertTrue(latch.suppressMenuOpen(frame()));
        assertFalse(latch.suppressMenuOpen(frame(PadButton.BACK)));
    }

    @Test
    void doesNotSuppressNormalGameplayWithoutGuiTransition() {
        assertFalse(latch.suppressMenuOpen(frame(PadButton.START)));
    }

    private static ControllerFrame frame(PadButton... buttons) {
        EnumSet<PadButton> down = EnumSet.noneOf(PadButton.class);
        for (PadButton button : buttons) down.add(button);
        return new ControllerFrame(true, "test", 0, 0, 0, 0, down);
    }
}
