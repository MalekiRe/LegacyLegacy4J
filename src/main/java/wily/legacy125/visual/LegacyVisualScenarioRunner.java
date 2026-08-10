package wily.legacy125.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.ContainerChest;
import net.minecraft.src.ContainerFurnace;
import net.minecraft.src.ContainerWorkbench;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.PlayerControllerCreative;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.TileEntityFurnace;
import net.minecraft.src.WorldSettings;
import net.minecraft.src.WorldType;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Controllers;
import org.lwjgl.opengl.GL11;
import wily.legacy125.api.LegacyContainerLayout;
import wily.legacy125.client.screen.LegacyContainerScreen;
import wily.legacy125.client.screen.LegacyCraftingScreen;
import wily.legacy125.client.screen.LegacyInventoryScreen;
import wily.legacy125.client.screen.LegacyControllerScreen;
import wily.legacy125.client.screen.LegacyGuiContainer125;
import wily.legacy125.client.screen.LegacyOptionsScreen;
import wily.legacy125.client.screen.LegacyPauseScreen;
import wily.legacy125.client.screen.LegacyPlayScreen;
import wily.legacy125.client.screen.LegacySettingsScreen;
import wily.legacy125.client.screen.LegacyTitleScreen;
import wily.legacy125.client.screen.LegacyCreateWorldScreen;
import wily.legacy125.input.PadButton;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Opt-in deterministic visual-conformance driver.
 *
 * Enable with -Dlegacy4j.visualScenarios=inventory,chest,... and select an
 * output directory with -Dlegacy4j.visualOutput=/absolute/path. Normal players
 * never execute this code because the scenario property is absent.
 */
public final class LegacyVisualScenarioRunner {
    private static final String DEFAULT_WORLD_DIRECTORY = "__legacy4j_visual_tests";
    private static final String WORLD_NAME = "New World";
    private static final int SETTLE_TICKS = 24;
    private static final int FIXTURE_X = 2;
    private static final int FIXTURE_Y = 64;
    private static final int FIXTURE_Z = 0;

    private final List<String> scenarios;
    private final File outputDirectory;
    private final boolean exitWhenDone;
    private final String worldDirectory;
    private boolean worldRequested;
    private int worldWarmupTicks = SETTLE_TICKS;
    private int scenarioIndex = -1;
    private int settleTicks;
    private boolean complete;

    public static LegacyVisualScenarioRunner fromSystemProperties() {
        String configured = System.getProperty("legacy4j.visualScenarios", "").trim();
        if (configured.length() == 0) return new LegacyVisualScenarioRunner(new ArrayList<String>(), null, false);
        List<String> scenarios = new ArrayList<String>();
        for (String value : configured.split(",")) {
            String scenario = value.trim();
            if (scenario.length() > 0) scenarios.add(scenario);
        }
        String output = System.getProperty("legacy4j.visualOutput", "build/visual-captures");
        boolean exit = Boolean.parseBoolean(System.getProperty("legacy4j.visualExit", "true"));
        return new LegacyVisualScenarioRunner(scenarios, new File(output), exit);
    }

    LegacyVisualScenarioRunner(List<String> scenarios, File outputDirectory, boolean exitWhenDone) {
        this.scenarios = scenarios;
        this.outputDirectory = outputDirectory;
        this.exitWhenDone = exitWhenDone;
        this.worldDirectory = System.getProperty("legacy4j.visualWorld", DEFAULT_WORLD_DIRECTORY);
    }

    public boolean enabled() {
        return !scenarios.isEmpty();
    }

    public void tick(Minecraft minecraft) {
        if (!enabled() || complete) return;
        // Recheck immediately before world loading: another launcher/plugin
        // may initialize Controllers after ModLoader invokes load().
        if (Controllers.isCreated()) Controllers.destroy();
        if (needsWorld() && (minecraft.theWorld == null || minecraft.thePlayer == null)) {
            requestWorld(minecraft);
            return;
        }
        stabilizeWorld(minecraft);
        if (scenarioIndex < 0) {
            // Let one normal world render initialize RenderManager and skin
            // services before an inventory preview tries to render the player.
            if (worldWarmupTicks-- > 0) return;
            scenarioIndex = 0;
            openScenario(minecraft, scenarios.get(scenarioIndex));
            settleTicks = SETTLE_TICKS;
            return;
        }
        if (settleTicks-- > 0) return;
        capture(minecraft, scenarios.get(scenarioIndex));
        scenarioIndex++;
        if (scenarioIndex >= scenarios.size()) {
            complete = true;
            if (exitWhenDone) minecraft.shutdown();
            return;
        }
        openScenario(minecraft, scenarios.get(scenarioIndex));
        settleTicks = SETTLE_TICKS;
    }

