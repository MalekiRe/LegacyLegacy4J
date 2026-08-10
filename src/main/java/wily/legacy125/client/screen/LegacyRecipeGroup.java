package wily.legacy125.client.screen;

import net.minecraft.src.ItemStack;
import net.minecraft.src.ItemArmor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    static String key(ItemStack output) {
        if (output.getItem() instanceof ItemArmor) {
            return "armor:" + ((ItemArmor) output.getItem()).armorType;
        }
        String name = output.getItem().getItemDisplayName(output);
        String normalized = name.toLowerCase(Locale.ENGLISH).trim();
        String[] prefixes = {
                "wooden ", "wood ", "stone ", "iron ", "diamond ", "golden ", "gold ",
                "leather ", "chainmail ", "chain ", "oak ", "spruce ", "birch ", "jungle ",
                "white ", "orange ", "magenta ", "light blue ", "yellow ", "lime ", "pink ",
                "gray ", "light gray ", "cyan ", "purple ", "blue ", "brown ", "green ", "red ", "black "
        };
        boolean changed;
        do {
            changed = false;
            for (String prefix : prefixes) {
                if (normalized.startsWith(prefix)) {
                    normalized = normalized.substring(prefix.length());
                    changed = true;
                    break;
                }
            }
        } while (changed);
        return normalized.length() == 0 ? name.toLowerCase(Locale.ENGLISH) : normalized;
    }
}
