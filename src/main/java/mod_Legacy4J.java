import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiScreen;
import org.lwjgl.input.Controllers;
import wily.legacy125.LegacyConfig;
import wily.legacy125.LegacyController;
import wily.legacy125.client.LegacyGuiIngame125;
import wily.legacy125.visual.LegacyVisualScenarioRunner;

/** Risugami ModLoader entry point for Minecraft 1.2.5. */
public final class mod_Legacy4J extends BaseMod {
    private LegacyController controller;
    private LegacyVisualScenarioRunner visualScenarios;

    @Override
    public void load() {
        visualScenarios = LegacyVisualScenarioRunner.fromSystemProperties();
        if (visualScenarios.enabled()) {
            // Display.update() polls every initialized LWJGL controller. Some
            // Linux joystick devices block forever inside native event reads;
            // deterministic UI captures do not consume controller input.
            if (Controllers.isCreated()) Controllers.destroy();
        } else controller = new LegacyController(LegacyConfig.load());
        ModLoader.setInGameHook(this, true, false);
        ModLoader.setInGUIHook(this, true, false);
    }

    @Override
    public String getVersion() {
        return "Legacy4J 1.2.5 Backport 0.4.0";
    }

    @Override
    public boolean onTickInGame(float partialTick, Minecraft minecraft) {
        LegacyGuiIngame125.install(minecraft);
        // ModLoader also invokes the GUI hook for an in-world screen. Count a
        // visual settle tick only once so captures are deterministic.
        if (visualScenarios.enabled()) {
            if (minecraft.currentScreen == null) visualScenarios.tick(minecraft);
            return true;
        }
        controller.tickGame(minecraft);
        return true;
    }

    @Override
    public boolean onTickInGUI(float partialTick, Minecraft minecraft, GuiScreen screen) {
        LegacyGuiIngame125.install(minecraft);
        if (visualScenarios.enabled()) {
            visualScenarios.tick(minecraft);
            return true;
        }
        controller.tickGui(minecraft, screen);
        return true;
    }
}
