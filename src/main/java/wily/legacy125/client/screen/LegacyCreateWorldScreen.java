package wily.legacy125.client.screen;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiCreateWorld;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.PlayerControllerCreative;
import net.minecraft.src.PlayerControllerSP;
import net.minecraft.src.WorldSettings;
import net.minecraft.src.WorldType;
import wily.legacy125.input.PadButton;

import java.util.Random;

public final class LegacyCreateWorldScreen extends LegacyPanelScreen {
    private String worldName = "New World";
    private String seedText = "";
    private int mode;
    private boolean structures = true;
    private boolean flat;

    public LegacyCreateWorldScreen(GuiScreen parent) {
        super(parent, "Create New World");
    }

    @Override
    protected void addLegacyControls() {
        int x = panelLeft + 16;
        int w = panelRight - panelLeft - 32;
        int y = panelTop + 35;
        for (int id = 1; id <= 5; id++) addButton(id, x, y + (id - 1) * 23, w, "");
        addButton(6, x, y + 5 * 23, w / 2 - 4, "Create World");
        addButton(0, x + w / 2 + 4, y + 5 * 23, w / 2 - 4, "Back");
        updateLabels();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            mc.displayGuiScreen(new LegacyKeyboardScreen(this, "World Name", worldName, 32,
                    new LegacyKeyboardScreen.Callback() {
                        public void accept(String value) { if (value.length() > 0) worldName = value; }
                    }));
        }
        if (button.id == 2) {
            mc.displayGuiScreen(new LegacyKeyboardScreen(this, "World Seed", seedText, 32,
                    new LegacyKeyboardScreen.Callback() {
                        public void accept(String value) { seedText = value; }
                    }));
        }
        if (button.id == 3) mode = (mode + 1) % 3;
        if (button.id == 4) flat = !flat;
        if (button.id == 5) structures = !structures;
        if (button.id == 6) createWorld();
        if (button.id == 0) goBack();
        updateLabels();
    }

    @Override
    public void onControllerButton(PadButton button) {
        if (button == PadButton.DPAD_LEFT || button == PadButton.DPAD_RIGHT) {
            GuiButton focused = getFocusedButton();
            if (focused != null && focused.id >= 3 && focused.id <= 5) {
                actionPerformed(focused);
            } else {
                super.onControllerButton(button);
            }
        } else {
            super.onControllerButton(button);
        }
    }

    private void createWorld() {
        long seed = new Random().nextLong();
        if (seedText != null && seedText.length() > 0) {
            try {
                long parsed = Long.parseLong(seedText);
                if (parsed != 0L) seed = parsed;
            } catch (NumberFormatException ignored) {
                seed = seedText.hashCode();
            }
        }
        int gameType = mode == 1 ? 1 : 0;
        boolean hardcore = mode == 2;
        WorldType worldType = flat ? WorldType.FLAT : WorldType.DEFAULT;
        WorldSettings settings = new WorldSettings(seed, gameType, structures, hardcore, worldType);
        String folder = GuiCreateWorld.func_25097_a(mc.getSaveLoader(), worldName);
        mc.playerController = gameType == 1 ? new PlayerControllerCreative(mc) : new PlayerControllerSP(mc);
        mc.displayGuiScreen(null);
        mc.startWorld(folder, worldName, settings);
        // The first null screen is required by vanilla before loading; now that a world exists,
        // the second call closes the temporary main menu instead of creating another one.
        mc.displayGuiScreen(null);
    }

    private void updateLabels() {
        setLabel(1, "World Name: " + worldName);
        setLabel(2, "Seed: " + (seedText.length() == 0 ? "Random" : seedText));
        String[] modes = {"Survival", "Creative", "Hardcore"};
        setLabel(3, "Game Mode: " + modes[mode]);
        setLabel(4, "World Type: " + (flat ? "Superflat" : "Default"));
        setLabel(5, "Generate Structures: " + (structures ? "Yes" : "No"));
    }

    private void setLabel(int id, String label) {
        for (Object object : controlList) {
            GuiButton button = (GuiButton) object;
            if (button.id == id) button.displayString = label;
        }
    }
}
