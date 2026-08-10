package wily.legacy125.client.screen;

import net.minecraft.src.GuiButton;
import net.minecraft.src.ServerNBTStorage;

final class LegacyServerEditScreen extends LegacyPanelScreen {
    private final LegacyMultiplayerScreen multiplayer;
    private final ServerNBTStorage original;
    private final boolean direct;
    private String name;
    private String host;

    LegacyServerEditScreen(LegacyMultiplayerScreen parent, ServerNBTStorage original, boolean direct) {
        super(parent, direct ? "Direct Connect" : original == null ? "Add Server" : "Edit Server");
        this.multiplayer = parent;
        this.original = original;
        this.direct = direct;
        this.name = original == null ? "Minecraft Server" : original.name;
        this.host = original == null ? "" : original.host;
    }

    @Override
    protected void addLegacyControls() {
        int x = panelLeft + 30;
        int w = panelRight - panelLeft - 60;
        int y = panelTop + 62;
        if (!direct) addButton(1, x, y, w, "");
        addButton(2, x, y + (direct ? 0 : 28), w, "");
        addButton(3, x, y + (direct ? 35 : 63), w / 2 - 4, direct ? "Connect" : "Save");
        addButton(0, x + w / 2 + 4, y + (direct ? 35 : 63), w / 2 - 4, "Cancel");
        updateLabels();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) mc.displayGuiScreen(new LegacyKeyboardScreen(this, "Server Name", name, 32,
                new LegacyKeyboardScreen.Callback() {
                    public void accept(String value) { if (value.length() > 0) name = value; }
                }));
        if (button.id == 2) mc.displayGuiScreen(new LegacyKeyboardScreen(this, "Server Address", host, 64,
                new LegacyKeyboardScreen.Callback() {
                    public void accept(String value) { host = value; }
                }));
        if (button.id == 3 && host.trim().length() > 0) multiplayer.commit(original, name, host, direct);
        if (button.id == 0) goBack();
        updateLabels();
    }

    private void updateLabels() {
        setLabel(1, "Server Name: " + name);
        setLabel(2, "Address: " + (host.length() == 0 ? "Select to enter" : host));
    }

    private void setLabel(int id, String label) {
        for (Object object : controlList) {
            GuiButton button = (GuiButton) object;
            if (button.id == id) button.displayString = label;
        }
    }
}
