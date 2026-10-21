/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.voiding;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.FilterLogicContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class VoidUpgradeContainer extends UpgradeContainerBase<VoidUpgradeWrapper, VoidUpgradeContainer> {
    private static final String DATA_SHOULD_WORK_IN_GUI = "shouldWorkdInGUI";
    private static final String DATA_SHOULD_VOID_OVERFLOW = "shouldVoidOverflow";
    private final FilterLogicContainer<FilterLogic> filterLogicContainer;

    public VoidUpgradeContainer(EntityPlayer player, int containerId, VoidUpgradeWrapper wrapper, UpgradeHandler upgradeHandler, UpgradeContainerType<VoidUpgradeWrapper, VoidUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        filterLogicContainer = new FilterLogicContainer<>(() -> getUpgradeWrapper().getFilterLogic(), this, slots::add);
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_SHOULD_WORK_IN_GUI)) {
            setShouldWorkInGUI(data.getBoolean(DATA_SHOULD_WORK_IN_GUI));
        } else if (data.hasKey(DATA_SHOULD_VOID_OVERFLOW)) {
            setShouldVoidOverflow(data.getBoolean(DATA_SHOULD_VOID_OVERFLOW));
        } else {
            filterLogicContainer.handleMessage(data);
        }
    }

    public FilterLogicContainer<FilterLogic> getFilterLogicContainer() {
        return filterLogicContainer;
    }

    public void setShouldWorkInGUI(boolean shouldWorkInGUI) {
        getUpgradeWrapper().setShouldWorkdInGUI(shouldWorkInGUI);
        sendBooleanToServer(DATA_SHOULD_WORK_IN_GUI, shouldWorkInGUI);
    }

    public void setShouldVoidOverflow(boolean shouldVoidOverflow) {
        getUpgradeWrapper().setShouldVoidOverflow(shouldVoidOverflow);
        sendBooleanToServer(DATA_SHOULD_VOID_OVERFLOW, shouldVoidOverflow);
    }

    public boolean shouldWorkInGUI() {
        return getUpgradeWrapper().shouldWorkInGUI();
    }

    public boolean shouldVoidOverflow() {
        return getUpgradeWrapper().shouldVoidOverflow();
    }
}
