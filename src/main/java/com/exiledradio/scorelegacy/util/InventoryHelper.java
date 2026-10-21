/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class InventoryHelper {
    private InventoryHelper() {}

    public static boolean hasItem(IItemHandler inventory, Predicate<ItemStack> matches) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (matches.test(inventory.getStackInSlot(slot))) {
                return true;
            }
        }
        return false;
    }

    public static Set<Integer> getItemSlots(IItemHandler inventory, Predicate<ItemStack> matches) {
        Set<Integer> slots = new LinkedHashSet<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (matches.test(inventory.getStackInSlot(slot))) {
                slots.add(slot);
            }
        }
        return slots;
    }

    public static ItemStack insertIntoInventory(ItemStack stack, IItemHandler inventory, boolean simulate) {
        if (inventory instanceof IItemHandlerSimpleInserter) {
            return ((IItemHandlerSimpleInserter) inventory).insertItem(stack, simulate);
        }
        ItemStack remainingStack = stack.copy();
        for (int slot = 0; slot < inventory.getSlots() && !remainingStack.isEmpty(); slot++) {
            remainingStack = inventory.insertItem(slot, remainingStack, simulate);
        }
        return remainingStack;
    }

    public static List<ItemStack> insertIntoInventory(List<ItemStack> stacks, IItemHandler inventory, boolean simulate) {
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ItemStack left = insertIntoInventory(stack, inventory, simulate);
            if (!left.isEmpty()) {
                remaining.add(left);
            }
        }
        return remaining;
    }

    /** Extracts up to stack.getCount() of items that stack with the given stack, from any slots. */
    public static ItemStack extractFromInventory(ItemStack stack, IItemHandler inventory, boolean simulate) {
        int extractedCount = 0;
        for (int slot = 0; slot < inventory.getSlots() && extractedCount < stack.getCount(); slot++) {
            ItemStack slotStack = inventory.getStackInSlot(slot);
            if (ItemHandlerHelper.canItemStacksStack(stack, slotStack)) {
                int toExtract = Math.min(slotStack.getCount(), stack.getCount() - extractedCount);
                extractedCount += inventory.extractItem(slot, toExtract, simulate).getCount();
            }
        }
        if (extractedCount == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.copy();
        result.setCount(extractedCount);
        return result;
    }

    public static int getCountMissingInHandler(IItemHandler itemHandler, ItemStack filter, int expectedCount) {
        int missingCount = expectedCount;
        for (int slot = 0; slot < itemHandler.getSlots() && missingCount > 0; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (ItemHandlerHelper.canItemStacksStack(stack, filter)) {
                missingCount -= Math.min(stack.getCount(), missingCount);
            }
        }
        return missingCount;
    }

    /** Moves everything it can from A to B; onInserted is told about every stack moved. */
    public static void transfer(IItemHandler handlerA, IItemHandler handlerB, Consumer<ItemStack> onInserted) {
        for (int slot = 0; slot < handlerA.getSlots(); slot++) {
            ItemStack slotStack = handlerA.getStackInSlot(slot);
            if (slotStack.isEmpty()) {
                continue;
            }
            ItemStack resultStack = insertIntoInventory(slotStack, handlerB, true);
            int countToExtract = slotStack.getCount() - resultStack.getCount();
            if (countToExtract > 0 && handlerA.extractItem(slot, countToExtract, true).getCount() == countToExtract) {
                ItemStack moved = handlerA.extractItem(slot, countToExtract, false);
                insertIntoInventory(moved, handlerB, false);
                onInserted.accept(moved);
            }
        }
    }

    /** A detached copy with the same slot limits, for "would all of this fit?" checks. */
    public static IItemHandler cloneInventory(IItemHandler inventory) {
        ItemStackHandler cloned = new ItemStackHandler(inventory.getSlots()) {
            @Override
            public int getSlotLimit(int slot) {
                return inventory.getSlotLimit(slot);
            }

            @Override
            protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
                return Math.min(getSlotLimit(slot), stack.getMaxStackSize() * Math.max(1, getSlotLimit(slot) / 64));
            }
        };
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            cloned.setStackInSlot(slot, inventory.getStackInSlot(slot).copy());
        }
        return cloned;
    }

    public static Set<ItemStackKey> getUniqueStacks(IItemHandler handler) {
        Set<ItemStackKey> uniqueStacks = new LinkedHashSet<>();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                uniqueStacks.add(new ItemStackKey(stack));
            }
        }
        return uniqueStacks;
    }

    /** Puts the stack into the first inventory that takes it and drops whatever is left. */
    public static void insertOrDropItem(EntityPlayer player, ItemStack stack, IItemHandler... inventories) {
        ItemStack remaining = stack;
        for (IItemHandler inventory : inventories) {
            remaining = insertIntoInventory(remaining, inventory, false);
            if (remaining.isEmpty()) {
                return;
            }
        }
        player.dropItem(remaining, false);
    }
}
