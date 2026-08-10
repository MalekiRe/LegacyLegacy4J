package wily.legacy125.client.screen;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.PlayerControllerCreative;
import net.minecraft.src.PlayerControllerSP;
import net.minecraft.src.SaveFormatComparator;
import wily.legacy125.api.LegacyUIAccessor125;
import wily.legacy125.api.LegacyUIDefinitions125;
import wily.legacy125.input.PadButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Three-list Load/Create/Join screen with upstream tab-panel composition. */
public final class LegacyPlayScreen extends LegacyScreen {
    private static final int SAVE_BUTTON_BASE = 100;
    private List<SaveFormatComparator> saves = Collections.emptyList();
    private LegacyUIAccessor125 accessor;
    private int selectedTab;

    public LegacyPlayScreen(GuiScreen parent) {
        super(parent, "Play Game");
    }

    @Override
    protected void addLegacyControls() {
        accessor = LegacyUIDefinitions125.playGame(width, height);
        panelLeft = accessor.getInteger("panel.x");
        panelTop = accessor.getInteger("panel.y");
        panelRight = panelLeft + accessor.getInteger("panel.width");
        panelBottom = panelTop + accessor.getInteger("panel.height");
        saves = new ArrayList<SaveFormatComparator>(mc.getSaveLoader().getSaveList());
        Collections.sort(saves);
        String visualWorld = System.getProperty("legacy4j.visualWorld");
        if (visualWorld != null && visualWorld.length() > 0) {
            List<SaveFormatComparator> fixtureOnly = new ArrayList<SaveFormatComparator>();
            for (SaveFormatComparator save : saves) {
                if (visualWorld.equals(save.getFileName())) fixtureOnly.add(save);
            }
            saves = fixtureOnly;
        }
        rebuildActiveList();
    }

    private void rebuildActiveList() {
        controlList.clear();
        int x = accessor.getInteger("renderableVList.x");
        int y = accessor.getInteger("renderableVList.y");
        int w = accessor.getInteger("renderableVList.width");
        int h = accessor.getInteger("buttonsHeight");
        if (selectedTab == 0) {
            int visible = Math.min(7, saves.size());
            for (int i = 0; i < visible; i++) {
                SaveFormatComparator save = saves.get(i);
                String name = save.getDisplayName();
                if (name == null || name.trim().length() == 0) name = save.getFileName();
                controlList.add(new LegacyWorldButton(SAVE_BUTTON_BASE + i,
                        x, y + i * h, w, h, name));
            }
        } else if (selectedTab == 1) {
            addButton(10, x, y, w, h, "Create New World");
        } else {
            addButton(11, x, y, w, h, "Join Server");
            addButton(12, x, y + h, w, h, "Direct Connect");
        }
        setFocusedIndex(0);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= SAVE_BUTTON_BASE) loadSave(button.id - SAVE_BUTTON_BASE);
        if (button.id == 10) mc.displayGuiScreen(new LegacyCreateWorldScreen(this));
        if (button.id == 11 || button.id == 12) mc.displayGuiScreen(new LegacyMultiplayerScreen(this));
    }

    private void loadSave(int index) {
        if (index < 0 || index >= saves.size()) return;
        SaveFormatComparator save = saves.get(index);
        mc.playerController = save.getGameType() == 0
                ? new PlayerControllerSP(mc) : new PlayerControllerCreative(mc);
        mc.displayGuiScreen(null);
        mc.startWorld(save.getFileName(), save.getDisplayName(), null);
        mc.displayGuiScreen(null);
    }

    private LegacyTabList125 tabs() {
        LegacyTabList125 tabs = new LegacyTabList125();
        String[] labels = {"Load", "Create", "Join"};
        int x = panelLeft;
        int y = accessor.getInteger("tabList.y");
        int tabWidth = accessor.getInteger("tabList.buttonWidth");
        for (int i = 0; i < labels.length; i++) {
            tabs.add(new LegacyTabButton125(x + i * tabWidth, y, tabWidth,
                    accessor.getInteger("tabList.height"), i, labels.length, labels[i], 1.4F));
        }
        tabs.setSelectedIndex(selectedTab);
        return tabs;
    }

    @Override
    protected void drawBehindPanel(int mouseX, int mouseY, float partialTick) {
        tabs().renderUnselected(mc);
    }

    @Override
    protected void drawPanelLayer(int mouseX, int mouseY, float partialTick) {
        LegacyTheme.panel(mc, panelLeft, panelTop, panelRight, panelBottom);
    }

    @Override
    protected boolean drawPanel() {
        return true;
    }

    @Override
    protected void drawAbovePanel(int mouseX, int mouseY, float partialTick) {
        tabs().renderSelected(mc);
        int x = accessor.getInteger("panelRecess.x");
        int y = accessor.getInteger("panelRecess.y");
        LegacyTheme.squareRecess(mc, x, y, x + accessor.getInteger("panelRecess.width"),
                y + accessor.getInteger("panelRecess.height"));
    }

    @Override
    protected void drawScreenContents(int mouseX, int mouseY, float partialTick) {
        if (selectedTab == 0 && saves.isEmpty()) {
            LegacyTheme.drawCenteredString(mc, "No saved worlds yet", width / 2,
                    accessor.getInteger("renderableVList.y") + 12, LegacyTheme.TEXT, false);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            int tabY = accessor.getInteger("tabList.y");
            int tabWidth = accessor.getInteger("tabList.buttonWidth");
            if (mouseY >= tabY && mouseY < tabY + accessor.getInteger("tabList.height")
                    && mouseX >= panelLeft && mouseX < panelRight) {
                setTab(Math.min(2, (mouseX - panelLeft) / tabWidth));
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onControllerButton(PadButton button) {
        if (button == PadButton.LEFT_BUMPER) setTab((selectedTab + 2) % 3);
        else if (button == PadButton.RIGHT_BUMPER) setTab((selectedTab + 1) % 3);
        else if (button == PadButton.X && selectedTab == 0) deleteFocusedSave();
        else super.onControllerButton(button);
    }

    private void setTab(int tab) {
        selectedTab = Math.max(0, Math.min(2, tab));
        rebuildActiveList();
    }

    private void deleteFocusedSave() {
        final int index = focusedSaveIndex();
        if (index < 0) return;
        final SaveFormatComparator save = saves.get(index);
        mc.displayGuiScreen(new LegacyConfirmationScreen(this, "Delete Save",
                "Delete '" + save.getDisplayName() + "'? This cannot be undone.",
                new LegacyConfirmationScreen.Callback() {
                    public void answer(boolean confirmed) {
                        if (confirmed) mc.getSaveLoader().deleteWorldDirectory(save.getFileName());
                        mc.displayGuiScreen(new LegacyPlayScreen(parent));
                    }
                }));
    }

    private int focusedSaveIndex() {
        GuiButton focused = getFocusedButton();
        if (focused == null || focused.id < SAVE_BUTTON_BASE) return -1;
        int index = focused.id - SAVE_BUTTON_BASE;
        return index < saves.size() ? index : -1;
    }

    @Override
    protected void drawControlHints() {
        String hint = selectedTab == 0 ? "LB/RB Tab   A Select   X Delete   B Back"
                : "LB/RB Tab   A Select   B Back";
        LegacyTheme.drawControlHint(mc, hint, 32, height - 20);
    }
}
