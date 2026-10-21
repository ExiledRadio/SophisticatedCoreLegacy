/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.common.gui;

import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import net.minecraft.entity.player.EntityPlayer;

public class UpgradeContainerType<W extends IUpgradeWrapper, C extends UpgradeContainerBase<W, C>> {
    private final IFactory<W, C> factory;

    public UpgradeContainerType(IFactory<W, C> factory) {
        this.factory = factory;
    }

    public C create(EntityPlayer player, int containerId, W wrapper, UpgradeHandler upgradeHandler) {
        return factory.create(player, containerId, wrapper, upgradeHandler, this);
    }

    public interface IFactory<W extends IUpgradeWrapper, C extends UpgradeContainerBase<W, C>> {
        C create(EntityPlayer player, int containerId, W upgrade, UpgradeHandler upgradeHandler, UpgradeContainerType<W, C> type);
    }
}
