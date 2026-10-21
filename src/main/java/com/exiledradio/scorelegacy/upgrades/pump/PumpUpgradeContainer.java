/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.pump;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class PumpUpgradeContainer extends UpgradeContainerBase<PumpUpgradeWrapper, PumpUpgradeContainer> {
    private static final String DATA_IS_INPUT = "isInput";
    private static final String DATA_INTERACT_WITH_HAND = "interactWithHand";
    private static final String DATA_INTERACT_WITH_WORLD = "interactWithWorld";
    private final FluidFilterContainer fluidFilterContainer;

    public PumpUpgradeContainer(EntityPlayer player, int containerId, PumpUpgradeWrapper wrapper, UpgradeHandler upgradeHandler,
                                UpgradeContainerType<PumpUpgradeWrapper, PumpUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        fluidFilterContainer = new FluidFilterContainer(player, this, () -> getUpgradeWrapper().getFluidFilterLogic());
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_IS_INPUT)) {
            setIsInput(data.getBoolean(DATA_IS_INPUT));
        } else if (data.hasKey(DATA_INTERACT_WITH_HAND)) {
            setInteractWithHand(data.getBoolean(DATA_INTERACT_WITH_HAND));
        } else if (data.hasKey(DATA_INTERACT_WITH_WORLD)) {
            setInteractWithWorld(data.getBoolean(DATA_INTERACT_WITH_WORLD));
        } else {
            fluidFilterContainer.handleMessage(data);
        }
    }

    public FluidFilterContainer getFluidFilterContainer() {
        return fluidFilterContainer;
    }

    public void setIsInput(boolean input) {
        getUpgradeWrapper().setIsInput(input);
        sendBooleanToServer(DATA_IS_INPUT, input);
    }

    public boolean isInput() {
        return getUpgradeWrapper().isInput();
    }

    public void setInteractWithHand(boolean interactWithHand) {
        getUpgradeWrapper().setInteractWithHand(interactWithHand);
        sendBooleanToServer(DATA_INTERACT_WITH_HAND, interactWithHand);
    }

    public boolean shouldInteractWithHand() {
        return getUpgradeWrapper().shouldInteractWithHand();
    }

    public void setInteractWithWorld(boolean interactWithWorld) {
        getUpgradeWrapper().setInteractWithWorld(interactWithWorld);
        sendBooleanToServer(DATA_INTERACT_WITH_WORLD, interactWithWorld);
    }

    public boolean shouldInteractWithWorld() {
        return getUpgradeWrapper().shouldInteractWithWorld();
    }
}
