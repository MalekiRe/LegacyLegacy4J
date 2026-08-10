package wily.legacy125.client.screen;

import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

/** Legacy4J Help & Options landing list (not an option-values panel). */
public final class LegacyOptionsScreen extends LegacyRenderableVListScreen {
    private final GameSettings settings;

    public LegacyOptionsScreen(GuiScreen parent, GameSettings settings) {
        super(parent, "Help & Options");
        this.settings = settings;
    }

    @Override
    protected void addListControls() {
        addListButton(1, "Change Skin");
        addListButton(2, "How To Play");
        addListButton(3, "Controls");
        addListButton(4, "Settings");
        addListButton(5, "Credits");
        addListButton(6, "Reinstall Content");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) mc.displayGuiScreen(new LegacyProfileScreen(this));
        if (button.id == 2) mc.displayGuiScreen(LegacyTextScreen.howToPlay(this));
        if (button.id == 3) mc.displayGuiScreen(new LegacyControllerSettingsScreen(this));
        if (button.id == 4) mc.displayGuiScreen(new LegacySettingsScreen(this, settings));
        if (button.id == 5) mc.displayGuiScreen(LegacyTextScreen.credits(this));
        if (button.id == 6) mc.displayGuiScreen(new LegacyConfirmationScreen(this,
                "Reinstall Content", "Legacy4J resources are bundled into this backport. Rebuild the mod jar to reinstall them.",
                new LegacyConfirmationScreen.Callback() {
                    public void answer(boolean confirmed) {
                        mc.displayGuiScreen(LegacyOptionsScreen.this);
                    }
                }));
    }
}
