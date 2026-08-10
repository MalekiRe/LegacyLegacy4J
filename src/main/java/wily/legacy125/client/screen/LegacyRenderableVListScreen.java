package wily.legacy125.client.screen;

import net.minecraft.src.GuiScreen;
import wily.legacy125.api.LegacyUIAccessor125;
import wily.legacy125.api.LegacyUIDefinitions125;

/** Upstream-style free-standing SD list: logo/background, list, then tooltips. */
public abstract class LegacyRenderableVListScreen extends LegacyScreen {
    protected LegacyUIAccessor125 listAccessor;
    private int nextListRow;

    protected LegacyRenderableVListScreen(GuiScreen parent, String title) {
        super(parent, title);
    }

    @Override
    protected final void addLegacyControls() {
        listAccessor = LegacyUIDefinitions125.renderableList(width);
        nextListRow = 0;
        addListControls();
    }

    protected abstract void addListControls();

    protected LegacyButton addListButton(int id, String text) {
        int row = nextListRow++;
        return addButton(id, listAccessor.getInteger("renderableVList.x"),
                listAccessor.getInteger("renderableVList.y")
                        + row * listAccessor.getInteger("renderableVList.pitch"),
                listAccessor.getInteger("renderableVList.width"),
                listAccessor.getInteger("buttonsHeight"), text);
    }

    @Override
    protected boolean drawPanel() {
        return false;
    }

    @Override
    protected void drawScreenContents(int mouseX, int mouseY, float partialTick) {
        drawMinecraftLogo();
    }

    @Override
    protected void drawControlHints() {
        LegacyTheme.image(mc, "/legacy4j125/gui/esc_key.png", 33, height - 22, 8, 8);
        LegacyTheme.drawControlHint(mc, "Back", 42, height - 20);
    }
}
