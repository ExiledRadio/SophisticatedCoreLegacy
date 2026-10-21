/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import net.minecraft.item.ItemStack;

/** Implemented by every item that can go in a backpack's upgrade slots. */
public interface IUpgradeItem<T extends IUpgradeWrapper> {
    UpgradeType<T> getType();

    /** Overrides that add their own rules should still check {@link UpgradeLimits#check} first. */
    default UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack, boolean firstLevelBackpack) {
        return UpgradeLimits.check(this, storageWrapper);
    }

    /** The group this upgrade counts towards for per-storage limits. */
    default UpgradeGroup getUpgradeGroup() {
        return UpgradeGroup.NONE;
    }

    /** Columns of inventory slots this upgrade replaces with its own display (tank, battery). */
    default int getInventoryColumnsTaken() {
        return 0;
    }

    default UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper) {
        return UpgradeSlotChangeResult.SUCCESS;
    }

    default UpgradeSlotChangeResult canSwapUpgradeFor(ItemStack upgradeStackToPut, IStorageWrapper storageWrapper) {
        UpgradeSlotChangeResult limits = UpgradeLimits.checkSwap(this, upgradeStackToPut, storageWrapper);
        return limits.isSuccessful() ? canRemoveUpgradeFrom(storageWrapper) : limits;
    }
}
