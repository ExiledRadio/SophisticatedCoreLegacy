/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.stonecutter;

import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Consumer;

/**
 * The input item and the chosen result are kept in the upgrade stack. 1.12 recipes have no ids
 * the backports share, so the choice is remembered as the result item (the original keeps the
 * recipe id); putting the same kind of block back in picks the same result again.
 */
public class StonecutterUpgradeWrapper extends UpgradeWrapperBase<StonecutterUpgradeWrapper, StonecutterUpgradeItem> {
    private static final String INPUT_TAG = "input";
    private static final String SELECTED_RESULT_TAG = "selectedResult";
    private static final String SHIFT_CLICK_INTO_STORAGE_TAG = "shiftClickIntoStorage";
    private final ItemStackHandler inputInventory;

    public StonecutterUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        inputInventory = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                upgrade.setTagInfo(INPUT_TAG, getStackInSlot(0).writeToNBT(new NBTTagCompound()));
                save();
            }
        };
        NBTTagCompound tag = upgrade.getTagCompound();
        if (tag != null && tag.hasKey(INPUT_TAG)) {
            inputInventory.setStackInSlot(0, new ItemStack(tag.getCompoundTag(INPUT_TAG)));
        }
    }

    public ItemStackHandler getInputInventory() {
        return inputInventory;
    }

    public ItemStack getSelectedResult() {
        NBTTagCompound tag = upgrade.getTagCompound();
        return tag != null && tag.hasKey(SELECTED_RESULT_TAG) ? new ItemStack(tag.getCompoundTag(SELECTED_RESULT_TAG)) : ItemStack.EMPTY;
    }

    public void setSelectedResult(ItemStack result) {
        if (result.isEmpty()) {
            NBTHelper.removeTag(upgrade, SELECTED_RESULT_TAG);
        } else {
            upgrade.setTagInfo(SELECTED_RESULT_TAG, result.writeToNBT(new NBTTagCompound()));
        }
        save();
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    public boolean shouldShiftClickIntoStorage() {
        return NBTHelper.getBoolean(upgrade, SHIFT_CLICK_INTO_STORAGE_TAG, true);
    }

    public void setShiftClickIntoStorage(boolean shiftClickIntoStorage) {
        NBTHelper.setBoolean(upgrade, SHIFT_CLICK_INTO_STORAGE_TAG, shiftClickIntoStorage);
        save();
    }
}
