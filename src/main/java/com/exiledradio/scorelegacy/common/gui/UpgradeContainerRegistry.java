/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.common.gui;

import com.exiledradio.scorelegacy.api.IUpgradeItem;
import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** Which upgrade items get a settings tab, and the container half of that tab. */
public final class UpgradeContainerRegistry {
    private static final Map<Item, UpgradeContainerType<?, ?>> UPGRADE_CONTAINERS = new HashMap<>();

    private UpgradeContainerRegistry() {}

    public static void register(Item upgradeItem, UpgradeContainerType<?, ?> containerType) {
        UPGRADE_CONTAINERS.put(upgradeItem, containerType);
    }

    @Nullable
    public static UpgradeContainerBase<?, ?> instantiateContainer(EntityPlayer player, int containerId, IUpgradeWrapper wrapper, UpgradeHandler upgradeHandler) {
        Item item = wrapper.getUpgradeStack().getItem();
        if (!(item instanceof IUpgradeItem<?>) || wrapper.hideSettingsTab() || !UPGRADE_CONTAINERS.containsKey(item)) {
            return null;
        }
        return create(UPGRADE_CONTAINERS.get(item), player, containerId, wrapper, upgradeHandler);
    }

    @SuppressWarnings("unchecked")
    private static <W extends IUpgradeWrapper, C extends UpgradeContainerBase<W, C>> C create(UpgradeContainerType<?, ?> type, EntityPlayer player, int containerId, IUpgradeWrapper wrapper, UpgradeHandler upgradeHandler) {
        return ((UpgradeContainerType<W, C>) type).create(player, containerId, (W) wrapper, upgradeHandler);
    }
}
