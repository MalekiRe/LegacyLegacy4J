package wily.legacy125.client.screen;

import net.minecraft.src.CraftingManager;
import net.minecraft.src.Block;
import net.minecraft.src.IRecipe;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import wily.legacy125.api.LegacyCraftingTabs;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
