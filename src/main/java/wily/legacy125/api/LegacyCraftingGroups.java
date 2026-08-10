package wily.legacy125.api;

import net.minecraft.src.Item;
import net.minecraft.src.ItemArmor;
import net.minecraft.src.ItemAxe;
import net.minecraft.src.ItemHoe;
import net.minecraft.src.ItemPickaxe;
import net.minecraft.src.ItemSpade;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ItemSword;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Semantic recipe-family resolver. It deliberately depends only on Minecraft
 * base types and names, allowing old Forge/ModLoader items to participate
 * without a compile-time dependency on their mod.
 */
public final class LegacyCraftingGroups {
    public interface Resolver {
        /** Returns a stable group key, or {@code null} when this resolver does not apply. */
        String resolve(ItemStack output);
    }

    private static final List<Resolver> RESOLVERS = new ArrayList<Resolver>();
    private static final String[] PREFIXES = {
            "light blue ", "light gray ", "green sapphire ", "refined iron ",
            "dark matter ", "red matter ", "lapis lazuli ",
            "wooden ", "wood ", "stone ", "cobblestone ", "sandstone ",
            "iron ", "diamond ", "golden ", "gold ", "leather ",
            "chainmail ", "chain ", "bronze ", "copper ", "tin ",
            "silver ", "brass ", "steel ", "ruby ", "emerald ",
            "sapphire ", "marble ", "basalt ", "obsidian ", "thaumium ",
            "oak ", "spruce ", "birch ", "jungle ",
            "white ", "orange ", "magenta ", "yellow ", "lime ", "pink ",
            "gray ", "cyan ", "purple ", "blue ", "brown ", "green ",
            "red ", "black "
    };

    private LegacyCraftingGroups() {
    }

    /** Gives a mod resolver priority over the built-in cross-mod heuristics. */
    public static synchronized void registerFirst(Resolver resolver) {
        if (resolver == null) throw new IllegalArgumentException("resolver");
        RESOLVERS.add(0, resolver);
    }

    public static String key(ItemStack output) {
        if (output == null || output.getItem() == null) return "unknown";
        synchronized (LegacyCraftingGroups.class) {
            for (Resolver resolver : RESOLVERS) {
                String key = resolver.resolve(output);
                if (key != null && key.length() > 0) return "custom:" + key;
            }
        }

        Item item = output.getItem();
        if (item instanceof ItemArmor) return "armor:" + ((ItemArmor) item).armorType;

        String tool = toolFamily(output);
        if (tool != null) return "tool:" + tool;

        String armor = namedArmorFamily(normalizedName(output));
        if (armor != null) return "armor-name:" + armor;

        String normalized = normalizedName(output);
        boolean changed;
        do {
            changed = false;
            for (String prefix : PREFIXES) {
                if (normalized.startsWith(prefix)) {
                    normalized = normalized.substring(prefix.length()).trim();
                    changed = true;
                    break;
                }
            }
        } while (changed);
        return normalized.length() == 0 ? normalizedName(output) : normalized;
    }

    public static boolean isToolLike(ItemStack output) {
        return toolFamily(output) != null;
    }

    public static boolean isArmorLike(ItemStack output) {
        return output != null && output.getItem() != null
                && (output.getItem() instanceof ItemArmor
                || namedArmorFamily(normalizedName(output)) != null);
    }

    private static String toolFamily(ItemStack output) {
        if (output == null || output.getItem() == null) return null;
        Item item = output.getItem();
        if (item instanceof ItemPickaxe) return "pickaxe";
        if (item instanceof ItemSpade) return "shovel";
        if (item instanceof ItemAxe) return "axe";
        if (item instanceof ItemHoe) return "hoe";
        if (item instanceof ItemSword) return "sword";

        String type = hierarchyName(item.getClass());
        String family = toolFamilyFromText(type);
        return family != null ? family : toolFamilyFromText(normalizedName(output));
    }

    private static String hierarchyName(Class<?> type) {
        StringBuilder names = new StringBuilder();
        while (type != null && type != Object.class) {
            if (names.length() > 0) names.append(' ');
            names.append(type.getSimpleName().toLowerCase(Locale.ENGLISH));
            type = type.getSuperclass();
        }
        return names.toString();
    }

    private static String toolFamilyFromText(String value) {
        if (containsWord(value, "pickaxe")) return "pickaxe";
        if (containsWord(value, "shovel") || containsWord(value, "spade")) return "shovel";
        if (containsWord(value, "chainsaw")) return "chainsaw";
        if (containsWord(value, "handsaw") || endsWithWord(value, "saw")) return "handsaw";
        if (containsWord(value, "sickle")) return "sickle";
        if (containsWord(value, "sword") || containsWord(value, "athame")) return "sword";
        if (containsWord(value, "wrench") || containsWord(value, "screwdriver")) return "wrench";
        if (containsWord(value, "drill")) return "drill";
        if (containsWord(value, "mining laser")) return "mining laser";
        if (endsWithWord(value, "hoe") || value.contains("toolhoe")) return "hoe";
        if (endsWithWord(value, "axe") || value.contains("itemaxe") || value.contains("customaxe")) return "axe";
        return null;
    }

    private static String namedArmorFamily(String value) {
        if (endsWithWord(value, "helmet") || endsWithWord(value, "hood")) return "helmet";
        if (endsWithWord(value, "chestplate") || endsWithWord(value, "chestpiece")) return "chestplate";
        if (endsWithWord(value, "leggings") || endsWithWord(value, "pants")) return "leggings";
        if (endsWithWord(value, "boots")) return "boots";
        return null;
    }

    private static boolean containsWord(String value, String word) {
        return (" " + value + " ").contains(" " + word + " ") || value.contains(word);
    }

    private static boolean endsWithWord(String value, String word) {
        return value.equals(word) || value.endsWith(" " + word) || value.endsWith(word);
    }

    private static String normalizedName(ItemStack output) {
        String name = output.getItem().getItemDisplayName(output);
        if (name == null) name = "item-" + output.itemID;
        // Formatting codes and punctuation must not split otherwise identical
        // families supplied by different ModLoader-era mods.
        name = name.replaceAll("\\u00a7.", "").toLowerCase(Locale.ENGLISH).trim();
        name = name.replace('-', ' ').replace('_', ' ');
        return name.replaceAll("\\s+", " ");
    }
}
