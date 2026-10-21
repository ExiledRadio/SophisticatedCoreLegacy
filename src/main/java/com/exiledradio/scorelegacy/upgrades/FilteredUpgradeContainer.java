/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/** Tab container for upgrades whose only settings are a plain filter. */
public class FilteredUpgradeContainer<W extends IUpgradeWrapper & IFilteredUpgrade> extends UpgradeContainerBase<W, FilteredUpgradeContainer<W>> {
    private final FilterLogicContainer<FilterLogic> filterLogicContainer;

    public FilteredUpgradeContainer(EntityPlayer player, int containerId, W wrapper, UpgradeHandler upgradeHandler, UpgradeContainerType<W, FilteredUpgradeContainer<W>> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        filterLogicContainer = new FilterLogicContainer<>(() -> getUpgradeWrapper().getFilterLogic(), this, slots::add);
    }

    public FilterLogicContainer<FilterLogic> getFilterLogicContainer() {
        return filterLogicContainer;
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        filterLogicContainer.handleMessage(data);
    }
}
