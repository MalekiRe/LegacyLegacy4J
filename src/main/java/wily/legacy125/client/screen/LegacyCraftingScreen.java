package wily.legacy125.client.screen;

import net.minecraft.src.Block;
import net.minecraft.src.Container;
import net.minecraft.src.ContainerPlayer;
import net.minecraft.src.ContainerWorkbench;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.IRecipe;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Item;
import net.minecraft.src.RenderHelper;
import net.minecraft.src.Slot;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import wily.legacy125.api.LegacyCraftingTab;
import wily.legacy125.api.LegacyCraftingTabs;
import wily.legacy125.api.LegacyUIAccessor125;
import wily.legacy125.api.LegacyUIDefinitions125;
import wily.legacy125.input.PadButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Legacy Console recipe book backed by Minecraft's real player/workbench container. */
public final class LegacyCraftingScreen extends LegacyGuiContainer125 implements LegacyControllerScreen {
    private static final int RECIPE_TILES = 12;
    private static final int TOP_TABS = 7;
    private final LegacyUIAccessor125 accessor = LegacyUIDefinitions125.crafting();
    private final InventoryCrafting craftMatrix;
    private final int gridWidth;
    private final int gridHeight;
    private final List<LegacyRecipe> recipes = new ArrayList<LegacyRecipe>();
    private final int[] originalX;
    private final int[] originalY;
    private int categoryIndex;
    private int groupIndex;
    private int variantIndex;
    private int inventoryIndex;
    private boolean inventoryMode;
    private boolean craftableOnly;
    private boolean slotsArranged;
    private String status = "";
    private int statusTicks;

    public LegacyCraftingScreen(Container container) {
        super(container);
        allowUserInput = true;
        xSize = accessor.getInteger("imageWidth");
        ySize = accessor.getInteger("imageHeight");
        if (container instanceof ContainerWorkbench) {
            craftMatrix = ((ContainerWorkbench) container).craftMatrix;
            gridWidth = 3;
            gridHeight = 3;
        } else if (container instanceof ContainerPlayer) {
            craftMatrix = ((ContainerPlayer) container).craftMatrix;
            gridWidth = 2;
            gridHeight = 2;
        } else {
            throw new IllegalArgumentException("Legacy crafting requires a crafting container");
        }
        originalX = new int[container.inventorySlots.size()];
        originalY = new int[container.inventorySlots.size()];
        for (int i = 0; i < container.inventorySlots.size(); i++) {
            Slot slot = (Slot) container.inventorySlots.get(i);
            originalX[i] = slot.xDisplayPosition;
            originalY[i] = slot.yDisplayPosition;
        }
        loadRecipes();
    }

    /** True only for the crafting view opened directly from the player's inventory. */
    public boolean isPlayerCrafting() {
        return gridWidth == 2 && gridHeight == 2;
    }

    @Override
    public void initGui() {
        super.initGui();
        guiLeft += 21;
        guiTop += accessor.getInteger("tabYOffset");
        arrangeSlots();
        inventoryIndex = firstInventorySlot();
        normalizeSelection();
    }

    @Override
    public void onGuiClosed() {
        restoreSlots();
        super.onGuiClosed();
    }

    @Override
    public void drawDefaultBackground() {
        // Legacy Console keeps the world visible behind this opaque center panel.
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (statusTicks > 0) statusTicks--;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        net.minecraft.src.RenderItem previous = itemRenderer;
        itemRenderer = LegacyScaledRenderItem.INSTANCE;
        LegacyScaledRenderItem.begin(this);
        try {
            super.drawScreen(mouseX, mouseY, partialTick);
        } finally {
            LegacyScaledRenderItem.end();
            itemRenderer = previous;
        }
    }

