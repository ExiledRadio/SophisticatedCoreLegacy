/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.tank;

import com.exiledradio.scorelegacy.config.TankUpgradeConfig;

import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.api.UpgradeSlotChangeResult;
import com.exiledradio.scorelegacy.api.UpgradeType;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeItemBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Fluid storage that takes two columns of the backpack's slots; at most two per backpack. */
public class TankUpgradeItem extends UpgradeItemBase<TankUpgradeWrapper> {
    public static final UpgradeType<TankUpgradeWrapper> TYPE = new UpgradeType<>(TankUpgradeWrapper::new);

    private final TankUpgradeConfig config;

    public TankUpgradeItem(String name, CreativeTabs tab, TankUpgradeConfig config) {
        super(name, tab);
        this.config = config;
    }

    public TankUpgradeConfig getConfig() {
        return config;
    }

    @Override
    public UpgradeType<TankUpgradeWrapper> getType() {
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
            if (entry.getValue() instanceof TankUpgradeWrapper) {
                errorUpgradeSlots.add(entry.getKey());
            }
        }
        if (errorUpgradeSlots.size() >= 2) {
            return UpgradeSlotChangeResult.fail("add.two_tank_upgrades_present", errorUpgradeSlots, Collections.emptySet());
        }
        FluidStack contents = TankUpgradeWrapper.getContents(upgradeStack);
        int multiplierRequired = contents == null ? 0 : (int) Math.ceil((float) contents.amount / TankUpgradeWrapper.getTankCapacity(storageWrapper, config));
        if (multiplierRequired > 1) {
            return UpgradeSlotChangeResult.fail("add.tank_capacity_high", Collections.emptySet(), Collections.emptySet(), multiplierRequired);
        }
        return UpgradeSlotChangeResult.SUCCESS;
    }
}
