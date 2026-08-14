package wily.legacy125.client.screen;

import net.minecraft.src.ItemStack;
import wily.legacy125.api.LegacyCraftingGroups;

import java.util.ArrayList;
import java.util.List;

/** Variants that occupy one Legacy Console recipe tile. */
final class LegacyRecipeGroup {
    final String key;
    final List<LegacyRecipe> recipes = new ArrayList<LegacyRecipe>();

    LegacyRecipeGroup(String key) {
        this.key = key;
    }

    LegacyRecipe selected(int variant) {
        return recipes.get(Math.max(0, Math.min(recipes.size() - 1, variant)));
    }

    void add(LegacyRecipe recipe) {
        for (LegacyRecipe existing : recipes) {
            if (sameVariant(existing.output, recipe.output)) {
                existing.mergeIngredientVariant(recipe);
                return;
            }
        }
        recipes.add(recipe);
    }

    static String key(ItemStack output) {
        return LegacyCraftingGroups.key(output);
    }

    private static boolean sameVariant(ItemStack left, ItemStack right) {
        return left.itemID == right.itemID
                && left.getItemDamage() == right.getItemDamage()
                && left.stackSize == right.stackSize;
    }
}
