package wily.legacy125.client.screen;

import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiConnecting;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.ServerNBTStorage;
import wily.legacy125.input.PadButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class LegacyMultiplayerScreen extends LegacyPanelScreen {
    private static final int SERVER_BASE = 100;
    private final List<ServerNBTStorage> servers = new ArrayList<ServerNBTStorage>();
    private int page;
    private int rows;

    public LegacyMultiplayerScreen(GuiScreen parent) {
        super(parent, "Multiplayer");
    }

    @Override
    protected void addLegacyControls() {
        loadServers();
        rows = Math.max(2, Math.min(6, (panelBottom - panelTop - 88) / 23));
        rebuild();
    }

    private void rebuild() {
        controlList.clear();
        int x = panelLeft + 14;
        int y = panelTop + 34;
        int start = page * rows;
        for (int row = 0; row < rows && start + row < servers.size(); row++) {
            ServerNBTStorage server = servers.get(start + row);
            addButton(SERVER_BASE + start + row, x, y + row * 23, panelRight - panelLeft - 28,
                    server.name + "  [" + server.host + "]");
        }
        int bottom = panelBottom - 47;
        addButton(10, x, bottom, 105, "Add Server");
        addButton(11, x + 111, bottom, 105, "Direct Connect");
        addButton(0, panelRight - 104, bottom, 90, "Back");
        if (servers.size() > rows) {
            addButton(12, panelRight - 104, panelTop + 34, 42, "<").enabled = page > 0;
            addButton(13, panelRight - 57, panelTop + 34, 42, ">").enabled = (page + 1) * rows < servers.size();
        }
        setFocusedIndex(0);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= SERVER_BASE) connect(servers.get(button.id - SERVER_BASE).host);
        if (button.id == 10) mc.displayGuiScreen(new LegacyServerEditScreen(this, null, false));
        if (button.id == 11) mc.displayGuiScreen(new LegacyServerEditScreen(this,
                new ServerNBTStorage("Direct Connect", ""), true));
        if (button.id == 0) goBack();
        if (button.id == 12 && page > 0) { page--; rebuild(); }
        if (button.id == 13 && (page + 1) * rows < servers.size()) { page++; rebuild(); }
    }

    @Override
    public void onControllerButton(PadButton button) {
        final int index = focusedServerIndex();
        if (button == PadButton.X && index >= 0) {
            final ServerNBTStorage server = servers.get(index);
            mc.displayGuiScreen(new LegacyConfirmationScreen(this, "Delete Server",
                    "Remove '" + server.name + "' from the server list?", new LegacyConfirmationScreen.Callback() {
                public void answer(boolean confirmed) {
                    if (confirmed) {
                        servers.remove(server);
                        saveServers();
                    }
                    mc.displayGuiScreen(new LegacyMultiplayerScreen(parent));
                }
            }));
        } else if (button == PadButton.Y && index >= 0) {
            mc.displayGuiScreen(new LegacyServerEditScreen(this, servers.get(index), false));
        } else {
            super.onControllerButton(button);
        }
    }

    void commit(ServerNBTStorage original, String name, String host, boolean direct) {
        if (direct) {
            connect(host);
            return;
        }
        if (original == null) {
            original = new ServerNBTStorage(name, host);
            servers.add(original);
        } else {
            original.name = name;
            original.host = host;
        }
        saveServers();
        mc.displayGuiScreen(new LegacyMultiplayerScreen(parent));
    }

    private void connect(String address) {
        String host = address == null ? "" : address.trim();
        int port = 25565;
        int separator = host.lastIndexOf(':');
        if (separator > 0 && separator == host.indexOf(':')) {
            try {
                port = Integer.parseInt(host.substring(separator + 1));
                host = host.substring(0, separator);
            } catch (NumberFormatException ignored) {
            }
        }
        if (host.length() > 0) mc.displayGuiScreen(new GuiConnecting(mc, host, port));
    }

    private int focusedServerIndex() {
        GuiButton button = getFocusedButton();
        if (button == null || button.id < SERVER_BASE) return -1;
        int index = button.id - SERVER_BASE;
        return index < servers.size() ? index : -1;
    }

    private void loadServers() {
        servers.clear();
        File file = new File(mc.mcDataDir, "servers.dat");
        if (!file.isFile()) return;
        try {
            NBTTagCompound root = CompressedStreamTools.read(file);
            if (root == null) return;
            NBTTagList list = root.getTagList("servers");
            for (int index = 0; index < list.tagCount(); index++) {
                servers.add(ServerNBTStorage.createServerNBTStorage((NBTTagCompound) list.tagAt(index)));
            }
        } catch (Exception ignored) {
        }
    }

    private void saveServers() {
        NBTTagList list = new NBTTagList("servers");
        for (ServerNBTStorage server : servers) list.appendTag(server.getCompoundTag());
        NBTTagCompound root = new NBTTagCompound();
        root.setTag("servers", list);
        CompressedStreamTools.safeWrite(root, new File(mc.mcDataDir, "servers.dat"));
    }

    @Override
    protected void drawScreenContents(int mouseX, int mouseY, float partialTick) {
        if (servers.isEmpty()) drawCenteredString(fontRenderer, "No saved servers", width / 2,
                panelTop + 60, 0xFFD7D7D7);
    }

    @Override
    protected void drawControlHints() {
        LegacyTheme.drawCenteredControlHint(mc, "A Join     X Delete     Y Edit     B Back",
                width / 2, height - 14);
    }
}
