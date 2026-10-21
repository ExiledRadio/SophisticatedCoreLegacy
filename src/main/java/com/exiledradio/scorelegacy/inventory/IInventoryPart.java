/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.inventory;

import net.minecraft.item.ItemStack;

/**
 * A run of inventory slots an upgrade takes over (the original's IInventoryPartHandler, used by
 * Storage's compression upgrade). The InventoryHandler sends every operation on these slots
 * here; the part keeps the real stacks through {@link InventoryHandler#getRawStack} and
 * {@link InventoryHandler#setRawStack}, so what is saved is always ordinary stacks.
 */
public interface IInventoryPart {
    int getFirstSlot();

    int getSlots();

    default boolean coversSlot(int slot) {
        return slot >= getFirstSlot() && slot < getFirstSlot() + getSlots();
    }

    ItemStack getStackInSlot(int slot);

    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    ItemStack extractItem(int slot, int amount, boolean simulate);

    void setStackInSlot(int slot, ItemStack stack);

    boolean isItemValid(int slot, ItemStack stack);

    int getSlotLimit(int slot);

    int getStackLimit(int slot, ItemStack stack);
}
