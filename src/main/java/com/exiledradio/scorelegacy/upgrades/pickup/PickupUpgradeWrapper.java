/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.pickup;

import com.exiledradio.scorelegacy.api.IPickupResponseUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.ContentsFilterLogic;
import com.exiledradio.scorelegacy.upgrades.IContentsFilteredUpgrade;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class PickupUpgradeWrapper extends UpgradeWrapperBase<PickupUpgradeWrapper, PickupUpgradeItem>
        implements IPickupResponseUpgrade, IContentsFilteredUpgrade {
    private final ContentsFilterLogic filterLogic;

    public PickupUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new ContentsFilterLogic(upgrade, stack -> save(), upgradeItem.getFilterSlotCount(), storageWrapper::getInventoryHandler);
    }

    @Override
    public ItemStack pickup(World world, ItemStack stack, boolean simulate) {
        if (!filterLogic.matchesFilter(stack)) {
            return stack;
        }
        return storageWrapper.getInventoryForUpgradeProcessing().insertItem(stack, simulate);
    }

    @Override
    public ContentsFilterLogic getFilterLogic() {
        return filterLogic;
    }
}
