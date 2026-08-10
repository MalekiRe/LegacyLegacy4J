package wily.legacy125.client.screen;

import net.minecraft.src.IRecipe;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ShapedRecipes;
import net.minecraft.src.ShapelessRecipes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Mapping-neutral view of the private recipe data used by the 1.2.5 recipe classes. */
final class LegacyRecipe {
    final IRecipe recipe;
    final ItemStack output;
    final ItemStack[] cells;
    final int width;
    final int height;

    private LegacyRecipe(IRecipe recipe, ItemStack output, ItemStack[] cells, int width, int height) {
        this.recipe = recipe;
        this.output = output;
        this.cells = cells;
        this.width = width;
        this.height = height;
    }

    static LegacyRecipe read(IRecipe recipe) {
        if (recipe == null || recipe.getRecipeOutput() == null) return null;
        try {
            if (recipe instanceof ShapedRecipes) return readShaped(recipe);
            if (recipe instanceof ShapelessRecipes) return readShapeless(recipe);
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    private static LegacyRecipe readShaped(IRecipe recipe) throws ReflectiveOperationException {
        ItemStack[] ingredients = null;
        List<Integer> dimensions = new ArrayList<Integer>();
        for (Field field : recipe.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            if (field.getType() == ItemStack[].class) ingredients = (ItemStack[]) field.get(recipe);
            if (field.getType() == Integer.TYPE && Modifier.isPrivate(field.getModifiers())) {
                dimensions.add(field.getInt(recipe));
            }
        }
        if (ingredients == null || dimensions.size() < 2) return null;
        int width = dimensions.get(0);
        int height = dimensions.get(1);
        if (width <= 0 || height <= 0 || width * height != ingredients.length) return null;
        return new LegacyRecipe(recipe, recipe.getRecipeOutput().copy(), copy(ingredients), width, height);
    }

    private static LegacyRecipe readShapeless(IRecipe recipe) throws ReflectiveOperationException {
        List<?> ingredients = Collections.emptyList();
        for (Field field : recipe.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || !List.class.isAssignableFrom(field.getType())) continue;
            field.setAccessible(true);
            ingredients = (List<?>) field.get(recipe);
            break;
        }
        ItemStack[] cells = new ItemStack[ingredients.size()];
        for (int i = 0; i < cells.length; i++) {
            Object value = ingredients.get(i);
            if (!(value instanceof ItemStack)) return null;
            cells[i] = ((ItemStack) value).copy();
        }
        return new LegacyRecipe(recipe, recipe.getRecipeOutput().copy(), cells, cells.length, 1);
    }

    boolean fits(int gridWidth, int gridHeight) {
        if (recipe instanceof ShapelessRecipes) return cells.length <= gridWidth * gridHeight;
        return width <= gridWidth && height <= gridHeight;
    }

    boolean displayFits(int gridWidth, int gridHeight) {
        List<ItemStack> compact = new ArrayList<ItemStack>();
        for (ItemStack cell : cells) {
            if (cell == null) continue;
            boolean known = false;
            for (ItemStack value : compact) {
                if (matches(value, cell)) {
                    known = true;
                    break;
                }
            }
            if (!known) compact.add(cell);
        }
        return compact.size() <= gridWidth * gridHeight;
    }

    ItemStack[] displayLayout(int gridWidth, int gridHeight) {
        if (fits(gridWidth, gridHeight)) return layout(gridWidth, gridHeight);
        ItemStack[] result = new ItemStack[gridWidth * gridHeight];
        int used = 0;
        for (ItemStack cell : cells) {
            if (cell == null) continue;
            int found = -1;
            for (int i = 0; i < used; i++) if (matches(result[i], cell)) {
                found = i;
                break;
            }
            if (found >= 0) result[found].stackSize++;
            else if (used < result.length) {
                result[used] = cell.copy();
                result[used].stackSize = 1;
                used++;
            }
        }
        return result;
    }

    ItemStack[] layout(int gridWidth, int gridHeight) {
        ItemStack[] result = new ItemStack[gridWidth * gridHeight];
        if (recipe instanceof ShapelessRecipes) {
            for (int i = 0; i < cells.length && i < result.length; i++) result[i] = copy(cells[i]);
        } else {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    result[x + y * gridWidth] = copy(cells[x + y * width]);
                }
            }
        }
        return result;
    }

    boolean hasIngredients(ItemStack[] inventory) {
        List<ItemStack> needed = new ArrayList<ItemStack>();
        for (ItemStack cell : cells) if (cell != null) needed.add(cell);
        int[] remaining = new int[inventory.length];
        for (int i = 0; i < inventory.length; i++) {
            remaining[i] = inventory[i] == null ? 0 : inventory[i].stackSize;
        }
        for (ItemStack ingredient : needed) {
            boolean found = false;
            for (int i = 0; i < inventory.length; i++) {
                if (remaining[i] > 0 && matches(ingredient, inventory[i])) {
                    remaining[i]--;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    static boolean matches(ItemStack expected, ItemStack actual) {
        return expected != null && actual != null && expected.itemID == actual.itemID
                && (expected.getItemDamage() == -1 || expected.getItemDamage() == actual.getItemDamage());
    }

    private static ItemStack[] copy(ItemStack[] values) {
        ItemStack[] result = new ItemStack[values.length];
        for (int i = 0; i < values.length; i++) result[i] = copy(values[i]);
        return result;
    }

    private static ItemStack copy(ItemStack value) {
        return value == null ? null : value.copy();
    }
}
