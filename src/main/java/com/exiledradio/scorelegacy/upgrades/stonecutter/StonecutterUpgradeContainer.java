/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.stonecutter;

import com.exiledradio.scorelegacy.common.gui.IResultSlot;
import com.exiledradio.scorelegacy.common.gui.SlotSuppliedHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.compat.BackportRecipes;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * The stonecutter tab's input and result slots and its list of results (the original's
 * StonecutterRecipeContainer). Both sides work out the same list from the input, so a click on
 * a result is shown at once and only the index goes to the server.
 */
public class StonecutterUpgradeContainer extends UpgradeContainerBase<StonecutterUpgradeWrapper, StonecutterUpgradeContainer> {
    private static final String DATA_SHIFT_CLICK_INTO_STORAGE = "shiftClickIntoStorage";
    private static final String DATA_SELECTED_RECIPE_INDEX = "selectedRecipeIndex";
    private final Slot inputSlot;
    private final ResultSlot outputSlot;
    private final InventoryCraftResult resultInventory = new InventoryCraftResult();
    private ItemStack recipesFor = ItemStack.EMPTY;
    private List<ItemStack> recipes = Collections.emptyList();

    public StonecutterUpgradeContainer(EntityPlayer player, int containerId, StonecutterUpgradeWrapper wrapper, UpgradeHandler upgradeHandler,
                                       UpgradeContainerType<StonecutterUpgradeWrapper, StonecutterUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        Supplier<IItemHandler> input = () -> getUpgradeWrapper().getInputInventory();
        inputSlot = new SlotSuppliedHandler(input, 0, -100, -100);
        slots.add(inputSlot);
        outputSlot = new ResultSlot();
        slots.add(outputSlot);
    }

    /** What the input can be cut into, worked out again whenever the input changes kind. */
    public List<ItemStack> getRecipeList() {
        ItemStack input = inputSlot.getStack();
        if (input.isEmpty()) {
            recipesFor = ItemStack.EMPTY;
            recipes = Collections.emptyList();
        } else if (!ItemStack.areItemsEqual(input, recipesFor) || !ItemStack.areItemStackTagsEqual(input, recipesFor)) {
            recipesFor = input.copy();
            recipesFor.setCount(1);
            recipes = BackportRecipes.getStonecutterResults(recipesFor);
        }
        return recipes;
    }

    /** The chosen result's place in the list, or -1. */
    public int getSelectedRecipe() {
        ItemStack selected = getUpgradeWrapper().getSelectedResult();
        if (selected.isEmpty()) {
            return -1;
        }
        List<ItemStack> list = getRecipeList();
        for (int i = 0; i < list.size(); i++) {
            if (ItemStack.areItemStacksEqual(list.get(i), selected)) {
                return i;
            }
        }
        return -1;
    }

    public boolean hasItemsInInputSlot() {
        return inputSlot.getHasStack() && !getRecipeList().isEmpty();
    }

    public void selectRecipe(int recipeIndex) {
        List<ItemStack> list = getRecipeList();
        if (recipeIndex < 0 || recipeIndex >= list.size()) {
            return;
        }
        getUpgradeWrapper().setSelectedResult(list.get(recipeIndex));
        resultInventory.clear();
        sendDataToServer(() -> NBTHelper.withInt(DATA_SELECTED_RECIPE_INDEX, recipeIndex));
    }

    /** What the result slot holds: the chosen result while there's input to cut it from. */
    private ItemStack getExpectedResult() {
        int selected = getSelectedRecipe();
        return selected < 0 ? ItemStack.EMPTY : getRecipeList().get(selected);
    }

    public Slot getInputSlot() {
        return inputSlot;
    }

    public Slot getOutputSlot() {
        return outputSlot;
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_SHIFT_CLICK_INTO_STORAGE)) {
            setShiftClickIntoStorage(data.getBoolean(DATA_SHIFT_CLICK_INTO_STORAGE));
        } else if (data.hasKey(DATA_SELECTED_RECIPE_INDEX)) {
            selectRecipe(data.getInteger(DATA_SELECTED_RECIPE_INDEX));
        }
    }

    public boolean shouldShiftClickIntoStorage() {
        return getUpgradeWrapper().shouldShiftClickIntoStorage();
    }

    public void setShiftClickIntoStorage(boolean shiftClickIntoStorage) {
        getUpgradeWrapper().setShiftClickIntoStorage(shiftClickIntoStorage);
        sendBooleanToServer(DATA_SHIFT_CLICK_INTO_STORAGE, shiftClickIntoStorage);
    }

    @Override
    public boolean mergeIntoBackpackFirst(Slot slot) {
        return slot != outputSlot || shouldShiftClickIntoStorage();
    }

    /**
     * Holds one cut of the chosen result, refilled from the input after each take. Taking it
     * uses one input item; a shift-click repeats until the input or the room runs out.
     */
    private class ResultSlot extends Slot implements IResultSlot {
        ResultSlot() {
            super(resultInventory, 0, -100, -100);
        }

        @Nonnull
        @Override
        public ItemStack getStack() {
            ItemStack expected = getExpectedResult();
            ItemStack held = resultInventory.getStackInSlot(0);
            if (expected.isEmpty()) {
                if (!held.isEmpty()) {
                    resultInventory.clear();
                }
                return ItemStack.EMPTY;
            }
            if (held.isEmpty() || !ItemStack.areItemsEqual(held, expected) || !ItemStack.areItemStackTagsEqual(held, expected)) {
                resultInventory.setInventorySlotContents(0, expected.copy());
            }
            return resultInventory.getStackInSlot(0);
        }

        @Override
        public boolean getHasStack() {
            return !getStack().isEmpty();
        }

        @Nonnull
        @Override
        public ItemStack decrStackSize(int amount) {
            // Always the whole cut, as vanilla's result slots give.
            ItemStack result = getStack().copy();
            resultInventory.clear();
            return result;
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return false;
        }

        @Nonnull
        @Override
        public ItemStack onTake(@Nonnull EntityPlayer player, @Nonnull ItemStack stack) {
            stack.onCrafting(player.world, player, stack.getCount());
            resultInventory.clear();
            inputSlot.decrStackSize(1);
            return super.onTake(player, stack);
        }
    }
}
