/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui;

import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** Client half of the upgrade registry: which tab GUI goes with which upgrade container type. */
public final class UpgradeGuiManager {
    private static final Map<UpgradeContainerType<?, ?>, IUpgradeSettingsFactory<?>> UPGRADE_TABS = new HashMap<>();
    private static final Map<UpgradeContainerType<?, ?>, IInventoryPartFactory<?>> INVENTORY_PARTS = new HashMap<>();

    private UpgradeGuiManager() {}

    public static <C extends UpgradeContainerBase<?, ?>> void registerTab(UpgradeContainerType<?, ?> containerType, IUpgradeSettingsFactory<C> factory) {
        UPGRADE_TABS.put(containerType, factory);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <C extends UpgradeContainerBase<?, ?>> UpgradeSettingsTab<C> getTab(C container, Position position, StorageScreenBase screen) {
        IUpgradeSettingsFactory<C> factory = (IUpgradeSettingsFactory<C>) UPGRADE_TABS.get(container.getType());
        return factory == null ? null : factory.create(container, position, screen);
    }

    public static <C extends UpgradeContainerBase<?, ?>> void registerInventoryPart(UpgradeContainerType<?, ?> containerType, IInventoryPartFactory<C> factory) {
        INVENTORY_PARTS.put(containerType, factory);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <C extends UpgradeContainerBase<?, ?>> UpgradeInventoryPartBase<C> getInventoryPart(int upgradeSlot, C container, Position position, int height, StorageScreenBase screen) {
        IInventoryPartFactory<C> factory = (IInventoryPartFactory<C>) INVENTORY_PARTS.get(container.getType());
        return factory == null ? null : factory.create(upgradeSlot, container, position, height, screen);
    }

    public interface IInventoryPartFactory<C extends UpgradeContainerBase<?, ?>> {
        UpgradeInventoryPartBase<C> create(int upgradeSlot, C container, Position position, int height, StorageScreenBase screen);
    }

    public interface IUpgradeSettingsFactory<C extends UpgradeContainerBase<?, ?>> {
        UpgradeSettingsTab<C> create(C container, Position position, StorageScreenBase screen);
    }
}
