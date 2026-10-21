/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.filter;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.upgrades.ContentsFilterLogicContainer;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class FilterUpgradeContainer extends UpgradeContainerBase<FilterUpgradeWrapper, FilterUpgradeContainer> {
    public static final UpgradeContainerType<FilterUpgradeWrapper, FilterUpgradeContainer> BASIC_TYPE = new UpgradeContainerType<>(FilterUpgradeContainer::new);
    public static final UpgradeContainerType<FilterUpgradeWrapper, FilterUpgradeContainer> ADVANCED_TYPE = new UpgradeContainerType<>(FilterUpgradeContainer::new);
    private static final String DATA_DIRECTION = "direction";
    private final ContentsFilterLogicContainer filterLogicContainer;

    private FilterUpgradeContainer(EntityPlayer player, int containerId, FilterUpgradeWrapper wrapper, UpgradeHandler upgradeHandler, UpgradeContainerType<FilterUpgradeWrapper, FilterUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        filterLogicContainer = new ContentsFilterLogicContainer(() -> getUpgradeWrapper().getFilterLogic(), this, slots::add);
    }

    public ContentsFilterLogicContainer getFilterLogicContainer() {
        return filterLogicContainer;
    }

    public FilterUpgradeWrapper.Direction getDirection() {
        return getUpgradeWrapper().getDirection();
    }

    public void setDirection(FilterUpgradeWrapper.Direction direction) {
        getUpgradeWrapper().setDirection(direction);
        sendDataToServer(() -> NBTHelper.withString(DATA_DIRECTION, direction.getSerializedName()));
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (filterLogicContainer.handleMessage(data)) {
            return;
        }
        if (data.hasKey(DATA_DIRECTION)) {
            setDirection(FilterUpgradeWrapper.Direction.fromName(data.getString(DATA_DIRECTION)));
        }
    }
}