    @Override
    public int legacySlotSize(int x, int y) {
        for (Object value : inventorySlots.inventorySlots) {
            Slot slot = (Slot) value;
            if (visibleSlot(slot) && slot.xDisplayPosition == x && slot.yDisplayPosition == y) return 11;
        }
        return 16;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        LegacyTabList125 categoryTabs = categoryTabs();
        categoryTabs.renderUnselected(mc);
        LegacyTheme.panel(mc, guiLeft, guiTop, guiLeft + xSize, guiTop + ySize);
        categoryTabs.renderSelected(mc);
        drawSelectedTypeTab();
        int bottomY = accessor.getInteger("bottomPanel.y");
        int bottomHeight = accessor.getInteger("bottomPanel.height");
        int gridX = accessor.getInteger("craftingGridPanel.x");
        int gridWidth = accessor.getInteger("craftingGridPanel.width");
        int inventoryX = accessor.getInteger("inventoryPanel.x");
        int inventoryWidth = accessor.getInteger("inventoryPanel.width");
        LegacyTheme.squareRecess(mc, guiLeft + gridX, guiTop + bottomY,
                guiLeft + gridX + gridWidth, guiTop + bottomY + bottomHeight);
        LegacyTheme.squareRecess(mc, guiLeft + inventoryX, guiTop + bottomY,
                guiLeft + inventoryX + inventoryWidth, guiTop + bottomY + bottomHeight);
        drawSlotHolders();
    }

    private void drawSlotHolders() {
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        for (Object value : inventorySlots.inventorySlots) {
            Slot slot = (Slot) value;
            if (!visibleSlot(slot)) continue;
            LegacyTheme.slot(mc, guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, 11);
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderHelper.disableStandardItemLighting();
    }

    private LegacyTabList125 categoryTabs() {
        List<LegacyCraftingTab> tabs = availableTabs();
        int start = tabWindowStart(tabs.size());
        int count = Math.min(TOP_TABS, tabs.size() - start);
        LegacyTabList125 result = new LegacyTabList125();
        for (int visible = 0; visible < count; visible++) {
            int index = start + visible;
            int x = guiLeft + visible * accessor.getInteger("tabList.step");
            int y = guiTop + accessor.getInteger("tabList.y");
            result.add(new LegacyTabButton125(x, y,
                    accessor.getInteger("tabList.buttonWidth"), accessor.getInteger("tabList.height"),
                    visible, TOP_TABS, false, tabs.get(index).iconTexture,
                    accessor.getInteger("tabList.icon.x"), accessor.getInteger("tabList.icon.y"),
                    0.0F, 2.5F));
        }
        result.setSelectedIndex(categoryIndex - start);
        return result;
    }

    private void drawSelectedTypeTab() {
        int x = guiLeft + accessor.getInteger("typeTabList.x");
        int y = guiTop + accessor.getInteger("typeTabList.y");
        LegacyTheme.verticalTab(mc, x, y, accessor.getInteger("typeTabList.buttonWidth"),
                accessor.getInteger("typeTabList.height"), true);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, new ItemStack(Block.workbench),
                x + 4, y + 3);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        List<LegacyCraftingTab> tabs = availableTabs();
        LegacyCraftingTab category = tabs.get(clamp(categoryIndex, 0, tabs.size() - 1));
        String recipeLabel = category.title + (craftableOnly ? " - Available" : "");
        LegacyTheme.drawCenteredString(mc, trim(recipeLabel, 27), xSize / 2,
                accessor.getInteger("craftingTitle.y"),
                LegacyTheme.MUTED_TEXT, false);
        LegacyTheme.drawCenteredString(mc, "Inventory", accessor.getInteger("inventoryTitle.x"),
                accessor.getInteger("inventoryTitle.y"),
                LegacyTheme.MUTED_TEXT, false);
        drawRecipeStrip();
        drawSelectedRecipe();
        drawSelection();
        String hints = inventoryMode
                ? "A Pick Up  X Split  Y Move  LS Recipes  B Close"
                : "LB/RB Tab  LT/RT Type  A Craft  Y Filter  LS Inv  B Close";
        LegacyTheme.drawCenteredControlHint(mc, hints, xSize / 2, ySize + 24);
        if (statusTicks > 0) LegacyTheme.drawCenteredString(mc, status,
                xSize / 2, ySize + 14, 0xFFFFFF55, true);
    }

