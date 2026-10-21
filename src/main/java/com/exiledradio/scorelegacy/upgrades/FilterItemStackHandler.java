/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

/** Ghost slots: they hold a copy of an item as a filter and never take or give real items. */
public class FilterItemStackHandler extends ItemStackHandler {
    private boolean onlyEmptyFilters = true;

    public FilterItemStackHandler(int size) {
        super(size);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        updateEmptyFilters();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        updateEmptyFilters();
    }

    private void updateEmptyFilters() {
        onlyEmptyFilters = true;
        for (ItemStack filter : stacks) {
            if (!filter.isEmpty()) {
                onlyEmptyFilters = false;
                return;
            }
        }
    }

    public boolean hasOnlyEmptyFilters() {
        return onlyEmptyFilters;
    }
}
