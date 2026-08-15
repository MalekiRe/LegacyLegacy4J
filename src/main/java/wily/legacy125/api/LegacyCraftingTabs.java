package wily.legacy125.api;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemBoat;
import net.minecraft.src.ItemBow;
import net.minecraft.src.ItemFishingRod;
import net.minecraft.src.ItemFlintAndSteel;
import net.minecraft.src.ItemFood;
import net.minecraft.src.ItemHoe;
import net.minecraft.src.ItemMinecart;
import net.minecraft.src.ItemSeeds;
import net.minecraft.src.ItemShears;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ItemSword;
import net.minecraft.src.ItemTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Public category registry and the seven Legacy Console crafting defaults. */
public final class LegacyCraftingTabs {
    private static final String ICON = "/legacy4j125/gui/icon/";
    private static final List<LegacyCraftingTab> TABS = new ArrayList<LegacyCraftingTab>();

    static {
        TABS.add(tab("structures", "Structures"));
        TABS.add(tab("tools", "Tools"));
        TABS.add(tab("food", "Food"));
        TABS.add(tab("armour", "Armour"));
        TABS.add(tab("mechanisms", "Mechanisms"));
        TABS.add(tab("transport", "Transport"));
        TABS.add(tab("decoration", "Decoration"));

        addModTabs("ic2", "IC2");
        addModTabs("buildcraft", "BuildCraft");
        addModTabs("redpower", "RedPower");
        addModTabs("ee2", "EE2");
        addModTabs("thaumcraft", "ThaumCraft");
        addModTabs("wirelessredstone", "Wireless Redstone");
        addModTabs("compactsolars", "Compact Solars");
        addModTabs("ironchest", "Iron Chests");
        addModTabs("enderstorage", "EnderStorage");
    }

    private LegacyCraftingTabs() {
    }

    private static LegacyCraftingTab tab(final String id, String title) {
        return new LegacyCraftingTab(id, title, ICON + id + ".png", new LegacyCraftingTab.Matcher() {
            public boolean matches(ItemStack output) {
                return id.equals(classifyId(output));
            }
        });
    }

    private static void addModTabs(final String modId, String title) {
        TABS.add(modTab(modId, "equipment", title + " Equipment", "tools"));
        TABS.add(modTab(modId, "components", title + " Components", "decoration"));
        TABS.add(modTab(modId, "machines", title + " Blocks & Machines", "mechanisms"));
    }

    private static LegacyCraftingTab modTab(final String modId, final String section, String title,
                                             String icon) {
        return new LegacyCraftingTab("mod:" + modId + ":" + section, title, ICON + icon + ".png",
                new LegacyCraftingTab.Matcher() {
                    public boolean matches(ItemStack output) {
                        return modId.equals(modId(output)) && section.equals(modSection(output));
                    }
                });
    }

    /** Registers a mod category after the built-ins and before the Structures fallback. */
    public static synchronized void register(LegacyCraftingTab tab) {
        if (tab == null) throw new IllegalArgumentException("tab");
        for (LegacyCraftingTab existing : TABS) {
            if (existing.id.equals(tab.id)) throw new IllegalArgumentException("Duplicate crafting tab: " + tab.id);
        }
        TABS.add(tab);
    }

    public static synchronized List<LegacyCraftingTab> tabs() {
        return Collections.unmodifiableList(new ArrayList<LegacyCraftingTab>(TABS));
    }

    public static LegacyCraftingTab categoryFor(ItemStack output) {
        List<LegacyCraftingTab> tabs = tabs();
        // User categories get first refusal; this lets a mod split its blocks out of Structures.
        for (int i = 7; i < tabs.size(); i++) if (tabs.get(i).matches(output)) return tabs.get(i);
        String id = classifyId(output);
        for (LegacyCraftingTab tab : tabs) if (tab.id.equals(id)) return tab;
        return tabs.get(0);
    }