    private boolean needsWorld() {
        for (String scenario : scenarios) if (!"title".equals(scenario)) return true;
        return false;
    }

    private void requestWorld(Minecraft minecraft) {
        if (worldRequested) return;
        worldRequested = true;
        minecraft.gameSettings.difficulty = 0;
        minecraft.gameSettings.saveOptions();
        minecraft.playerController = new PlayerControllerCreative(minecraft);
        WorldSettings settings = new WorldSettings(125L, 1, false, false, WorldType.FLAT);
        minecraft.displayGuiScreen(null);
        minecraft.startWorld(worldDirectory, WORLD_NAME, settings);
        minecraft.displayGuiScreen(null);
    }

    private void stabilizeWorld(Minecraft minecraft) {
        minecraft.gameSettings.difficulty = 0;
        if (minecraft.theWorld != null) minecraft.theWorld.setWorldTime(6000L);
        if (minecraft.thePlayer != null) {
            minecraft.thePlayer.setPosition(0.5D, 65.0D, 0.5D);
            minecraft.thePlayer.rotationYaw = 180.0F;
            minecraft.thePlayer.rotationPitch = 15.0F;
            for (int i = 0; i < minecraft.thePlayer.inventory.mainInventory.length; i++) {
                minecraft.thePlayer.inventory.mainInventory[i] = null;
            }
            for (int i = 0; i < minecraft.thePlayer.inventory.armorInventory.length; i++) {
                minecraft.thePlayer.inventory.armorInventory[i] = null;
            }
        }
    }

    private void openScenario(Minecraft minecraft, String scenario) {
        // Restore any slot coordinates owned by the previous screen before a
        // new screen snapshots a shared player container.
        minecraft.displayGuiScreen(null);
        movePointerOffUi(minecraft);
        String screenScenario = baseScenario(scenario);
        GuiScreen screen;
        if ("title".equals(screenScenario)) {
            screen = new LegacyTitleScreen();
        } else if ("pause".equals(screenScenario)) {
            screen = new LegacyPauseScreen();
        } else if ("help_options".equals(screenScenario)) {
            screen = new LegacyOptionsScreen(new LegacyPauseScreen(), minecraft.gameSettings);
        } else if ("settings".equals(screenScenario)) {
            screen = new LegacySettingsScreen(new LegacyOptionsScreen(
                    new LegacyPauseScreen(), minecraft.gameSettings), minecraft.gameSettings);
        } else if ("play".equals(screenScenario)) {
            screen = new LegacyPlayScreen(new LegacyTitleScreen());
        } else if ("create_world".equals(screenScenario)) {
            screen = new LegacyCreateWorldScreen(new LegacyPlayScreen(new LegacyTitleScreen()));
        } else if ("inventory".equals(screenScenario)) {
            screen = new LegacyInventoryScreen(minecraft.thePlayer.inventorySlots);
        } else if ("crafting_player".equals(screenScenario)) {
            screen = new LegacyCraftingScreen(minecraft.thePlayer.inventorySlots);
        } else if ("crafting_table".equals(screenScenario)) {
            setBlock(minecraft, Block.workbench.blockID);
            screen = new LegacyCraftingScreen(new ContainerWorkbench(
                    minecraft.thePlayer.inventory, minecraft.theWorld, FIXTURE_X, FIXTURE_Y, FIXTURE_Z));
        } else if ("crafting_modded_tools".equals(screenScenario)) {
            setBlock(minecraft, Block.workbench.blockID);
            screen = new LegacyCraftingScreen(new ContainerWorkbench(
                    minecraft.thePlayer.inventory, minecraft.theWorld, FIXTURE_X, FIXTURE_Y, FIXTURE_Z));
        } else if ("chest".equals(screenScenario)) {
            setBlock(minecraft, Block.chest.blockID);
            TileEntityChest chest = (TileEntityChest) minecraft.theWorld.getBlockTileEntity(
                    FIXTURE_X, FIXTURE_Y, FIXTURE_Z);
            ContainerChest container = new ContainerChest(minecraft.thePlayer.inventory, chest);
            screen = new LegacyContainerScreen(container,
                    new LegacyContainerLayout(LegacyContainerLayout.Style.CHEST, "Chest", 130, 128));
        } else if ("furnace".equals(screenScenario)) {
            setBlock(minecraft, Block.stoneOvenIdle.blockID);
            TileEntityFurnace furnace = (TileEntityFurnace) minecraft.theWorld.getBlockTileEntity(
                    FIXTURE_X, FIXTURE_Y, FIXTURE_Z);
            ContainerFurnace container = new ContainerFurnace(minecraft.thePlayer.inventory, furnace);
            screen = new LegacyContainerScreen(container,
                    new LegacyContainerLayout(LegacyContainerLayout.Style.FURNACE, "Furnace", 130, 145));
        } else {
            throw new IllegalArgumentException("Unknown visual scenario: " + scenario);
        }
        minecraft.displayGuiScreen(screen);
        if ("crafting_modded_tools".equals(screenScenario)) {
            String members = ((LegacyCraftingScreen) screen).legacyVisualSelectRecipeFamily("tool:pickaxe");
            if (members.length() == 0) throw new IllegalStateException("No pickaxe recipe family found");
            writeMetadata(scenario, "tool:pickaxe = " + members + System.getProperty("line.separator"));
        }
        if (scenario.endsWith("_mouse_hover")) movePointerToFirstSlot(minecraft, screen);
        else if (scenario.endsWith("_controller_hover")) showControllerFocus(screen);
    }

