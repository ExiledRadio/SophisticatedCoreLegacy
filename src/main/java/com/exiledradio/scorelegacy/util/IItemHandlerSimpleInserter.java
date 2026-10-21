package com.exiledradio.scorelegacy.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/** A handler that takes a stack without being told the slot, stacking onto matches first. */
public interface IItemHandlerSimpleInserter extends IItemHandlerModifiable {
    ItemStack insertItem(ItemStack stack, boolean simulate);
}