    static String classifyId(ItemStack output) {
        if (output == null || output.getItem() == null) return "structures";
        Item item = output.getItem();
        if (item == Item.stick || blockId(output) == id(Block.trapdoor)) return "structures";
        if (LegacyCraftingGroups.isArmorLike(output)) return "armour";
        if (item instanceof ItemTool || item instanceof ItemSword || item instanceof ItemHoe
                || item instanceof ItemBow || item instanceof ItemShears || item instanceof ItemFishingRod
                || item instanceof ItemFlintAndSteel || LegacyCraftingGroups.isToolLike(output)) return "tools";
        if (item instanceof ItemFood || item instanceof ItemSeeds || item == Item.sugar
                || item == Item.wheat || item == Item.bowlSoup || item == Item.cake) return "food";
        if (item instanceof ItemBoat || item instanceof ItemMinecart || item == Item.compass
                || item == Item.pocketSundial || isTransportBlock(output)) return "transport";
        if (item == Item.redstone || item == Item.redstoneRepeater || isMechanismBlock(output)) return "mechanisms";
        if (item == Item.painting || item == Item.sign || item == Item.bed || item == Item.dyePowder
                || item == Item.book || isDecorationBlock(output)) return "decoration";
        if (item instanceof ItemBlock) return "structures";
        return "decoration";
    }

    static String modId(ItemStack output) {
        if (output == null || output.getItem() == null) return null;
        String owner = modIdFromType(output.getItem().getClass());
        if (owner != null) return owner;
        if (output.getItem() instanceof ItemBlock) {
            int block = ((ItemBlock) output.getItem()).getBlockID();
            if (block >= 0 && block < Block.blocksList.length && Block.blocksList[block] != null) {
                return modIdFromType(Block.blocksList[block].getClass());
            }
        }
        return null;
    }

    static String modIdFromType(Class<?> type) {
        while (type != null && type != Object.class) {
            String name = type.getName().toLowerCase(Locale.ENGLISH);
            if (name.startsWith("ic2.")) return "ic2";
            if (name.startsWith("buildcraft.") || name.contains(".buildcraft.")) return "buildcraft";
            if (name.startsWith("eloraam.")) return "redpower";
            if (name.startsWith("ee.")) return "ee2";
            if (name.startsWith("thaumcraft.")) return "thaumcraft";
            if (name.startsWith("codechicken.wirelessredstone.")) return "wirelessredstone";
            if (name.startsWith("cpw.mods.compactsolars.")) return "compactsolars";
            if (name.startsWith("cpw.mods.ironchest.")) return "ironchest";
            if (name.startsWith("codechicken.enderstorage.")) return "enderstorage";
            type = type.getSuperclass();
        }
        return null;
    }

    private static String modSection(ItemStack output) {
        Item item = output.getItem();
        if (item instanceof ItemBlock) return "machines";
        if (item instanceof ItemTool || item instanceof ItemSword || item instanceof ItemHoe
                || item instanceof ItemBow || item instanceof ItemShears || item instanceof ItemFishingRod
                || item instanceof ItemFlintAndSteel || LegacyCraftingGroups.isToolLike(output)
                || LegacyCraftingGroups.isArmorLike(output)) return "equipment";
        return "components";
    }

    private static boolean isMechanismBlock(ItemStack output) {
        int id = blockId(output);
        return id == id(Block.dispenser) || id == id(Block.music) || id == id(Block.pistonStickyBase)
                || id == id(Block.pistonBase) || id == id(Block.tnt) || id == id(Block.redstoneWire)
                || id == id(Block.lever) || id == id(Block.pressurePlateStone)
                || id == id(Block.pressurePlatePlanks) || id == id(Block.torchRedstoneIdle)
                || id == id(Block.torchRedstoneActive) || id == id(Block.button)
                || id == id(Block.redstoneRepeaterIdle) || id == id(Block.redstoneRepeaterActive)
                || id == id(Block.redstoneLampIdle)
                || id == id(Block.redstoneLampActive);
    }

    private static boolean isTransportBlock(ItemStack output) {
        int id = blockId(output);
        return id == id(Block.rail) || id == id(Block.railPowered) || id == id(Block.railDetector);
    }

    private static boolean isDecorationBlock(ItemStack output) {
        int id = blockId(output);
        return id == id(Block.glass) || id == id(Block.thinGlass) || id == id(Block.cloth)
                || id == id(Block.bookShelf) || id == id(Block.jukebox) || id == id(Block.torchWood)
                || id == id(Block.pumpkinLantern) || id == id(Block.plantYellow)
                || id == id(Block.plantRed) || id == id(Block.mushroomBrown) || id == id(Block.mushroomRed)
                || id == id(Block.vine) || id == id(Block.waterlily);
    }

    private static int blockId(ItemStack output) {
        return output.getItem() instanceof ItemBlock ? ((ItemBlock) output.getItem()).getBlockID() : -1;
    }

    private static int id(Block block) {
        return block == null ? -2 : block.blockID;
    }
}
