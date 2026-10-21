/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.inventory;

import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

/** Lets items in only if an input filter accepts them, and out only if an output filter does. */
public class FilteredItemHandler implements IItemHandlerSimpleInserter {
    private final IItemHandler inventoryHandler;
    private final List<FilterLogic> inputFilters;
    private final List<FilterLogic> outputFilters;

    public FilteredItemHandler(IItemHandler inventoryHandler, List<FilterLogic> inputFilters, List<FilterLogic> outputFilters) {
        this.inventoryHandler = inventoryHandler;
        this.inputFilters = inputFilters;
        this.outputFilters = outputFilters;
    }

    private static boolean anyMatches(List<FilterLogic> filters, ItemStack stack) {
        if (filters.isEmpty()) {
            return true;
        }
        for (FilterLogic filter : filters) {
            if (filter.matchesFilter(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getSlots() {
        return inventoryHandler.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventoryHandler.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return anyMatches(inputFilters, stack) ? inventoryHandler.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        if (!anyMatches(inputFilters, stack)) {
            return stack;
        }
        if (inventoryHandler instanceof IItemHandlerSimpleInserter) {
            return ((IItemHandlerSimpleInserter) inventoryHandler).insertItem(stack, simulate);
        }
        ItemStack remaining = stack;
        for (int slot = 0; slot < inventoryHandler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = inventoryHandler.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return anyMatches(outputFilters, getStackInSlot(slot)) ? inventoryHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventoryHandler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return inventoryHandler.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (inventoryHandler instanceof IItemHandlerSimpleInserter) {
            ((IItemHandlerSimpleInserter) inventoryHandler).setStackInSlot(slot, stack);
        }
    }
}