    private void drawRecipeStrip() {
        List<LegacyRecipeGroup> groups = visibleGroups();
        if (groups.isEmpty()) {
            LegacyTheme.drawString(mc, "No matching recipes", 8, 25, LegacyTheme.MUTED_TEXT);
            return;
        }
        groupIndex = clamp(groupIndex, 0, groups.size() - 1);
        int start = Math.max(0, Math.min(groupIndex - RECIPE_TILES + 1, groups.size() - RECIPE_TILES));
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        for (int tile = 0; tile < RECIPE_TILES && start + tile < groups.size(); tile++) {
            int index = start + tile;
            int x = accessor.getInteger("craftingButtons.x")
                    + tile * accessor.getInteger("craftingButtons.size");
            boolean selected = !inventoryMode && index == groupIndex;
            LegacyRecipeGroup group = groups.get(index);
            LegacyRecipe recipe = group.selected(index == groupIndex ? variantIndex : 0);
            int recipeY = accessor.getInteger("craftingButtons.y");
            LegacyTheme.recipeSlot(mc, x, recipeY, recipe.hasIngredients(playerInventory()));
            if (selected) {
                LegacyTheme.recipeFocus(mc, x, recipeY);
                if (group.recipes.size() > 1) LegacyTheme.recipeScroll(mc, x, recipeY);
            }
            LegacyScaledRenderItem.renderInHolder(fontRenderer, mc.renderEngine, recipe.output,
                    x, recipeY, accessor.getInteger("craftingButtons.size"),
                    recipe.hasIngredients(playerInventory()) ? 1.0F : 0.5F, false);
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderHelper.disableStandardItemLighting();
    }

    private void drawSelectedRecipe() {
        LegacyRecipe recipe = selectedRecipe();
        if (recipe == null) return;
        String name = recipeName(recipe);
        LegacyTheme.drawCenteredString(mc, trim(name, 27), 65, 51,
                LegacyTheme.TEXT, false);
        int ingredientVariant = (int) ((System.currentTimeMillis() / 1000L)
                % recipe.ingredientVariantCount());
        ItemStack[] layout = recipe.displayLayout(gridWidth, gridHeight, ingredientVariant);
        boolean available = recipe.hasIngredients(playerInventory());
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        for (int i = 0; i < gridWidth * gridHeight; i++) {
            int slotSize = accessor.getInteger("craftingGridSlot.size");
            int x = accessor.getInteger("craftingGridSlot.x") + (i % gridWidth) * slotSize;
            int y = accessor.getInteger("craftingGridSlot.y") + (gridWidth == 2 ? 8 : 0)
                    + (i / gridWidth) * slotSize;
            if (layout[i] != null && !available) {
                LegacyTheme.warningSlot(mc, x, y, 16);
                LegacyTheme.warning(mc, x - 1, y - 1, 8);
            } else LegacyTheme.slot(mc, x, y, 16);
            if (layout[i] != null) {
                LegacyScaledRenderItem.renderInHolder(fontRenderer, mc.renderEngine, layout[i], x, y, 16);
            }
        }
        int layoutOffset = gridWidth == 3 ? 8 : 0;
        int arrowX = accessor.getInteger("craftingArrow.x") + layoutOffset;
        int arrowY = accessor.getInteger("craftingArrow.y");
        int resultX = accessor.getInteger("resultSlot.x") + layoutOffset;
        int resultY = accessor.getInteger("resultSlot.y");
        LegacyTheme.smallArrow(mc, arrowX, arrowY, available ? 16 : 0);
        if (available) LegacyTheme.resultSlot(mc, resultX, resultY, 25);
        else {
            LegacyTheme.warningSlot(mc, resultX, resultY, 25);
            LegacyTheme.warning(mc, resultX - 1, resultY - 1, 9);
        }
        LegacyScaledRenderItem.renderInHolder(fontRenderer, mc.renderEngine, recipe.output,
                resultX, resultY, 25);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderHelper.disableStandardItemLighting();
    }

    private void drawSelection() {
        Slot slot = inventoryIndex >= 0 && inventoryIndex < inventorySlots.inventorySlots.size()
                ? (Slot) inventorySlots.inventorySlots.get(inventoryIndex) : null;
        drawLegacySlotFocus(slot, inventoryMode && visibleSlot(slot));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            List<LegacyCraftingTab> tabs = availableTabs();
            int start = tabWindowStart(tabs.size());
            for (int visible = 0; visible < Math.min(TOP_TABS, tabs.size() - start); visible++) {
                if (inside(mouseX, mouseY, guiLeft + visible * 36, guiTop - 23, 37, 31)) {
                    setCategory(start + visible);
                    return;
                }
            }
            List<LegacyRecipeGroup> groups = visibleGroups();
            int recipeStart = Math.max(0, Math.min(groupIndex - RECIPE_TILES + 1, groups.size() - RECIPE_TILES));
            for (int tile = 0; tile < RECIPE_TILES && recipeStart + tile < groups.size(); tile++) {
                if (inside(mouseX, mouseY, guiLeft + 6 + tile * 20, guiTop + 18, 20, 20)) {
                    groupIndex = recipeStart + tile;
                    variantIndex = 0;
                    inventoryMode = false;
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) close();
        else if (keyCode == Keyboard.KEY_UP) navigate(PadButton.DPAD_UP);
        else if (keyCode == Keyboard.KEY_DOWN) navigate(PadButton.DPAD_DOWN);
        else if (keyCode == Keyboard.KEY_LEFT) navigate(PadButton.DPAD_LEFT);
        else if (keyCode == Keyboard.KEY_RIGHT) navigate(PadButton.DPAD_RIGHT);
        else if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_SPACE) activate();
        else if (keyCode == Keyboard.KEY_TAB) toggleInventoryMode();
        else if (keyCode == Keyboard.KEY_Q || keyCode == Keyboard.KEY_PRIOR) changeCategory(-1);
        else if (keyCode == Keyboard.KEY_E || keyCode == Keyboard.KEY_NEXT) changeCategory(1);
        else super.keyTyped(character, keyCode);
    }

    @Override
    public void onControllerButton(PadButton button) {
        if (button == PadButton.B || button == PadButton.BACK || button == PadButton.START) close();
        else if (button == PadButton.LEFT_STICK) toggleInventoryMode();
        else if (button == PadButton.Y) {
            if (inventoryMode) clickInventory(0, true);
            else toggleFilter();
        }
        else if (button == PadButton.A) activate();
        else if (button == PadButton.X) {
            if (inventoryMode) clickInventory(1, false);
            else clearGrid();
        } else if (!inventoryMode && button == PadButton.LEFT_BUMPER) changeCategory(-1);
        else if (!inventoryMode && button == PadButton.RIGHT_BUMPER) changeCategory(1);
        else if (!inventoryMode && (button == PadButton.LEFT_TRIGGER || button == PadButton.RIGHT_TRIGGER)) {
            message("Crafting is the only recipe type in Minecraft 1.2.5");
        } else if (button == PadButton.DPAD_UP || button == PadButton.DPAD_DOWN
                || button == PadButton.DPAD_LEFT || button == PadButton.DPAD_RIGHT) navigate(button);
    }

    private void navigate(PadButton direction) {
        if (!inventoryMode) {
            if (direction == PadButton.DPAD_LEFT) changeGroup(-1);
            else if (direction == PadButton.DPAD_RIGHT) changeGroup(1);
            else if (direction == PadButton.DPAD_UP) changeVariant(-1);
            else if (direction == PadButton.DPAD_DOWN) changeVariant(1);
            return;
        }
        inventoryIndex = neighborSlot(inventoryIndex, direction);
    }

    private void activate() {
        if (inventoryMode) clickInventory(false);
        else prepareOrCraft();
    }

    private void prepareOrCraft() {
        LegacyRecipe selected = selectedRecipe();
        if (selected == null) return;
        if (!selected.fits(gridWidth, gridHeight)) {
            message("Requires a crafting table");
            return;
        }
        Slot result = findResultSlot();
        if (result != null && result.getHasStack() && sameOutput(selected.output, result.getStack())) {
            click(result.slotNumber, 0, true);
            message("Crafted " + selected.output.getItem().getItemDisplayName(selected.output));
            return;
        }
        if (mc.thePlayer.inventory.getItemStack() != null) {
            message("Put down the held stack first");
            return;
        }
        if (!clearGrid()) return;
        if (!selected.hasIngredients(playerInventory())) {
            message("Missing ingredients");
            return;
        }
        ItemStack[] layout = selected.layoutForInventory(playerInventory(), gridWidth, gridHeight);
        List<Slot> matrixSlots = matrixSlots();
        for (int i = 0; i < layout.length; i++) {
            ItemStack ingredient = layout[i];
            if (ingredient == null) continue;
            Slot source = findIngredientSlot(ingredient);
            if (source == null) {
                message("Inventory changed; try again");
                return;
            }
            click(source.slotNumber, 0, false);
            click(matrixSlots.get(i).slotNumber, 1, false);
            if (mc.thePlayer.inventory.getItemStack() != null) click(source.slotNumber, 0, false);
        }
        message("Ready - press A to craft");
    }

    private boolean clearGrid() {
        if (mc.thePlayer.inventory.getItemStack() != null) {
            message("Put down the held stack first");
            return false;
        }
        for (Slot slot : matrixSlots()) if (slot.getHasStack()) click(slot.slotNumber, 0, true);
        for (Slot slot : matrixSlots()) {
            if (slot.getHasStack()) {
                message("Make inventory space to clear crafting");
                return false;
            }
        }
        return true;
    }

    private void clickInventory(boolean quickMove) {
        clickInventory(0, quickMove);
    }

    private void clickInventory(int button, boolean quickMove) {
        if (inventoryIndex < 0 || inventoryIndex >= inventorySlots.inventorySlots.size()) return;
        Slot slot = (Slot) inventorySlots.inventorySlots.get(inventoryIndex);
        if (visibleSlot(slot)) click(slot.slotNumber, button, quickMove);
    }

    private void click(int slot, int button, boolean quickMove) {
        mc.playerController.windowClick(inventorySlots.windowId, slot, button, quickMove, mc.thePlayer);
    }

    private Slot findIngredientSlot(ItemStack ingredient) {
        for (Object value : inventorySlots.inventorySlots) {
            Slot slot = (Slot) value;
            if (slot.inventory == mc.thePlayer.inventory && visibleSlot(slot)
                    && LegacyRecipe.matches(ingredient, slot.getStack())) return slot;
        }
        return null;
    }

    private Slot findResultSlot() {
        for (Object value : inventorySlots.inventorySlots) {
            Slot slot = (Slot) value;
            if (slot.slotNumber == 0) return slot;
        }
        return null;
    }

    private List<Slot> matrixSlots() {
        List<Slot> result = new ArrayList<Slot>();
        for (Object value : inventorySlots.inventorySlots) {
            Slot slot = (Slot) value;
            if (slot.inventory == craftMatrix) result.add(slot);
        }
        return result;
    }

    private int neighborSlot(int from, PadButton direction) {
        if (from < 0 || from >= inventorySlots.inventorySlots.size()) return firstInventorySlot();
        Slot origin = (Slot) inventorySlots.inventorySlots.get(from);
        int best = from;
        int bestScore = Integer.MAX_VALUE;
        for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
            if (i == from) continue;
            Slot candidate = (Slot) inventorySlots.inventorySlots.get(i);
            if (!visibleSlot(candidate)) continue;
            int dx = candidate.xDisplayPosition - origin.xDisplayPosition;
            int dy = candidate.yDisplayPosition - origin.yDisplayPosition;
            if (direction == PadButton.DPAD_LEFT && dx >= 0) continue;
            if (direction == PadButton.DPAD_RIGHT && dx <= 0) continue;
            if (direction == PadButton.DPAD_UP && dy >= 0) continue;
            if (direction == PadButton.DPAD_DOWN && dy <= 0) continue;
            int primary = direction == PadButton.DPAD_LEFT || direction == PadButton.DPAD_RIGHT
                    ? Math.abs(dx) : Math.abs(dy);
            int secondary = direction == PadButton.DPAD_LEFT || direction == PadButton.DPAD_RIGHT
                    ? Math.abs(dy) : Math.abs(dx);
            int score = primary * 4 + secondary * 7;
            if (score < bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }

    private void arrangeSlots() {
        int player = 0;
        for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
            Slot slot = (Slot) inventorySlots.inventorySlots.get(i);
            boolean armor = slot.getClass().getSimpleName().indexOf("SlotArmor") >= 0;
            if (slot.inventory != mc.thePlayer.inventory || armor) {
                slot.xDisplayPosition = -1000;
                slot.yDisplayPosition = -1000;
                continue;
            }
            if (player < 27) {
                slot.xDisplayPosition = accessor.getInteger("inventory.x")
                        + player % 9 * accessor.getInteger("inventory.slotSize");
                slot.yDisplayPosition = accessor.getInteger("inventory.y")
                        + player / 9 * accessor.getInteger("inventory.slotSize");
            } else {
                slot.xDisplayPosition = accessor.getInteger("inventory.x")
                        + (player - 27) * accessor.getInteger("inventory.slotSize");
                slot.yDisplayPosition = accessor.getInteger("quick_select.y");
            }
            player++;
        }
        slotsArranged = true;
    }

    private void restoreSlots() {
        if (!slotsArranged) return;
        for (int i = 0; i < inventorySlots.inventorySlots.size() && i < originalX.length; i++) {
            Slot slot = (Slot) inventorySlots.inventorySlots.get(i);
            slot.xDisplayPosition = originalX[i];
            slot.yDisplayPosition = originalY[i];
        }
        slotsArranged = false;
    }

    private boolean visibleSlot(Slot slot) {
        return slot.inventory == mc.thePlayer.inventory && slot.xDisplayPosition >= 0 && slot.yDisplayPosition >= 0;
    }

    private int firstInventorySlot() {
        for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
            if (visibleSlot((Slot) inventorySlots.inventorySlots.get(i))) return i;
        }
        return -1;
    }

