/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.cooking;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class SmeltingUpgradeContainer extends UpgradeContainerBase<SmeltingUpgradeWrapper, SmeltingUpgradeContainer> {
    private final CookingLogicContainer cookingLogicContainer;

    public SmeltingUpgradeContainer(EntityPlayer player, int containerId, SmeltingUpgradeWrapper wrapper, UpgradeHandler upgradeHandler,
                                    UpgradeContainerType<SmeltingUpgradeWrapper, SmeltingUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        cookingLogicContainer = new CookingLogicContainer(() -> getUpgradeWrapper().getCookingLogic(), slots::add);
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        //noop
    }

    public CookingLogicContainer getCookingLogicContainer() {
        return cookingLogicContainer;
    }
}
