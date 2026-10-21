/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.util;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Which items the compacting upgrade can squeeze (2x2 or 3x3 crafting into one item), and with
 * what result. Keyed on item + metadata, since 1.12 keeps variants (dyes, wool) in metadata.
 */
public final class RecipeHelper {
    private static final int MAX_FOLLOW_UP_COMPACTING_RECIPES = 30;
    private static final Map<ItemStackKey, Set<CompactingShape>> ITEM_COMPACTING_SHAPES = new HashMap<>();
    private static final Map<String, CompactingResult> COMPACTING_RESULTS = new HashMap<>();
    private static final Map<ItemStackKey, UncompactingResult> UNCOMPACTING_RESULTS = new HashMap<>();

    private RecipeHelper() {}

    /** Recipes can change on /reload-style events from other mods; clearing is cheap. */
    public static void clearCache() {
        ITEM_COMPACTING_SHAPES.clear();
        COMPACTING_RESULTS.clear();
        UNCOMPACTING_RESULTS.clear();
    }

    private static final List<java.util.function.Supplier<String[]>> ALWAYS_COMPACTED_SOURCES = new ArrayList<>();

    /**
     * A config list of items the compacting upgrades compact although no recipe crafts them back
     * (registry name, optionally "@metadata"; a trailing "*" matches any name starting with the
     * rest). Each mod adds its own; an item in any list counts.
     */
    public static void addAlwaysCompactedSource(java.util.function.Supplier<String[]> source) {
        ALWAYS_COMPACTED_SOURCES.add(source);
    }

