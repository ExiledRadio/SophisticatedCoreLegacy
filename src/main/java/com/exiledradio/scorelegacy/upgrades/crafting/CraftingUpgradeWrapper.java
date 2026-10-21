/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.crafting;

import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Consumer;

/** The grid's items are kept in the upgrade stack, so they travel with the upgrade. */
public class CraftingUpgradeWrapper extends UpgradeWrapperBase<CraftingUpgradeWrapper, CraftingUpgradeItem> {
    private static final String CRAFTING_INVENTORY_TAG = "craftingInventory";
    private final ItemStackHandler inventory;

    public CraftingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        inventory = new ItemStackHandler(9) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                upgrade.setTagInfo(CRAFTING_INVENTORY_TAG, serializeNBT());
                save();
            }
        };
        NBTTagCompound tag = upgrade.getTagCompound();
        if (tag != null && tag.hasKey(CRAFTING_INVENTORY_TAG)) {
            inventory.deserializeNBT(tag.getCompoundTag(CRAFTING_INVENTORY_TAG));
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    public boolean shouldShiftClickIntoBackpack() {
        return NBTHelper.getBoolean(upgrade, "shiftClickIntoBackpack", true);
    }

    public void setShiftClickIntoBackpack(boolean shiftClickIntoBackpack) {
        NBTHelper.setBoolean(upgrade, "shiftClickIntoBackpack", shiftClickIntoBackpack);
        save();
    }
}
