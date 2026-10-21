package com.exiledradio.scorelegacy.util;

import net.minecraft.item.ItemStack;

import java.util.Objects;

/** Item + metadata + NBT identity of a stack, ignoring count. Usable as a map or set key. */
public final class ItemStackKey {
    private final ItemStack stack;
    private final int hash;

    public ItemStackKey(ItemStack stack) {
        this.stack = stack.copy();
        this.stack.setCount(1);
        hash = Objects.hash(stack.getItem(), stack.getMetadata(), stack.getTagCompound());
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemStackKey)) {
            return false;
        }
        ItemStack other = ((ItemStackKey) o).stack;
        return stack.getItem() == other.getItem() && stack.getMetadata() == other.getMetadata() && ItemStack.areItemStackTagsEqual(stack, other);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
