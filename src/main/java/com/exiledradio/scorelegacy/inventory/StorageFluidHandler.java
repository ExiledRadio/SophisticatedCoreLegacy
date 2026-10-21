/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.inventory;

import com.exiledradio.scorelegacy.api.IStorageWrapper;

import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeWrapper;
import net.minecraft.item.ItemStack;
import com.exiledradio.scorelegacy.init.ModFluids;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/** All of a backpack's tanks as one fluid handler, for pipes on a placed backpack. */
public class StorageFluidHandler implements IFluidHandlerItem {
    private final IStorageWrapper storageWrapper;

    public StorageFluidHandler(IStorageWrapper storageWrapper) {
        this.storageWrapper = storageWrapper;
    }

    private List<TankUpgradeWrapper> getAllTanks() {
        return storageWrapper.getUpgradeHandler().getTypeWrappers(TankUpgradeItem.TYPE);
    }

    public boolean hasTanks() {
        return !getAllTanks().isEmpty();
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        List<TankUpgradeWrapper> tanks = getAllTanks();
        IFluidTankProperties[] properties = new IFluidTankProperties[tanks.size()];
        for (int i = 0; i < tanks.size(); i++) {
            FluidStack contents = tanks.get(i).getContents();
            properties[i] = new FluidTankProperties(contents == null ? null : contents.copy(), tanks.get(i).getTankCapacity());
        }
        return properties;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
        int filled = 0;
        for (TankUpgradeWrapper tank : getAllTanks()) {
            filled += tank.fill(new FluidStack(resource, resource.amount - filled), doFill, false);
            if (filled >= resource.amount) {
                break;
            }
        }
        return filled;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0) {
            return null;
        }
        int drained = 0;
        for (TankUpgradeWrapper tank : getAllTanks()) {
            FluidStack contents = tank.getContents();
            if (contents != null && contents.isFluidEqual(resource)) {
                FluidStack fromTank = tank.drain(resource.amount - drained, doDrain, false);
                drained += fromTank == null ? 0 : fromTank.amount;
                if (drained >= resource.amount) {
                    break;
                }
            }
        }
        return drained == 0 ? null : new FluidStack(resource, drained);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        for (TankUpgradeWrapper tank : getAllTanks()) {
            FluidStack drained = tank.drain(maxDrain, doDrain, false);
            if (drained != null && drained.amount > 0) {
                return drained;
            }
        }
        return null;
    }

    /**
     * Puts liquid experience into tanks that already hold an experience fluid, then empty ones
     * (as xpjuice). Returns the mB that went in.
     */
    public int fillExperience(int amount, boolean doFill, boolean ignoreInOutLimit) {
        Fluid defaultXp = ModFluids.getExperienceFluid();
        int filled = 0;
        for (int pass = 0; pass < 2 && filled < amount; pass++) {
            for (TankUpgradeWrapper tank : getAllTanks()) {
                FluidStack contents = tank.getContents();
                Fluid fluid = pass == 0 ? (contents != null && ModFluids.isExperience(contents.getFluid()) ? contents.getFluid() : null)
                        : (contents == null ? defaultXp : null);
                if (fluid != null) {
                    filled += tank.fill(new FluidStack(fluid, amount - filled), doFill, ignoreInOutLimit);
                    if (filled >= amount) {
                        break;
                    }
                }
            }
        }
        return filled;
    }

    /** Takes liquid experience from any tank holding an experience fluid. Returns the mB taken. */
    public int drainExperience(int maxAmount, boolean doDrain, boolean ignoreInOutLimit) {
        int drained = 0;
        for (TankUpgradeWrapper tank : getAllTanks()) {
            FluidStack contents = tank.getContents();
            if (contents != null && ModFluids.isExperience(contents.getFluid())) {
                FluidStack fromTank = tank.drain(maxAmount - drained, doDrain, ignoreInOutLimit);
                drained += fromTank == null ? 0 : fromTank.amount;
                if (drained >= maxAmount) {
                    break;
                }
            }
        }
        return drained;
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return storageWrapper.getWrappedStorageStack();
    }
}