    private void loadRecipes() {
        for (Object value : CraftingManager.getInstance().getRecipeList()) {
            if (!(value instanceof IRecipe)) continue;
            LegacyRecipe recipe = LegacyRecipe.read((IRecipe) value);
            if (recipe != null && recipe.displayFits(gridWidth, gridHeight)) recipes.add(recipe);
        }
    }

    private List<LegacyRecipeGroup> visibleGroups() {
        List<LegacyCraftingTab> tabs = availableTabs();
        LegacyCraftingTab category = tabs.get(clamp(categoryIndex, 0, tabs.size() - 1));
        Map<String, LegacyRecipeGroup> grouped = new LinkedHashMap<String, LegacyRecipeGroup>();
        for (LegacyRecipe recipe : recipes) {
            if (LegacyCraftingTabs.categoryFor(recipe.output) != category) continue;
            if (craftableOnly && mc != null && mc.thePlayer != null
                    && !recipe.hasIngredients(playerInventory())) continue;
            String key = listingKey(recipe, category);
            if (key == null) continue;
            LegacyRecipeGroup group = grouped.get(key);
            if (group == null) {
                group = new LegacyRecipeGroup(key);
                grouped.put(key, group);
            }
            group.add(recipe);
        }
        List<LegacyRecipeGroup> result = new ArrayList<LegacyRecipeGroup>(grouped.values());
        if ("structures".equals(category.id)) Collections.sort(result, new Comparator<LegacyRecipeGroup>() {
            public int compare(LegacyRecipeGroup left, LegacyRecipeGroup right) {
                return left.key.compareTo(right.key);
            }
        });
        return result;
    }

