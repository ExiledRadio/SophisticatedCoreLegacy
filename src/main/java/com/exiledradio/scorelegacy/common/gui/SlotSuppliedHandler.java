/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.common.gui;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/** A slot over a handler that is looked up each time, so it follows a re-created upgrade wrapper. */
public class SlotSuppliedHandler extends SlotItemHandler {
    private final Supplier<IItemHandler> itemHandlerSupplier;
    private final int slot;

    public SlotSuppliedHandler(Supplier<IItemHandler> itemHandlerSupplier, int slot, int xPosition, int yPosition) {
        super(itemHandlerSupplier.get(), slot, xPosition, yPosition);
        this.itemHandlerSupplier = itemHandlerSupplier;
        this.slot = slot;
    }

    @Override
    public IItemHandler getItemHandler() {
        return itemHandlerSupplier.get();
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        return getItemHandler().getStackInSlot(slot);
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        ((IItemHandlerModifiable) getItemHandler()).setStackInSlot(slot, stack);
        onSlotChanged();
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return getItemHandler().isItemValid(slot, stack);
    }

    @Override
    public int getSlotStackLimit() {
        return getItemHandler().getSlotLimit(slot);
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        return getSlotStackLimit();
    }
}
