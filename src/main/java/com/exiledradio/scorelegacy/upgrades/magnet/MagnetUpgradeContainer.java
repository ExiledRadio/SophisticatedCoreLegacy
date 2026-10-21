/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.magnet;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.upgrades.ContentsFilterLogicContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class MagnetUpgradeContainer extends UpgradeContainerBase<MagnetUpgradeWrapper, MagnetUpgradeContainer> {
    private static final String DATA_PICKUP_ITEMS = "pickupItems";
    private static final String DATA_PICKUP_XP = "pickupXp";
    private final ContentsFilterLogicContainer filterLogicContainer;

    public MagnetUpgradeContainer(EntityPlayer player, int containerId, MagnetUpgradeWrapper wrapper, UpgradeHandler upgradeHandler, UpgradeContainerType<MagnetUpgradeWrapper, MagnetUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        filterLogicContainer = new ContentsFilterLogicContainer(() -> getUpgradeWrapper().getFilterLogic(), this, slots::add);
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_PICKUP_ITEMS)) {
            setPickupItems(data.getBoolean(DATA_PICKUP_ITEMS));
        } else if (data.hasKey(DATA_PICKUP_XP)) {
            setPickupXp(data.getBoolean(DATA_PICKUP_XP));
        } else {
            filterLogicContainer.handleMessage(data);
        }
    }

    public ContentsFilterLogicContainer getFilterLogicContainer() {
        return filterLogicContainer;
    }

    public void setPickupItems(boolean pickupItems) {
        getUpgradeWrapper().setPickupItems(pickupItems);
        sendBooleanToServer(DATA_PICKUP_ITEMS, pickupItems);
    }

    public boolean shouldPickupItems() {
        return getUpgradeWrapper().shouldPickupItems();
    }

    public void setPickupXp(boolean pickupXp) {
        getUpgradeWrapper().setPickupXp(pickupXp);
        sendBooleanToServer(DATA_PICKUP_XP, pickupXp);
    }

    public boolean shouldPickupXp() {
        return getUpgradeWrapper().shouldPickupXp();
    }
}