    private LegacyRecipe selectedRecipe() {
        List<LegacyRecipeGroup> groups = visibleGroups();
        if (groups.isEmpty()) return null;
        groupIndex = clamp(groupIndex, 0, groups.size() - 1);
        LegacyRecipeGroup group = groups.get(groupIndex);
        variantIndex = clamp(variantIndex, 0, group.recipes.size() - 1);
        return group.selected(variantIndex);
    }

    private ItemStack[] playerInventory() {
        return mc == null || mc.thePlayer == null ? new ItemStack[0] : mc.thePlayer.inventory.mainInventory;
    }

    private void changeCategory(int amount) {
        List<LegacyCraftingTab> tabs = availableTabs();
        setCategory((categoryIndex + amount + tabs.size()) % tabs.size());
    }

    private List<LegacyCraftingTab> availableTabs() {
        List<LegacyCraftingTab> available = new ArrayList<LegacyCraftingTab>();
        for (LegacyCraftingTab tab : LegacyCraftingTabs.tabs()) {
            if (gridWidth == 2 && "armour".equals(tab.id)) continue;
            for (LegacyRecipe recipe : recipes) {
                if (LegacyCraftingTabs.categoryFor(recipe.output) == tab) {
                    available.add(tab);
                    break;
                }
            }
        }
        if (available.isEmpty()) available.add(LegacyCraftingTabs.tabs().get(0));
        return available;
    }

