package wily.legacy125.client.screen;

import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import wily.legacy125.input.PadButton;

/** Explicit opt-in panel used by one settings category. */
final class LegacyOptionSectionScreen extends LegacyPanelScreen {
    private final GameSettings settings;
    private final int section;

    LegacyOptionSectionScreen(GuiScreen parent, GameSettings settings, int section) {
        super(parent, sectionTitle(section));
        this.settings = settings;
        this.section = section;
    }

    @Override
    protected void addLegacyControls() {
        int panelHeight = section == 2 ? 88 : section == 4 ? 138 : section == 3 ? 162 : 110;
        panelLeft = (width - 180) / 2;
        panelRight = panelLeft + 180;
        panelTop = section == 4 ? 50 : (height - panelHeight) / 2 + (section == 2 ? -12 : 0);
        panelBottom = panelTop + panelHeight;
        int x = panelLeft + 10;
        int y = panelTop + 32;
        int w = 160;
        if (section == 1) addButton(1, x, y, w, 18, "");
        if (section == 2) {
            addButton(2, x, y, w, 18, "");
            addButton(3, x, y + 22, w, 18, "");
        }
        if (section == 3) {
            addButton(4, x, y, w, 18, "");
            addButton(5, x, y + 22, w, 18, "");
        }
        if (section == 4) addButton(5, x, y, w, 18, "");
        addButton(0, x, panelBottom - 28, w, 18, "Back");
        updateLabels();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            goBack();
            return;
        }
        adjust(button.id, 1);
    }

    @Override
    public void onControllerButton(PadButton button) {
        if (button == PadButton.DPAD_LEFT || button == PadButton.DPAD_RIGHT) {
            GuiButton focused = getFocusedButton();
            if (focused != null && focused.id != 0) {
                adjust(focused.id, button == PadButton.DPAD_LEFT ? -1 : 1);
                return;
            }
        }
        super.onControllerButton(button);
    }

    private void adjust(int id, int direction) {
        if (id == 1) settings.difficulty = wrap(settings.difficulty + direction, 4);
        if (id == 2) settings.musicVolume = clamp(settings.musicVolume + direction * 0.1F);
        if (id == 3) settings.soundVolume = clamp(settings.soundVolume + direction * 0.1F);
        if (id == 4) settings.gammaSetting = clamp(settings.gammaSetting + direction * 0.1F);
        if (id == 5) settings.guiScale = wrap(settings.guiScale + direction, 4);
        settings.saveOptions();
        updateLabels();
    }

    private void updateLabels() {
        String[] difficulties = {"Peaceful", "Easy", "Normal", "Hard"};
        String[] scales = {"Auto", "Small", "Normal", "Large"};
        setLabel(1, "Difficulty: " + difficulties[wrap(settings.difficulty, 4)]);
        setLabel(2, "Music: " + Math.round(settings.musicVolume * 100.0F) + "%");
        setLabel(3, "Sound: " + Math.round(settings.soundVolume * 100.0F) + "%");
        setLabel(4, "Gamma: " + Math.round(settings.gammaSetting * 100.0F) + "%");
        setLabel(5, "Interface Scale: " + scales[wrap(settings.guiScale, 4)]);
    }

    private void setLabel(int id, String value) {
        for (Object object : controlList) {
            GuiButton button = (GuiButton) object;
            if (button.id == id) button.displayString = value;
        }
    }

    private static String sectionTitle(int section) {
        if (section == 1) return "Game Options";
        if (section == 2) return "Audio";
        if (section == 3) return "Graphics";
        return "User Interface";
    }

    private static int wrap(int value, int count) {
        int result = value % count;
        return result < 0 ? result + count : result;
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
