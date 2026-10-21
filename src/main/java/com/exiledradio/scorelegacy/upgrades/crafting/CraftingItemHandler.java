/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.crafting;

import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The vanilla crafting inventory, but reading and writing the upgrade's item handler, so
 * vanilla's recipe matching and SlotCrafting work on the upgrade's grid unchanged.
 */
public class CraftingItemHandler extends InventoryCrafting {
    private final Supplier<IItemHandlerModifiable> supplyInventory;
    private final Consumer<IInventory> onCraftingMatrixChanged;

    public CraftingItemHandler(Supplier<IItemHandlerModifiable> supplyInventory, Consumer<IInventory> onCraftingMatrixChanged) {
        super(new Container() {
            @Override
            public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
                return false;
            }
        }, 3, 3);
        this.supplyInventory = supplyInventory;
        this.onCraftingMatrixChanged = onCraftingMatrixChanged;
    }

    @Override
    public int getSizeInventory() {
        return supplyInventory.get().getSlots();
    }

    @Override
    public boolean isEmpty() {
        IItemHandlerModifiable inventory = supplyInventory.get();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int index) {
        IItemHandlerModifiable inventory = supplyInventory.get();
        return index >= inventory.getSlots() ? ItemStack.EMPTY : inventory.getStackInSlot(index);
    }

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int index) {
        IItemHandlerModifiable inventory = supplyInventory.get();
        ItemStack stack = inventory.getStackInSlot(index);
        inventory.setStackInSlot(index, ItemStack.EMPTY);
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = supplyInventory.get().extractItem(index, count, false);
        if (!stack.isEmpty()) {
            onCraftingMatrixChanged.accept(this);
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
        supplyInventory.get().setStackInSlot(index, stack);
        onCraftingMatrixChanged.accept(this);
    }

    @Override
    public void clear() {
        IItemHandlerModifiable inventory = supplyInventory.get();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public void fillStackedContents(@Nonnull RecipeItemHelper helper) {
        IItemHandlerModifiable inventory = supplyInventory.get();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            helper.accountStack(inventory.getStackInSlot(slot));
        }
    }
}