    private String listingKey(LegacyRecipe recipe, LegacyCraftingTab category) {
        if ("structures".equals(category.id)) {
            int id = recipe.output.itemID;
            if (id == Block.planks.blockID) return "00_planks";
            if (id == Item.stick.shiftedIndex) return "01_stick";
            if (id == Block.blockSnow.blockID) return "02_snow";
            if (id == Block.sandStone.blockID) return "03_sandstone";
            if (id == Block.workbench.blockID) return "04_utility";
            if (id == Block.chest.blockID) return "05_chest";
            if (id == Block.trapdoor.blockID) return "06_trapdoor";
            return gridWidth == 2 ? null : "50_" + LegacyRecipeGroup.key(recipe.output);
        }
        return LegacyRecipeGroup.key(recipe.output);
    }

    private static String recipeName(LegacyRecipe recipe) {
        if (recipe.output.itemID == Block.planks.blockID) {
            switch (recipe.output.getItemDamage()) {
                case 0: return "Oak Planks";
                case 1: return "Spruce Planks";
                case 2: return "Birch Planks";
                case 3: return "Jungle Planks";
                default: break;
            }
        }
        return recipe.output.getItem().getItemDisplayName(recipe.output);
    }

    /** Deterministic modpack audit hook used by the visual scenario runner. */
    public String legacyVisualSelectRecipeFamily(String familyKey) {
        List<LegacyCraftingTab> tabs = availableTabs();
        for (int tab = 0; tab < tabs.size(); tab++) {
            categoryIndex = tab;
            List<LegacyRecipeGroup> groups = visibleGroups();
            for (int group = 0; group < groups.size(); group++) {
                LegacyRecipeGroup candidate = groups.get(group);
                if (!candidate.key.equals(familyKey)) continue;
                groupIndex = group;
                variantIndex = 0;
                inventoryMode = false;
                StringBuilder members = new StringBuilder();
                for (LegacyRecipe recipe : candidate.recipes) {
                    if (members.length() > 0) members.append(" | ");
                    members.append(recipeName(recipe));
                    members.append(" [").append(recipe.recipe.getClass().getName()).append(']');
                }
                return members.toString();
            }
        }
        return "";
    }

