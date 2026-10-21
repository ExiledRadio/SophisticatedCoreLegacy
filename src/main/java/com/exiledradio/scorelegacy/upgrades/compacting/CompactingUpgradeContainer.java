/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.compacting;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.FilterLogicContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class CompactingUpgradeContainer extends UpgradeContainerBase<CompactingUpgradeWrapper, CompactingUpgradeContainer> {
    private static final String DATA_SHOULD_WORK_IN_GUI = "shouldWorkdInGUI";
    private static final String DATA_SHOULD_COMPACT_NON_UNCRAFTABLE = "shouldCompactNonUncraftable";
    private final FilterLogicContainer<FilterLogic> filterLogicContainer;

    public CompactingUpgradeContainer(EntityPlayer player, int containerId, CompactingUpgradeWrapper wrapper, UpgradeHandler upgradeHandler, UpgradeContainerType<CompactingUpgradeWrapper, CompactingUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        filterLogicContainer = new FilterLogicContainer<>(() -> getUpgradeWrapper().getFilterLogic(), this, slots::add);
    }

    public FilterLogicContainer<FilterLogic> getFilterLogicContainer() {
        return filterLogicContainer;
    }

    public void setCompactNonUncraftable(boolean shouldCompactNonUncraftable) {
        getUpgradeWrapper().setCompactNonUncraftable(shouldCompactNonUncraftable);
        sendBooleanToServer(DATA_SHOULD_COMPACT_NON_UNCRAFTABLE, shouldCompactNonUncraftable);
    }

    public boolean shouldCompactNonUncraftable() {
        return getUpgradeWrapper().shouldCompactNonUncraftable();
    }

    public void setShouldWorkInGUI(boolean shouldWorkInGUI) {
        getUpgradeWrapper().setShouldWorkdInGUI(shouldWorkInGUI);
        sendBooleanToServer(DATA_SHOULD_WORK_IN_GUI, shouldWorkInGUI);
    }

    public boolean shouldWorkInGUI() {
        return getUpgradeWrapper().shouldWorkInGUI();
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_SHOULD_COMPACT_NON_UNCRAFTABLE)) {
            setCompactNonUncraftable(data.getBoolean(DATA_SHOULD_COMPACT_NON_UNCRAFTABLE));
        } else if (data.hasKey(DATA_SHOULD_WORK_IN_GUI)) {
            setShouldWorkInGUI(data.getBoolean(DATA_SHOULD_WORK_IN_GUI));
        } else {
            filterLogicContainer.handleMessage(data);
        }
    }
}
