package wily.legacy125;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftActionsTest {
    @Test
    void resolvesMinecraft125GuiInputEntrypoints() {
        assertTrue(MinecraftActions.guiBridgeAvailable());
    }
}
