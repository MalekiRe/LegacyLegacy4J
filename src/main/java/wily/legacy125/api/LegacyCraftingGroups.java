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

        String normalized = normalizedName(output);
        String namedFamily = namedVariantFamily(output, normalized);
        if (namedFamily != null) return namedFamily;

        String tool = toolFamily(output);
        if (tool != null) return "tool:" + tool;

        String armor = namedArmorFamily(normalized);
        if (armor != null) return "armor-name:" + armor;

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

    private static String namedVariantFamily(ItemStack output, String name) {
        int jacket = name.indexOf(" jacketed ");
        if (jacket > 0) return "redpower:jacketed";

        if (oneOf(name, "treetap", "tree tap", "electric treetap", "electric tree tap")) {
            return "ic2:treetap";
        }

        if (name.equals("energy collector") || name.startsWith("collector mk")) {
            return "ee2:energy-collector";
        }
        if (name.equals("anti matter relay") || name.startsWith("relay mk")) {
            return "ee2:anti-matter-relay";
        }
        if (name.endsWith(" engine")) return "machine:engine";
        if (oneOf(name, "low voltage solar array", "medium voltage solar array",
                "high voltage solar array")) return "compact-solars:array";
        if (oneOf(name, "lv transformer", "mv transformer", "hv transformer")) {
            return "ic2:transformer";
        }
        if (oneOf(name, "batbox", "mfe", "mfsu")) return "ic2:energy-storage";
        if (name.startsWith("klein star ")) return "ee2:klein-star";
        if (oneOf(name, "silver chest", "iron chest", "copper chest", "gold chest",
                "diamond chest", "crystal chest")) return "iron-chests:chest";
        if (oneOf(name, "dm furnace", "rm furnace")) return "ee2:matter-furnace";
        if (oneOf(name, "dm block", "rm block")) return "ee2:matter-block";
        if (name.equals("vis ore") || name.endsWith(" vis ore")) return "thaumcraft:vis-ore";
        if (oneOf(name, "generator", "geothermal generator", "water mill", "wind mill")
                || name.equals("solar panel") && isIc2GeneratorItem(output)) return "ic2:generator";
        if (oneOf(name, "and gate", "nand gate")) return "redpower:and-gate";
        if (oneOf(name, "or gate", "nor gate")) return "redpower:or-gate";
        if (oneOf(name, "xor gate", "xnor gate")) return "redpower:xor-gate";
        if (oneOf(name, "pneumatic tube", "restriction tube", "redstone tube", "magtube",
                "fluid pipe")) return "redpower:tube";
        if (oneOf(name, "wireless receiver", "wireless transmitter", "wireless jammer")) {
            return "wireless-redstone:node";
        }
        if (oneOf(name, "od scanner", "ov scanner")) return "ic2:scanner";
        if (name.equals("tfbp empty") || name.startsWith("tfbp ")) return "ic2:tfbp";
        if (oneOf(name, "overclocker upgrade", "transformer upgrade", "energy storage upgrade")) {
            return "ic2:upgrade";
        }
        if (oneOf(name, "land mark", "path mark")) return "buildcraft:marker";

        // Approved Ye Olde Times recipe families. Keep these explicit: broad
        // suffix matching can accidentally combine unrelated ModLoader items.
        if (oneOf(name, "machine block", "advanced machine block")) {
            return "ic2:machine-block";
        }
        if (oneOf(name, "electric furnace", "induction furnace")) return "ic2:furnace";
        if (oneOf(name, "gold cable", "insulated gold cable", "2xins. gold cable")) {
            return "ic2:cable-gold";
        }
        if (oneOf(name, "copper cable", "uninsulated copper cable")) return "ic2:cable-copper";
        if (oneOf(name, "hv cable", "insulated hv cable", "2xins. hv cable", "4xins. hv cable")) {
            return "ic2:cable-hv";
        }
        if (oneOf(name, "glass fibre cable", "eu detector cable", "eu splitter cable",
                "ultra low current cable")) return "ic2:cable-special";
        if (oneOf(name, "empty cell", "water cell", "lava cell", "bio cell", "h. coal cell",
                "uranium cell", "near depleted uranium cell", "depleted isotope cell")) {
            return "ic2:cell";
        }
        if (oneOf(name, "macerator", "extractor", "compressor", "recycler", "canning machine")) {
            return "ic2:processing-machine";
        }

        if (isIronChestUpgrade(name)) return "iron-chests:upgrade";

        if (oneOf(name, "alchemical coal", "mobius fuel", "aeternalis fuel")) return "ee2:fuel";
        if (oneOf(name, "soul stone", "body stone", "life stone", "mind stone")) return "ee2:stone";
        if (oneOf(name, "evertide amulet", "volcanite amulet")) return "ee2:amulet";
        if (oneOf(name, "destruction catalyst", "hyperkinetic lens", "catalytic lens")) {
            return "ee2:catalyst-lens";
        }
        if (oneOf(name, "iron band", "black hole band", "ring of ignition", "zero ring",
                "ring of arcana", "void ring", "swiftwolf's rending gale", "harvest goddess band")) {
            return "ee2:ring-band";
        }

        if (oneOf(name, "mining well", "quarry", "mining pipe")) return "buildcraft:mining";
        if (oneOf(name, "builder", "filler", "architect table", "blueprint library")) {
            return "buildcraft:construction";
        }

        if (oneOf(name, "rs latch", "toggle latch", "transparent latch")) return "redpower:latch";
        if (oneOf(name, "state cell", "invert cell", "non invert cell", "null cell")) {
            return "redpower:logic-cell";
        }
        if (oneOf(name, "timer", "sequencer", "repeater", "pulse former", "synchronizer")) {
            return "redpower:timing";
        }
        if (oneOf(name, "transposer", "filter", "retriever", "regulator", "item detector",
                "buffer", "relay")) return "redpower:transport-machine";
        if (oneOf(name, "deployer", "block breaker", "ejector", "assembler", "sorting machine")) {
            return "redpower:automation-machine";
        }
        if (oneOf(name, "backplane", "8k ram module", "central processing unit", "cpu",
                "io expander", "monitor", "disk drive", "ribbon cable")) {
            return "redpower:computer";
        }
        if (oneOf(name, "blulectric furnace", "blulectric alloy furnace")) {
            return "redpower:blulectric-furnace";
        }

        if (oneOf(name, "vis conduit", "vis filter", "vis valve")) return "thaumcraft:vis-network";
        if (oneOf(name, "vis pump", "vis condenser", "vis storage tank")) {
            return "thaumcraft:vis-machine";
        }

        if (oneOf(name, "wireless transceiver", "blaze transceiver")) {
            return "wireless-redstone:transceiver";
        }
        if (oneOf(name, "receiver dish", "blaze receiver dish")) {
            return "wireless-redstone:receiver-dish";
        }
        if (oneOf(name, "wireless sniffer", "private sniffer")) return "wireless-redstone:sniffer";
        if (oneOf(name, "wireless remote", "wireless map", "wireless tracker", "triangulator")) {
            return "wireless-redstone:handheld";
        }
        return null;
    }

    private static boolean isIronChestUpgrade(String name) {
        return oneOf(name, "copper to iron chest upgrade", "copper to silver chest upgrade",
                "silver to gold chest upgrade", "iron to gold chest upgrade",
                "gold to diamond chest upgrade", "diamond to crystal chest upgrade");
    }

    private static boolean oneOf(String value, String... candidates) {
        for (String candidate : candidates) if (candidate.equals(value)) return true;
        return false;
    }

    private static boolean isIc2GeneratorItem(ItemStack output) {
        Class<?> type = output.getItem().getClass();
        return type.getName().toLowerCase(Locale.ENGLISH).startsWith("ic2.")
                || hierarchyName(type).contains("itemgenerator");
    }

    public static boolean isToolLike(ItemStack output) {
        return toolFamily(output) != null
                || output != null && output.getItem() != null && isNamedUtilityTool(normalizedName(output));
    }

    /** Handheld utility items whose old mods extend Item directly instead of a vanilla tool base class. */
    private static boolean isNamedUtilityTool(String name) {
        if (oneOf(name, "eu reader", "insulation cutter", "cf sprayer", "tool box",
                "od scanner", "ov scanner", "paint brush", "paint can", "voltmeter",
                "wool card", "diamond drawplate", "arcane tinkering tool", "crystal ball",
                "crystalline bell", "portable hole", "vis detector", "taint detector",
                "void compass", "wireless remote", "remote", "wireless map", "wireless tracker",
                "wireless sniffer", "private sniffer", "triangulator", "wireless triangulator",
                "philosopher's stone", "watch of flowing time", "gem of eternal density",
                "talisman of repair", "dark matter hammer", "red matter hammer",
                "dark matter shears", "red matter shears", "red katar", "red morning star",
                "divining rod", "destruction catalyst", "hyperkinetic lens", "catalytic lens",
                "mercurial eye", "iron band", "black hole band", "ring of ignition", "zero ring",
                "ring of arcana", "void ring", "swiftwolf's rending gale",
                "harvest goddess band", "archangel's smite", "evertide amulet",
                "volcanite amulet", "soul stone", "body stone", "life stone", "mind stone",
                "alchemy bag", "transmutation tablet", "alchemical tome")) return true;
        return name.equals("painter") || name.endsWith(" painter") || name.endsWith(" paint")
                || name.startsWith("wand of ") || name.startsWith("arcane focus")
                || name.startsWith("charm of ");
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
        if (containsWord(value, "treetap") || containsWord(value, "tree tap")) return "treetap";
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
