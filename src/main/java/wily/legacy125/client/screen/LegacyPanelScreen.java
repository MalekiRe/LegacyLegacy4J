package wily.legacy125.client.screen;

import net.minecraft.src.GuiScreen;

/** Opt-in panel screen, corresponding to upstream PanelBackgroundScreen. */
public abstract class LegacyPanelScreen extends LegacyScreen {
    protected LegacyPanelScreen(GuiScreen parent, String title) {
        super(parent, title);
    }

    @Override
    protected final boolean drawPanel() {
        return true;
    }
}