    public static boolean isAlwaysCompacted(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return false;
        }
        String name = stack.getItem().getRegistryName().toString();
        for (java.util.function.Supplier<String[]> source : ALWAYS_COMPACTED_SOURCES) {
            for (String entry : source.get()) {
                String trimmed = entry.trim();
                int at = trimmed.indexOf('@');
                String entryName = at < 0 ? trimmed : trimmed.substring(0, at);
                boolean nameMatches = entryName.endsWith("*") ? name.startsWith(entryName.substring(0, entryName.length() - 1)) : entryName.equals(name);
                if (!nameMatches) {
                    continue;
                }
                if (at < 0) {
                    return true;
                }
                try {
                    if (Integer.parseInt(trimmed.substring(at + 1)) == stack.getMetadata()) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    // a typo in the config: the entry matches nothing
                }
            }
        }
        return false;
    }

    public static Set<CompactingShape> getItemCompactingShapes(ItemStack stack) {
        if (stack.isEmpty() || stack.hasTagCompound()) {
            return Collections.emptySet();
        }
        ItemStackKey key = new ItemStackKey(stack);
        Set<CompactingShape> cached = ITEM_COMPACTING_SHAPES.get(key);
        if (cached != null) {
            return cached;
        }
        World world = getWorld();
        if (world == null) {
            return Collections.emptySet();
        }
        Set<CompactingShape> shapes = EnumSet.noneOf(CompactingShape.class);
        addShape(stack, world, 2, CompactingShape.TWO_BY_TWO_UNCRAFTABLE, CompactingShape.TWO_BY_TWO, shapes);
        addShape(stack, world, 3, CompactingShape.THREE_BY_THREE_UNCRAFTABLE, CompactingShape.THREE_BY_THREE, shapes);
        ITEM_COMPACTING_SHAPES.put(key, shapes);
        return shapes;
    }

    private static void addShape(ItemStack stack, World world, int size, CompactingShape uncraftableShape, CompactingShape shape, Set<CompactingShape> shapes) {
        CompactingResult result = getCompactingResult(stack, world, size);
        if (result.getResult().isEmpty() || ItemStackKeyEquals(stack, result.getResult()) || isPartOfCompactingLoop(stack, result.getResult(), world)) {
            return;
        }
        shapes.add(uncompactMatchesItem(result.getResult(), world, stack, size * size) ? uncraftableShape : shape);
    }

    private static boolean ItemStackKeyEquals(ItemStack a, ItemStack b) {
        return new ItemStackKey(a).equals(new ItemStackKey(b));
    }

    private static boolean isPartOfCompactingLoop(ItemStack firstCompacted, ItemStack firstCompactResult, World world) {
        Set<ItemStackKey> compactedItems = new HashSet<>();
        Queue<ItemStack> itemsToCompact = new LinkedList<>();
        itemsToCompact.add(firstCompactResult);
        int iterations = 0;
        while (!itemsToCompact.isEmpty()) {
            ItemStack itemToCompact = itemsToCompact.poll();
            for (int size = 2; size <= 3; size++) {
                ItemStack result = getCompactingResult(itemToCompact, world, size).getResult();
                if (result.isEmpty()) {
                    continue;
                }
                if (ItemStackKeyEquals(result, firstCompacted)) {
                    return true;
                }
                if (compactedItems.contains(new ItemStackKey(result))) {
                    return false;
                }
                itemsToCompact.add(result);
            }
            compactedItems.add(new ItemStackKey(itemToCompact));
            if (++iterations > MAX_FOLLOW_UP_COMPACTING_RECIPES) {
                return true;
            }
        }
        return false;
    }

    private static boolean uncompactMatchesItem(ItemStack result, World world, ItemStack item, int count) {
        InventoryCrafting grid = getFilledCraftingGrid(result, 1);
        IRecipe recipe = CraftingManager.findMatchingRecipe(grid, world);
        if (recipe == null) {
            return false;
        }
        ItemStack uncompacted = recipe.getCraftingResult(grid);
        return ItemStackKeyEquals(uncompacted, item) && uncompacted.getCount() == count;
    }

    public static CompactingResult getCompactingResult(ItemStack stack, int size) {
        World world = getWorld();
        return world == null ? CompactingResult.EMPTY : getCompactingResult(stack, world, size);
    }

    private static CompactingResult getCompactingResult(ItemStack stack, World world, int size) {
        String key = stack.getItem().getRegistryName() + "@" + stack.getMetadata() + "x" + size;
        CompactingResult cached = COMPACTING_RESULTS.get(key);
        if (cached != null) {
            return cached;
        }
        InventoryCrafting grid = getFilledCraftingGrid(stack, size);
        IRecipe recipe = CraftingManager.findMatchingRecipe(grid, world);
        CompactingResult result = CompactingResult.EMPTY;
        if (recipe != null) {
            List<ItemStack> remaining = new ArrayList<>();
            for (ItemStack remainingStack : recipe.getRemainingItems(grid)) {
                if (!remainingStack.isEmpty()) {
                    remaining.add(remainingStack);
                }
            }
            result = new CompactingResult(recipe.getCraftingResult(grid), remaining);
        }
        COMPACTING_RESULTS.put(key, result);
        return result;
    }

    /**
     * What one of this item crafts into on its own, if that is 4 or 9 of something that crafts
     * back into exactly it (a block into ingots); else null. Used by Storage's compression upgrade.
     */
    @Nullable
    public static UncompactingResult getUncompactingResult(ItemStack stack) {
        if (stack.isEmpty() || stack.hasTagCompound()) {
            return null;
        }
        ItemStackKey key = new ItemStackKey(stack);
        if (UNCOMPACTING_RESULTS.containsKey(key)) {
            return UNCOMPACTING_RESULTS.get(key);
        }
        World world = getWorld();
        if (world == null) {
            return null;
        }
        UncompactingResult result = null;
        InventoryCrafting grid = getFilledCraftingGrid(stack, 1);
        IRecipe recipe = CraftingManager.findMatchingRecipe(grid, world);
        if (recipe != null) {
            ItemStack uncompacted = recipe.getCraftingResult(grid);
            int count = uncompacted.getCount();
            if ((count == 4 || count == 9) && !uncompacted.hasTagCompound()) {
                ItemStack single = uncompacted.copy();
                single.setCount(1);
                CompactingShape shape = count == 4 ? CompactingShape.TWO_BY_TWO_UNCRAFTABLE : CompactingShape.THREE_BY_THREE_UNCRAFTABLE;
                if (getItemCompactingShapes(single).contains(shape)
                        && ItemStackKeyEquals(getCompactingResult(single, world, count == 4 ? 2 : 3).getResult(), stack)) {
                    result = new UncompactingResult(single, shape);
                }
            }
        }
        UNCOMPACTING_RESULTS.put(key, result);
        return result;
    }

    /** One of an item uncrafted: the result item (count 1) and the shape that crafts it back. */
    public static class UncompactingResult {
        private final ItemStack result;
        private final CompactingShape compactUsingShape;

        public UncompactingResult(ItemStack result, CompactingShape compactUsingShape) {
            this.result = result;
            this.compactUsingShape = compactUsingShape;
        }

        public ItemStack getResult() {
            return result.copy();
        }

        public CompactingShape getCompactUsingShape() {
            return compactUsingShape;
        }
    }

    private static InventoryCrafting getFilledCraftingGrid(ItemStack stack, int size) {
        InventoryCrafting grid = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        }, size, size);
        for (int i = 0; i < grid.getSizeInventory(); i++) {
            ItemStack single = stack.copy();
            single.setCount(1);
            grid.setInventorySlotContents(i, single);
        }
        return grid;
    }

    @Nullable
    private static World getWorld() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null && server.getWorld(0) != null) {
            return server.getWorld(0);
        }
        return SophisticatedCoreLegacy.proxy.getClientWorld();
    }

    public enum CompactingShape {
        THREE_BY_THREE,
        TWO_BY_TWO,
        THREE_BY_THREE_UNCRAFTABLE,
        TWO_BY_TWO_UNCRAFTABLE
    }

    public static class CompactingResult {
        public static final CompactingResult EMPTY = new CompactingResult(ItemStack.EMPTY, Collections.emptyList());

        private final ItemStack result;
        private final List<ItemStack> remainingItems;

        CompactingResult(ItemStack result, List<ItemStack> remainingItems) {
            this.result = result;
            this.remainingItems = remainingItems;
        }

        public ItemStack getResult() {
            return result.copy();
        }

        public List<ItemStack> getRemainingItems() {
            List<ItemStack> copies = new ArrayList<>();
            for (ItemStack stack : remainingItems) {
                copies.add(stack.copy());
            }
            return copies;
        }
    }
}
