/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.util;

import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.settings.SettingsHandler;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import com.exiledradio.scorelegacy.settings.nosort.NoSortSettingsCategory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Sorts a storage: every stack outside no-sort and memory slots is merged by kind, ordered, and
 * laid out again from the first slot, as full as the slot limit allows.
 */
public final class InventorySorter {
    private InventorySorter() {}

    public static final Comparator<Map.Entry<ItemStackKey, Integer>> BY_NAME =
            Comparator.comparing((Map.Entry<ItemStackKey, Integer> e) -> getRegistryName(e.getKey())).thenComparingInt(e -> e.getKey().getStack().getMetadata());

    public static final Comparator<Map.Entry<ItemStackKey, Integer>> BY_COUNT = (first, second) -> {
        int ret = second.getValue().compareTo(first.getValue());
        return ret != 0 ? ret : BY_NAME.compare(first, second);
    };

    /** Items with more ore dictionary names first, then by the names themselves, then by registry name. */
    public static final Comparator<Map.Entry<ItemStackKey, Integer>> BY_TAGS = (first, second) -> {
        List<String> firstNames = getOreNames(first.getKey().getStack());
        List<String> secondNames = getOreNames(second.getKey().getStack());
        int ret = Integer.compare(secondNames.size(), firstNames.size());
        for (int i = 0; ret == 0 && i < firstNames.size(); i++) {
            ret = firstNames.get(i).compareTo(secondNames.get(i));
        }
        return ret != 0 ? ret : BY_NAME.compare(first, second);
    };

    public static void sort(IStorageWrapper wrapper) {
        InventoryHandler handler = wrapper.getInventoryHandler();
        Set<Integer> noSortSlots = new java.util.HashSet<>(getNoSortSlots(wrapper.getSettingsHandler()));
        // Slots an upgrade took over (compression) keep their order.
        noSortSlots.addAll(handler.getPartSlots());
        com.exiledradio.scorelegacy.settings.itemdisplay.ItemDisplaySettingsCategory display =
                wrapper.getSettingsHandler().getTypeCategory(com.exiledradio.scorelegacy.settings.itemdisplay.ItemDisplaySettingsCategory.class);
        Map<Integer, ItemStack> shown = display == null ? java.util.Collections.emptyMap() : display.getShownSlotItems();
        sortHandler(handler, getComparator(wrapper.getSortBy()), noSortSlots, handler.getUsableSlots());
        if (display != null) {
            display.followItems(shown);
        }
    }

    public static Comparator<Map.Entry<ItemStackKey, Integer>> getComparator(SortBy sortBy) {
        switch (sortBy) {
            case COUNT:
                return BY_COUNT;
            case TAGS:
                return BY_TAGS;
            case NAME:
            default:
                return BY_NAME;
        }
    }

    /** Slots sorting leaves alone: no-sort slots and memory slots. */
    public static Set<Integer> getNoSortSlots(SettingsHandler settings) {
        Set<Integer> slots = new HashSet<>();
        NoSortSettingsCategory noSort = settings.getTypeCategory(NoSortSettingsCategory.class);
        if (noSort != null) {
            slots.addAll(noSort.getNoSortSlots());
        }
        MemorySettingsCategory memory = settings.getTypeCategory(MemorySettingsCategory.class);
        if (memory != null) {
            slots.addAll(memory.getSlotIndexes());
        }
        return slots;
    }

    public static void sortHandler(InventoryHandler handler, Comparator<Map.Entry<ItemStackKey, Integer>> comparator, Set<Integer> noSortSlots, int slots) {
        Map<ItemStackKey, Integer> compacted = new LinkedHashMap<>();
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!noSortSlots.contains(slot) && !stack.isEmpty()) {
                compacted.merge(new ItemStackKey(stack), stack.getCount(), Integer::sum);
            }
        }
        List<Map.Entry<ItemStackKey, Integer>> sorted = new ArrayList<>(compacted.entrySet());
        sorted.sort(comparator);

        Iterator<Map.Entry<ItemStackKey, Integer>> it = sorted.iterator();
        ItemStackKey current = null;
        int count = 0;
        for (int slot = 0; slot < slots; slot++) {
            if (noSortSlots.contains(slot)) {
                continue;
            }
            if ((current == null || count <= 0) && it.hasNext()) {
                Map.Entry<ItemStackKey, Integer> entry = it.next();
                current = entry.getKey();
                count = entry.getValue();
            }
            if (current != null && count > 0) {
                count -= placeStack(handler, current, count, slot);
            } else if (!handler.getStackInSlot(slot).isEmpty()) {
                handler.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private static int placeStack(InventoryHandler handler, ItemStackKey current, int count, int slot) {
        ItemStack copy = current.getStack().copy();
        int placed = Math.min(count, handler.getMaxStackSizeFor(slot, copy));
        copy.setCount(placed);
        if (!ItemStack.areItemStacksEqual(handler.getStackInSlot(slot), copy)) {
            handler.setStackInSlot(slot, copy);
        }
        return placed;
    }

    private static String getRegistryName(ItemStackKey key) {
        return Objects.requireNonNull(key.getStack().getItem().getRegistryName()).toString();
    }

    private static List<String> getOreNames(ItemStack stack) {
        int[] ids = OreDictionary.getOreIDs(stack);
        String[] names = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            names[i] = OreDictionary.getOreName(ids[i]);
        }
        Arrays.sort(names);
        return Arrays.asList(names);
    }
}
