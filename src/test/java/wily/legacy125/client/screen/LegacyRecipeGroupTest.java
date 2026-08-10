package wily.legacy125.client.screen;

import net.minecraft.src.CraftingManager;
import net.minecraft.src.Block;
import net.minecraft.src.IRecipe;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import wily.legacy125.api.LegacyCraftingTabs;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyRecipeGroupTest {
    @BeforeAll
    static void initializeMinecraftInVanillaOrder() {
        assertEquals(1, Block.stone.blockID);
    }

    @Test
    void materialVariantsShareOneRecipeTile() {
        assertEquals(LegacyRecipeGroup.key(new ItemStack(Item.pickaxeWood)),
                LegacyRecipeGroup.key(new ItemStack(Item.pickaxeDiamond)));
        assertEquals(LegacyRecipeGroup.key(new ItemStack(Item.helmetLeather)),
                LegacyRecipeGroup.key(new ItemStack(Item.helmetGold)));
    }

    @Test
    void yeOldeTimesFamiliesGroupWithoutLinkingTheirModClasses() {
        assertEquals(LegacyRecipeGroup.key(named(30000, "Wood Sickle")),
                LegacyRecipeGroup.key(named(30001, "Ruby Sickle")));
        assertEquals(LegacyRecipeGroup.key(named(30002, "Wooden Transport Pipe")),
                LegacyRecipeGroup.key(named(30003, "Cobblestone Transport Pipe")));
        assertEquals(LegacyRecipeGroup.key(named(30004, "Ruby Block")),
                LegacyRecipeGroup.key(named(30005, "Sapphire Block")));
    }

    @Test
    void groupsEveryRedPowerJacketedVariantTogether() {
        assertSameGroup(named(30006, "Snow Jacketed Wire"),
                named(30007, "Snow Jacketed Cable"),
                named(30008, "Snow Jacketed Bluewire"));
        assertSameGroup(named(30009, "Pumpkin Jacketed Wire"),
                named(30010, "Pumpkin Jacketed Cable"),
                named(30011, "Pumpkin Jacketed Bluewire"));
        assertSameGroup(named(30012, "Ruby Block Jacketed Wire"),
                named(30013, "Ruby Block Jacketed Cable"),
                named(30014, "Ruby Block Jacketed Bluewire"));

        assertSameGroup(named(30006, "Snow Jacketed Wire"),
                named(30009, "Pumpkin Jacketed Wire"),
                named(30012, "Ruby Block Jacketed Wire"));
    }

    @Test
    void groupsEe2MkMachinesAndBuildCraftEngines() {
        assertSameGroup(named(30015, "Energy Collector"),
                named(30016, "Collector MK2"), named(30017, "Collector MK3"));
        assertSameGroup(named(30018, "Anti-Matter Relay"),
                named(30019, "Relay MK2"), named(30020, "Relay MK3"));
        assertSameGroup(named(30021, "Redstone Engine"),
                named(30022, "Steam Engine"), named(30023, "Combustion Engine"));

        assertFalse(LegacyRecipeGroup.key(named(30024, "Relay")).equals(
                LegacyRecipeGroup.key(named(30019, "Relay MK2"))));
    }

    @Test
    void groupsApprovedEnergyAndStorageProgressions() {
        assertSameGroup(named(30500, "Low Voltage Solar Array"),
                named(30501, "Medium Voltage Solar Array"), named(30502, "High Voltage Solar Array"));
        assertSameGroup(named(30503, "LV-Transformer"),
                named(30504, "MV-Transformer"), named(30505, "HV-Transformer"));
        assertSameGroup(named(30506, "BatBox"), named(30507, "MFE"), named(30508, "MFSU"));
        assertSameGroup(named(30509, "Generator"), named(30510, "Geothermal Generator"),
                named(30511, "Water Mill"), named(30512, "Wind Mill"), ic2Generator(30513, "Solar Panel"));
        assertFalse(LegacyRecipeGroup.key(named(30564, "Solar Panel")).equals(
                LegacyRecipeGroup.key(named(30509, "Generator"))));
    }

    @Test
    void groupsApprovedEe2AndThaumcraftProgressions() {
        assertSameGroup(named(30514, "Klein Star Ein"), named(30515, "Klein Star Zwei"),
                named(30516, "Klein Star Drei"), named(30517, "Klein Star Vier"),
                named(30518, "Klein Star Sphere"), named(30519, "Klein Star Omega"));
        assertSameGroup(named(30520, "DM Furnace"), named(30521, "RM Furnace"));
        assertSameGroup(named(30522, "DM Block"), named(30523, "RM Block"));
        assertSameGroup(named(30524, "Vis Ore"), named(30525, "Aqueous Vis Ore"),
                named(30526, "Earthen Vis Ore"), named(30527, "Fiery Vis Ore"),
                named(30528, "Tainted Vis Ore"), named(30529, "Vaporous Vis Ore"));
    }

    @Test
    void groupsApprovedRedPowerAndWirelessFamilies() {
        assertSameGroup(named(30530, "AND Gate"), named(30531, "NAND Gate"));
        assertSameGroup(named(30532, "OR Gate"), named(30533, "NOR Gate"));
        assertSameGroup(named(30534, "XOR Gate"), named(30535, "XNOR Gate"));
        assertSameGroup(named(30536, "Pneumatic Tube"), named(30537, "Restriction Tube"),
                named(30538, "Redstone Tube"), named(30539, "Magtube"), named(30540, "Fluid Pipe"));
        assertSameGroup(named(30541, "Wireless Receiver"), named(30542, "Wireless Transmitter"),
                named(30543, "Wireless Jammer"));
    }

    @Test
    void groupsApprovedIc2UtilitiesBuildCraftMarkersAndIronChests() {
        assertSameGroup(named(30544, "OD Scanner"), named(30545, "OV Scanner"));
        assertSameGroup(named(30546, "TFBP - Empty"), named(30547, "TFBP - Cultivation"),
                named(30548, "TFBP - Desertification"), named(30549, "TFBP - Irrigation"),
                named(30550, "TFBP - Chilling"), named(30551, "TFBP - Flatification"),
                named(30552, "TFBP - Mushroom"));
        assertSameGroup(named(30553, "Overclocker Upgrade"), named(30554, "Transformer Upgrade"),
                named(30555, "Energy Storage Upgrade"));
        assertSameGroup(named(30556, "Land Mark"), named(30557, "Path Mark"));
        assertSameGroup(named(30558, "Copper Chest"), named(30559, "Iron Chest"),
                named(30560, "Silver Chest"), named(30561, "Gold Chest"),
                named(30562, "Diamond Chest"), named(30563, "Crystal Chest"));
    }

    @Test
    void groupsApprovedIc2MachinesCablesCellsAndIronChestUpgrades() {
        assertSameGroup(named(30600, "Machine Block"), named(30601, "Advanced Machine Block"));
        assertSameGroup(named(30602, "Electric Furnace"), named(30603, "Induction Furnace"));
        assertSameGroup(named(30604, "Macerator"), named(30605, "Extractor"),
                named(30606, "Compressor"), named(30607, "Recycler"), named(30608, "Canning Machine"));

        assertSameGroup(named(30609, "Gold Cable"), named(30610, "Insulated Gold Cable"),
                named(30611, "2xIns. Gold Cable"));
        assertSameGroup(named(30612, "Copper Cable"), named(30613, "Uninsulated Copper Cable"));
        assertSameGroup(named(30614, "HV Cable"), named(30615, "Insulated HV Cable"),
                named(30616, "2xIns. HV Cable"), named(30617, "4xIns. HV Cable"));
        assertSameGroup(named(30618, "Glass Fibre Cable"), named(30619, "EU-Detector Cable"),
                named(30620, "EU-Splitter Cable"), named(30621, "Ultra-Low-Current Cable"));
        assertAllDifferent(named(30609, "Gold Cable"), named(30612, "Copper Cable"),
                named(30614, "HV Cable"), named(30618, "Glass Fibre Cable"));

        assertSameGroup(named(30622, "Empty Cell"), named(30623, "Water Cell"),
                named(30624, "Lava Cell"), named(30625, "Bio Cell"), named(30626, "H. Coal Cell"),
                named(30627, "Uranium Cell"), named(30628, "Near-depleted Uranium Cell"),
                named(30629, "Depleted Isotope Cell"));
        assertSameGroup(named(30630, "Copper to Iron Chest Upgrade"),
                named(30631, "Copper to Silver Chest Upgrade"), named(30632, "Silver to Gold Chest Upgrade"),
                named(30633, "Iron to Gold Chest Upgrade"), named(30634, "Gold to Diamond Chest Upgrade"),
                named(30635, "Diamond to Crystal Chest Upgrade"));
    }

    @Test
    void groupsApprovedEe2Families() {
        assertSameGroup(named(30700, "Alchemical Coal"), named(30701, "Mobius Fuel"),
                named(30702, "Aeternalis Fuel"));
        assertSameGroup(named(30703, "Soul Stone"), named(30704, "Body Stone"),
                named(30705, "Life Stone"), named(30706, "Mind Stone"));
        assertSameGroup(named(30707, "Evertide Amulet"), named(30708, "Volcanite Amulet"));
        assertSameGroup(named(30709, "Destruction Catalyst"), named(30710, "Hyperkinetic Lens"),
                named(30711, "Catalytic Lens"));
        assertSameGroup(named(30712, "Iron Band"), named(30713, "Black Hole Band"),
                named(30714, "Ring of Ignition"), named(30715, "Zero Ring"),
                named(30716, "Ring of Arcana"), named(30717, "Void Ring"),
                named(30718, "Swiftwolf's Rending Gale"), named(30719, "Harvest Goddess Band"));
    }

    @Test
    void groupsApprovedBuildCraftAndRedPowerSubsystems() {
        assertSameGroup(named(30800, "Mining Well"), named(30801, "Quarry"), named(30802, "Mining Pipe"));
        assertSameGroup(named(30803, "Builder"), named(30804, "Filler"),
                named(30805, "Architect Table"), named(30806, "Blueprint Library"));

        assertSameGroup(named(30807, "RS Latch"), named(30808, "Toggle Latch"),
                named(30809, "Transparent Latch"));
        assertSameGroup(named(30810, "State Cell"), named(30811, "Invert Cell"),
                named(30812, "Non-Invert Cell"), named(30813, "Null Cell"));
        assertSameGroup(named(30814, "Timer"), named(30815, "Sequencer"), named(30816, "Repeater"),
                named(30817, "Pulse Former"), named(30818, "Synchronizer"));
        assertSameGroup(named(30819, "Transposer"), named(30820, "Filter"), named(30821, "Retriever"),
                named(30822, "Regulator"), named(30823, "Item Detector"), named(30824, "Buffer"),
                named(30825, "Relay"));
        assertSameGroup(named(30826, "Deployer"), named(30827, "Block Breaker"),
                named(30828, "Ejector"), named(30829, "Assembler"), named(30830, "Sorting Machine"));
        assertSameGroup(named(30831, "Backplane"), named(30832, "8K RAM Module"),
                named(30833, "Central Processing Unit"), named(30834, "IO Expander"),
                named(30835, "Monitor"), named(30836, "Disk Drive"), named(30837, "Ribbon Cable"));
        assertSameGroup(named(30838, "Blulectric Furnace"), named(30839, "Blulectric Alloy Furnace"));
    }

    @Test
    void groupsApprovedThaumcraftAndWirelessSubsystems() {
        assertSameGroup(named(30900, "Vis Conduit"), named(30901, "Vis Filter"), named(30902, "Vis Valve"));
        assertSameGroup(named(30903, "Vis Pump"), named(30904, "Vis Condenser"),
                named(30905, "Vis Storage Tank"));
        assertSameGroup(named(30906, "Wireless Transceiver"), named(30907, "Blaze Transceiver"));
        assertSameGroup(named(30908, "Receiver Dish"), named(30909, "Blaze Receiver Dish"));
        assertSameGroup(named(30910, "Wireless Sniffer"), named(30911, "Private Sniffer"));
        assertSameGroup(named(30912, "Wireless Remote"), named(30913, "Wireless Map"),
                named(30914, "Wireless Tracker"), named(30915, "Triangulator"));
    }

    @Test
    void readsIc2StyleDirectRecipeImplementations() {
        ForeignRecipe shaped = new ForeignRecipe(new ItemStack(Item.pickaxeSteel),
                new ItemStack[]{new ItemStack(Item.ingotIron), new ItemStack(Item.ingotIron),
                        new ItemStack(Item.stick), new ItemStack(Item.stick)}, 2, false);
        LegacyRecipe read = LegacyRecipe.read(shaped);
        assertNotNull(read);
        assertEquals(2, read.width);
        assertEquals(2, read.height);
        assertFalse(read.shapeless);

        ForeignShapelessRecipe shapeless = new ForeignShapelessRecipe(new ItemStack(Item.dyePowder),
                new ItemStack[]{new ItemStack(Item.coal), new ItemStack(Item.redstone)}, false);
        LegacyRecipe readShapeless = LegacyRecipe.read(shapeless);
        assertNotNull(readShapeless);
        assertTrue(readShapeless.shapeless);
        assertEquals(2, readShapeless.cells.length);

        assertNull(LegacyRecipe.read(new ForeignRecipe(new ItemStack(Item.pickaxeSteel),
                shaped.input, 2, true)));
    }

    @Test
    void alternateRecipesForOneOutputDoNotDuplicateTheDropdownVariant() {
        LegacyRecipe shaped = LegacyRecipe.read(new ForeignRecipe(new ItemStack(Item.pickaxeWood),
                new ItemStack[]{new ItemStack(Block.planks), new ItemStack(Block.planks),
                        new ItemStack(Block.planks), null, new ItemStack(Item.stick), null,
                        null, new ItemStack(Item.stick), null}, 3, false));
        LegacyRecipe alternate = LegacyRecipe.read(new ForeignShapelessRecipe(
                new ItemStack(Item.pickaxeWood),
                new ItemStack[]{new ItemStack(Item.pickaxeWood), new ItemStack(Item.pickaxeWood)}, false));
        LegacyRecipe diamond = LegacyRecipe.read(new ForeignRecipe(new ItemStack(Item.pickaxeDiamond),
                new ItemStack[]{new ItemStack(Item.diamond), new ItemStack(Item.diamond),
                        new ItemStack(Item.diamond), null, new ItemStack(Item.stick), null,
                        null, new ItemStack(Item.stick), null}, 3, false));

        LegacyRecipeGroup group = new LegacyRecipeGroup("tool:pickaxe");
        group.add(shaped);
        group.add(alternate);
        group.add(diamond);

        assertEquals(2, group.recipes.size());
        assertEquals(Item.pickaxeWood.shiftedIndex, group.recipes.get(0).output.itemID);
        assertEquals(Item.pickaxeDiamond.shiftedIndex, group.recipes.get(1).output.itemID);
    }

    @Test
    void everyReadableVanillaRecipeHasATabAndStableGroupKey() {
        int readable = 0;
        Set<String> categories = new HashSet<String>();
        for (Object value : CraftingManager.getInstance().getRecipeList()) {
            if (!(value instanceof IRecipe)) continue;
            LegacyRecipe recipe = LegacyRecipe.read((IRecipe) value);
            if (recipe == null) continue;
            readable++;
            assertNotNull(LegacyCraftingTabs.categoryFor(recipe.output));
            assertTrue(LegacyRecipeGroup.key(recipe.output).length() > 0);
            categories.add(LegacyCraftingTabs.categoryFor(recipe.output).id);
        }
        assertTrue(readable > 100, "expected the real 1.2.5 recipe manager");
        assertEquals(7, categories.size(), "all seven console categories should contain recipes");
    }

    private static ItemStack named(int id, String name) {
        return new ItemStack(new NamedItem(id, name));
    }

    private static ItemStack ic2Generator(int id, String name) {
        return new ItemStack(new FakeItemGenerator(id, name));
    }

    private static void assertSameGroup(ItemStack first, ItemStack... others) {
        String expected = LegacyRecipeGroup.key(first);
        for (ItemStack other : others) assertEquals(expected, LegacyRecipeGroup.key(other));
    }

    private static void assertAllDifferent(ItemStack... stacks) {
        Set<String> keys = new HashSet<String>();
        for (ItemStack stack : stacks) keys.add(LegacyRecipeGroup.key(stack));
        assertEquals(stacks.length, keys.size());
    }

    private static final class NamedItem extends Item {
        private final String displayName;

        private NamedItem(int id, String displayName) {
            super(id);
            this.displayName = displayName;
        }

        @Override
        public String getItemDisplayName(ItemStack stack) {
            return displayName;
        }
    }

    private static final class FakeItemGenerator extends Item {
        private final String displayName;

        private FakeItemGenerator(int id, String displayName) {
            super(id);
            this.displayName = displayName;
        }

        @Override
        public String getItemDisplayName(ItemStack stack) {
            return displayName;
        }
    }

    private static class ForeignRecipe implements IRecipe {
        public final ItemStack output;
        public final ItemStack[] input;
        public final int inputWidth;
        public final boolean hidden;

        private ForeignRecipe(ItemStack output, ItemStack[] input, int inputWidth, boolean hidden) {
            this.output = output;
            this.input = input;
            this.inputWidth = inputWidth;
            this.hidden = hidden;
        }

        public boolean matches(InventoryCrafting crafting) {
            return false;
        }

        public ItemStack getCraftingResult(InventoryCrafting crafting) {
            return output.copy();
        }

        public int getRecipeSize() {
            return input.length;
        }

        public ItemStack getRecipeOutput() {
            return output;
        }
    }

    private static final class ForeignShapelessRecipe extends ForeignRecipe {
        private ForeignShapelessRecipe(ItemStack output, ItemStack[] input, boolean hidden) {
            super(output, input, input.length, hidden);
        }
    }
}