    /** Complete recipe-family inventory used to audit real ModLoader-era packs. */
    public String legacyVisualDescribeRecipeFamilies() {
        int originalCategory = categoryIndex;
        int originalGroup = groupIndex;
        int originalVariant = variantIndex;
        StringBuilder result = new StringBuilder();
        List<LegacyCraftingTab> tabs = availableTabs();
        for (int tab = 0; tab < tabs.size(); tab++) {
            categoryIndex = tab;
            for (LegacyRecipeGroup family : visibleGroups()) {
                result.append(tabs.get(tab).id).append('\t').append(family.key).append('\t');
                for (int member = 0; member < family.recipes.size(); member++) {
                    if (member > 0) result.append(" | ");
                    result.append(recipeName(family.recipes.get(member)));
                }
                result.append(System.getProperty("line.separator"));
            }
        }
        categoryIndex = originalCategory;
        groupIndex = originalGroup;
        variantIndex = originalVariant;
        return result.toString();
    }

    private void setCategory(int index) {
        categoryIndex = index;
        groupIndex = 0;
        variantIndex = 0;
        inventoryMode = false;
        normalizeSelection();
    }

    private void changeGroup(int amount) {
        List<LegacyRecipeGroup> groups = visibleGroups();
        if (groups.isEmpty()) return;
        groupIndex = (groupIndex + amount + groups.size()) % groups.size();
        variantIndex = 0;
    }

    private void changeVariant(int amount) {
        List<LegacyRecipeGroup> groups = visibleGroups();
        if (groups.isEmpty()) return;
        LegacyRecipeGroup group = groups.get(clamp(groupIndex, 0, groups.size() - 1));
        variantIndex = (variantIndex + amount + group.recipes.size()) % group.recipes.size();
    }

    private void toggleFilter() {
        craftableOnly = !craftableOnly;
        groupIndex = 0;
        variantIndex = 0;
        normalizeSelection();
        message(craftableOnly ? "Showing available recipes" : "Showing all recipes");
    }

    private void toggleInventoryMode() {
        inventoryMode = !inventoryMode;
        if (inventoryMode && inventoryIndex < 0) inventoryIndex = firstInventorySlot();
    }

    private void normalizeSelection() {
        List<LegacyRecipeGroup> groups = visibleGroups();
        groupIndex = groups.isEmpty() ? 0 : clamp(groupIndex, 0, groups.size() - 1);
        variantIndex = 0;
    }

    private int tabWindowStart(int size) {
        if (size <= TOP_TABS) return 0;
        return Math.max(0, Math.min(categoryIndex - TOP_TABS + 1, size - TOP_TABS));
    }

    private void message(String text) {
        status = text;
        statusTicks = 60;
    }

    private void close() {
        mc.thePlayer.closeScreen();
    }

    private static boolean sameOutput(ItemStack a, ItemStack b) {
        return a != null && b != null && a.itemID == b.itemID && a.getItemDamage() == b.getItemDamage();
    }

    private static boolean inside(int x, int y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}
