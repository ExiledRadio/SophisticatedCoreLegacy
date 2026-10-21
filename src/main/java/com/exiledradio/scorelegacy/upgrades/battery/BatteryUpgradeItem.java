/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.battery;

import com.exiledradio.scorelegacy.config.BatteryUpgradeConfig;

import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.api.UpgradeSlotChangeResult;
import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Forge Energy storage that takes two columns of the backpack's slots; one per backpack. */
public class BatteryUpgradeItem extends UpgradeItemBase<BatteryUpgradeWrapper> {
    public static final UpgradeType<BatteryUpgradeWrapper> TYPE = new UpgradeType<>(BatteryUpgradeWrapper::new);

    private final BatteryUpgradeConfig config;

    public BatteryUpgradeItem(String name, CreativeTabs tab, BatteryUpgradeConfig config) {
        super(name, tab);
        this.config = config;
    }

    public BatteryUpgradeConfig getConfig() {
        return config;
    }

    @Override
    public UpgradeType<BatteryUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public int getInventoryColumnsTaken() {
        return 2;
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack, boolean firstLevelBackpack) {
        UpgradeSlotChangeResult limits = com.exiledradio.scorelegacy.api.UpgradeLimits.check(this, storageWrapper);
        if (!limits.isSuccessful()) {
            return limits;
        }
        Set<Integer> errorUpgradeSlots = new HashSet<>();
        for (Map.Entry<Integer, IUpgradeWrapper> entry : storageWrapper.getUpgradeHandler().getSlotWrappers().entrySet()) {
            if (entry.getValue() instanceof BatteryUpgradeWrapper) {
                errorUpgradeSlots.add(entry.getKey());
            }
        }
        if (!errorUpgradeSlots.isEmpty()) {
            return UpgradeSlotChangeResult.fail("add.battery_exists", errorUpgradeSlots, Collections.emptySet());
        }
        int multiplierRequired = (int) Math.ceil((float) BatteryUpgradeWrapper.getEnergyStored(upgradeStack) / BatteryUpgradeWrapper.getMaxEnergyStored(storageWrapper, config));
        if (multiplierRequired > 1) {
            return UpgradeSlotChangeResult.fail("add.battery_energy_high", Collections.emptySet(), Collections.emptySet(), multiplierRequired);
        }
        return UpgradeSlotChangeResult.SUCCESS;
    }
}