    private String baseScenario(String scenario) {
        if (scenario.endsWith("_mouse_hover")) {
            return scenario.substring(0, scenario.length() - "_mouse_hover".length());
        }
        if (scenario.endsWith("_controller_hover")) {
            return scenario.substring(0, scenario.length() - "_controller_hover".length());
        }
        return scenario;
    }

    private void movePointerToFirstSlot(Minecraft minecraft, GuiScreen screen) {
        if (!(screen instanceof LegacyGuiContainer125)) return;
        // Inject the logical point so automated captures do not depend on the
        // window manager honoring an LWJGL 2 native cursor warp.
        ((LegacyGuiContainer125) screen).legacyVisualHoverFirstSlot();
    }

    private void showControllerFocus(GuiScreen screen) {
        if (!(screen instanceof LegacyControllerScreen)) return;
        LegacyControllerScreen controllerScreen = (LegacyControllerScreen) screen;
        // Crafting starts in recipe mode; the left-stick action switches its
        // focus to the first live inventory slot. An empty-slot A action makes
        // the other screens expose their existing initial selection without
        // changing that selection's position.
        if (screen instanceof LegacyCraftingScreen) controllerScreen.onControllerButton(PadButton.LEFT_STICK);
        else controllerScreen.onControllerButton(PadButton.A);
    }

    private void setBlock(Minecraft minecraft, int blockId) {
        minecraft.theWorld.setBlockWithNotify(FIXTURE_X, FIXTURE_Y, FIXTURE_Z, 0);
        minecraft.theWorld.setBlockWithNotify(FIXTURE_X, FIXTURE_Y, FIXTURE_Z, blockId);
    }

    private void movePointerOffUi(Minecraft minecraft) {
        try {
            // Keep mouse hover from selecting a row in otherwise controller-
            // neutral reference captures. LWJGL coordinates start at bottom.
            Mouse.setCursorPosition(minecraft.displayWidth - 4, minecraft.displayHeight / 2);
        } catch (RuntimeException ignored) {
        }
    }

    private void writeMetadata(String scenario, String contents) {
        try {
            if (!outputDirectory.isDirectory() && !outputDirectory.mkdirs()) {
                throw new IllegalStateException("Cannot create " + outputDirectory);
            }
            java.io.FileWriter writer = new java.io.FileWriter(new File(outputDirectory, scenario + ".txt"));
            try {
                writer.write(contents);
            } finally {
                writer.close();
            }
        } catch (java.io.IOException exception) {
            throw new RuntimeException("Unable to write visual scenario metadata", exception);
        }
    }

    private void capture(Minecraft minecraft, String scenario) {
        try {
            if (!outputDirectory.isDirectory() && !outputDirectory.mkdirs()) {
                throw new IllegalStateException("Cannot create " + outputDirectory);
            }
            int width = minecraft.displayWidth;
            int height = minecraft.displayHeight;
            ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 3);
            GL11.glFinish();
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            int previousReadBuffer = GL11.glGetInteger(GL11.GL_READ_BUFFER);
            // ModLoader's GUI tick hook runs while the next back buffer is
            // still being composed. The front buffer is the last complete,
            // displayed frame and therefore the deterministic screenshot.
            GL11.glReadBuffer(GL11.GL_FRONT);
            try {
                GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixels);
            } finally {
                GL11.glReadBuffer(previousReadBuffer);
            }
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int source = ((height - 1 - y) * width + x) * 3;
                    int red = pixels.get(source) & 255;
                    int green = pixels.get(source + 1) & 255;
                    int blue = pixels.get(source + 2) & 255;
                    image.setRGB(x, y, red << 16 | green << 8 | blue);
                }
            }
            File target = new File(outputDirectory, scenario + ".png");
            ImageIO.write(image, "png", target);
            System.out.println("[Legacy4J Visual] captured " + scenario + " -> " + target.getAbsolutePath());
        } catch (Exception exception) {
            throw new RuntimeException("Unable to capture visual scenario " + scenario, exception);
        }
    }
}
