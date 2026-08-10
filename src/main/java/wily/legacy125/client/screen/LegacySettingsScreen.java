package wily.legacy125.client.screen;

import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

/** Upstream Settings category list; individual categories own their panels. */
public final class LegacySettingsScreen extends LegacyRenderableVListScreen {
    private final GameSettings settings;

    public LegacySettingsScreen(GuiScreen parent, GameSettings settings) {
        super(parent, "Settings");
        this.settings = settings;
    }

    @Override
    protected void addListControls() {
        addListButton(1, "Game Options");
        addListButton(2, "Audio");
        addListButton(3, "Graphics");
        addListButton(4, "User Interface");
        addListButton(5, "Reset Defaults");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= 1 && button.id <= 4) {
            mc.displayGuiScreen(new LegacyOptionSectionScreen(this, settings, button.id));
        } else if (button.id == 5) {
            mc.displayGuiScreen(new LegacyConfirmationScreen(this, "Reset Defaults",
                    "Reset the backported Legacy4J display and audio options?",
                    new LegacyConfirmationScreen.Callback() {
                        public void answer(boolean confirmed) {
                            if (confirmed) {
                                settings.musicVolume = 1.0F;
                                settings.soundVolume = 1.0F;
                                settings.gammaSetting = 0.0F;
                                settings.guiScale = 0;
                                settings.difficulty = 2;
                                settings.saveOptions();
                            }
                            mc.displayGuiScreen(LegacySettingsScreen.this);
                        }
                    }));
        }
    }
}
