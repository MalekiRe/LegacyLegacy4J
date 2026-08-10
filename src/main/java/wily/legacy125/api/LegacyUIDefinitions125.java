package wily.legacy125.api;

/** Named SD measurements copied from Legacy4J's 480p UI definitions. */
public final class LegacyUIDefinitions125 {
    private LegacyUIDefinitions125() {
    }

    public static LegacyUIAccessor125 renderableList(int screenWidth) {
        int listWidth = 135;
        return new LegacyUIAccessor125()
                .putInteger("buttonsHeight", 18)
                .putInteger("renderableVList.width", listWidth)
                .putInteger("renderableVList.x", (screenWidth - listWidth) / 2)
                .putInteger("renderableVList.y", 55)
                .putInteger("renderableVList.pitch", 22);
    }

    public static LegacyUIAccessor125 playGame(int screenWidth, int screenHeight) {
        int panelWidth = 201;
        int panelHeight = 153;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = (screenHeight - panelHeight) / 2;
        return new LegacyUIAccessor125()
                .putInteger("panel.width", panelWidth)
                .putInteger("panel.height", panelHeight)
                .putInteger("panel.x", panelX)
                .putInteger("panel.y", panelY)
                .putInteger("buttonsHeight", 17)
                .putInteger("tabList.height", 21)
                .putInteger("tabList.y", panelY - 15)
                .putInteger("tabList.buttonWidth", 67)
                .putInteger("panelRecess.x", panelX + 9)
                .putInteger("panelRecess.y", panelY + 9)
                .putInteger("panelRecess.width", panelWidth - 18)
                .putInteger("panelRecess.height", panelHeight - 18)
                .putInteger("renderableVList.x", panelX + 15)
                .putInteger("renderableVList.y", panelY + 15)
                .putInteger("renderableVList.width", panelWidth - 30)
                .putInteger("renderableVList.height", panelHeight - 30);
    }

    public static LegacyUIAccessor125 crafting() {
        return new LegacyUIAccessor125()
                .putInteger("imageWidth", 253)
                .putInteger("imageHeight", 121)
                .putInteger("tabYOffset", 11)
                .putInteger("tabList.buttonWidth", 37)
                .putInteger("tabList.height", 29)
                .putInteger("tabList.y", -23)
                .putInteger("tabList.step", 36)
                .putInteger("tabList.icon.x", 10)
                .putInteger("tabList.icon.y", 6)
                .putInteger("typeTabList.x", -18)
                .putInteger("typeTabList.y", 4)
                .putInteger("typeTabList.buttonWidth", 24)
                .putInteger("typeTabList.height", 23)
                .putInteger("bottomPanel.y", 44)
                .putInteger("bottomPanel.height", 71)
                .putInteger("craftingGridPanel.x", 7)
                .putInteger("craftingGridPanel.width", 117)
                .putInteger("inventoryPanel.x", 128)
                .putInteger("inventoryPanel.width", 117)
                .putInteger("craftingButtons.x", 6)
                .putInteger("craftingButtons.y", 18)
                .putInteger("craftingButtons.size", 20)
                .putInteger("craftingButtons.count", 12)
                .putInteger("craftingTitle.y", 7)
                .putInteger("inventoryTitle.x", 186)
                .putInteger("inventoryTitle.y", 47)
                .putInteger("craftingGridSlot.x", 17)
                .putInteger("craftingGridSlot.y", 64)
                .putInteger("craftingGridSlot.size", 16)
                .putInteger("craftingArrow.x", 63)
                .putInteger("craftingArrow.y", 81)
                .putInteger("resultSlot.x", 87)
                .putInteger("resultSlot.y", 76)
                .putInteger("inventory.x", 137)
                .putInteger("inventory.y", 60)
                .putInteger("quick_select.y", 97)
                .putInteger("inventory.slotSize", 11);
    }
}
