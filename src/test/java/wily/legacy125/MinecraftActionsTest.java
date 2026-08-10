package wily.legacy125;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftActionsTest {
    @Test
    void resolvesMinecraft125GameplayAndGuiInputEntrypoints() {
        assertTrue(MinecraftActions.gameplayBridgeAvailable());
        assertTrue(MinecraftActions.guiBridgeAvailable());
    }
}
