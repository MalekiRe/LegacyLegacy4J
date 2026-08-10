package wily.legacy125.client.screen;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

public final class LegacyProfileScreen extends LegacyPanelScreen {
    private String username;

    public LegacyProfileScreen(GuiScreen parent) {
        super(parent, "Change Profile / Skin");
    }

    @Override
    protected void addLegacyControls() {
        username = mc.session.username;
        int x = panelLeft + 30;
        int w = panelRight - panelLeft - 60;
        int y = panelTop + 62;
        addButton(1, x, y, w, "Username: " + username);
        addButton(0, x, y + 30, w, "Done");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            mc.displayGuiScreen(new LegacyKeyboardScreen(this, "Profile Name", username, 16,
                    new LegacyKeyboardScreen.Callback() {
                        public void accept(String value) {
                            if (value.length() > 0) {
                                username = value;
                                mc.session.username = value;
                            }
                        }
                    }));
        }
        if (button.id == 0) goBack();
    }

    @Override
    protected void drawScreenContents(int mouseX, int mouseY, float partialTick) {
        fontRenderer.drawSplitString("Minecraft 1.2.5 downloads the player skin associated with the active username. "
                        + "Changing this value applies to newly opened worlds and offline profiles.",
                panelLeft + 24, panelTop + 35, panelRight - panelLeft - 48, 0xFFFFFFFF);
    }
}
