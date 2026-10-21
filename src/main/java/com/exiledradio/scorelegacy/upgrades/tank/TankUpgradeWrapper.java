/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.tank;

import com.exiledradio.scorelegacy.config.TankUpgradeConfig;
import com.exiledradio.scorelegacy.api.IStackableContentsUpgrade;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * One fluid tank. Capacity and the per-operation transfer rate grow with the backpack's rows
 * and stack upgrades. An item in the input slot is emptied into the tank, one in the output
 * slot is filled from it, a bucket's worth at a time. An empty tank is null (1.12 fluids).
 */
public class TankUpgradeWrapper extends UpgradeWrapperBase<TankUpgradeWrapper, TankUpgradeItem> implements ITickableUpgrade, IStackableContentsUpgrade {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private static final String CONTENTS_TAG = "contents";
    private static final String INVENTORY_TAG = "inventory";
    private final ItemStackHandler inventory;
    @Nullable
    private FluidStack contents;
    private long cooldownTime = 0;

    public TankUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        inventory = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                upgrade.setTagInfo(INVENTORY_TAG, serializeNBT());
                save();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return (slot == INPUT_SLOT || slot == OUTPUT_SLOT) && isValidFluidItem(stack, slot == OUTPUT_SLOT);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        NBTTagCompound tag = upgrade.getTagCompound();
        if (tag != null && tag.hasKey(INVENTORY_TAG)) {
            inventory.deserializeNBT(tag.getCompoundTag(INVENTORY_TAG));
        }
        contents = getContents(upgrade);
    }

    @Nullable
    public static FluidStack getContents(ItemStack upgrade) {
        NBTTagCompound tag = upgrade.getTagCompound();
        return tag != null && tag.hasKey(CONTENTS_TAG) ? FluidStack.loadFluidStackFromNBT(tag.getCompoundTag(CONTENTS_TAG)) : null;
    }

    @Nullable
    private static IFluidHandlerItem getFluidHandler(ItemStack stack) {
        return stack.isEmpty() || !stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
                ? null : stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    private boolean isValidFluidItem(ItemStack stack, boolean isOutput) {
        IFluidHandlerItem fluidHandler = getFluidHandler(stack.copy());
        return fluidHandler != null && isValidFluidHandler(fluidHandler, isOutput);
    }

    /** Output: something the tank's fluid can go into. Input: something holding fluid the tank takes. */
    private boolean isValidFluidHandler(IFluidHandlerItem fluidHandler, boolean isOutput) {
        boolean tankEmpty = contents == null;
        for (IFluidTankProperties tank : fluidHandler.getTankProperties()) {
            FluidStack fluidInTank = tank.getContents();
            if ((isOutput && (fluidInTank == null || (!tankEmpty && fluidInTank.getFluid() == contents.getFluid())))
                    || (!isOutput && fluidInTank != null && (tankEmpty || contents.getFluid() == fluidInTank.getFluid()))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public FluidStack getContents() {
        return contents;
    }

    public int getTankCapacity() {
        return getTankCapacity(storageWrapper, upgradeItem.getConfig());
    }

    public static int getAdjustedStackMultiplier(IStorageWrapper storageWrapper, TankUpgradeConfig config) {
        return 1 + (int) (config.stackMultiplierRatio * (storageWrapper.getInventoryHandler().getStackSizeMultiplier() - 1));
    }

    private static int getBaseCapacity(IStorageWrapper storageWrapper, TankUpgradeConfig config) {
        return config.capacityPerSlotRow * storageWrapper.getNumberOfSlotRows();
    }

    public static int getTankCapacity(IStorageWrapper storageWrapper, TankUpgradeConfig config) {
        int stackMultiplier = getAdjustedStackMultiplier(storageWrapper, config);
        int baseCapacity = getBaseCapacity(storageWrapper, config);
        return Integer.MAX_VALUE / stackMultiplier < baseCapacity ? Integer.MAX_VALUE : baseCapacity * stackMultiplier;
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    private int getMaxInOut() {
        return Math.max(Fluid.BUCKET_VOLUME, upgradeItem.getConfig().maxInputOutput * storageWrapper.getNumberOfSlotRows() * getAdjustedStackMultiplier(storageWrapper, upgradeItem.getConfig()));
    }

    public int fill(@Nullable FluidStack resource, boolean doFill, boolean ignoreInOutLimit) {
        int capacity = getTankCapacity();
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
        int amount = contents == null ? 0 : contents.amount;
        if (amount >= capacity || (contents != null && !contents.isFluidEqual(resource))) {
            return 0;
        }
        int toFill = Math.min(capacity - amount, resource.amount);
        if (!ignoreInOutLimit) {
            toFill = Math.min(getMaxInOut(), toFill);
        }
        if (doFill && toFill > 0) {
            if (contents == null) {
                contents = new FluidStack(resource, toFill);
            } else {
                contents.amount += toFill;
            }
            serializeContents();
        }
        return toFill;
    }

    @Nullable
    public FluidStack drain(int maxDrain, boolean doDrain, boolean ignoreInOutLimit) {
        if (contents == null || maxDrain <= 0) {
            return null;
        }
        int toDrain = Math.min(maxDrain, contents.amount);
        if (!ignoreInOutLimit) {
            toDrain = Math.min(getMaxInOut(), toDrain);
        }
        FluidStack drained = new FluidStack(contents, toDrain);
        if (doDrain) {
            if (toDrain == contents.amount) {
                contents = null;
            } else {
                contents.amount -= toDrain;
            }
            serializeContents();
        }
        return drained;
    }

    private void serializeContents() {
        if (contents == null) {
            NBTTagCompound tag = upgrade.getTagCompound();
            if (tag != null) {
                tag.removeTag(CONTENTS_TAG);
            }
        } else {
            upgrade.setTagInfo(CONTENTS_TAG, contents.writeToNBT(new NBTTagCompound()));
        }
        save();
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (world.getTotalWorldTime() < cooldownTime) {
            return;
        }
        boolean didSomething = false;
        IFluidHandlerItem input = getFluidHandler(inventory.getStackInSlot(INPUT_SLOT));
        if (input != null) {
            didSomething = drainHandler(input, stack -> inventory.setStackInSlot(INPUT_SLOT, stack));
        }
        IFluidHandlerItem output = getFluidHandler(inventory.getStackInSlot(OUTPUT_SLOT));
        if (output != null) {
            didSomething |= fillHandler(output, stack -> inventory.setStackInSlot(OUTPUT_SLOT, stack));
        }
        if (didSomething) {
            cooldownTime = world.getTotalWorldTime() + upgradeItem.getConfig().autoFillDrainContainerCooldown;
        }
    }

    /** Fills the item from the tank (up to a bucket); hands back the item's new form. */
    public boolean fillHandler(IFluidHandlerItem fluidHandler, Consumer<ItemStack> updateContainerStack) {
        if (contents == null || !isValidFluidHandler(fluidHandler, true)) {
            return false;
        }
        int filled = fluidHandler.fill(new FluidStack(contents, Math.min(Fluid.BUCKET_VOLUME, contents.amount)), false);
        if (filled <= 0) {
            return false;
        }
        FluidStack drained = drain(filled, true, false);
        fluidHandler.fill(drained, true);
        updateContainerStack.accept(fluidHandler.getContainer());
        return true;
    }

    /** Empties the item into the tank (up to a bucket); hands back the item's new form. */
    public boolean drainHandler(IFluidHandlerItem fluidHandler, Consumer<ItemStack> updateContainerStack) {
        if (!isValidFluidHandler(fluidHandler, false)) {
            return false;
        }
        FluidStack extracted = contents == null
                ? fluidHandler.drain(Fluid.BUCKET_VOLUME, false)
                : fluidHandler.drain(new FluidStack(contents, Math.min(Fluid.BUCKET_VOLUME, getTankCapacity() - contents.amount)), false);
        if (extracted == null || extracted.amount <= 0) {
            return false;
        }
        int filled = fill(extracted, false, false);
        if (filled <= 0) {
            return false;
        }
        // Only take from the item what the tank really accepts (a bucket is all or nothing).
        FluidStack actuallyDrained = fluidHandler.drain(new FluidStack(extracted, filled), true);
        if (actuallyDrained == null || actuallyDrained.amount <= 0) {
            return false;
        }
        fill(actuallyDrained, true, true);
        updateContainerStack.accept(fluidHandler.getContainer());
        return true;
    }

    @Override
    public int getMinimumMultiplierRequired() {
        return contents == null ? 1 : (int) Math.ceil((float) contents.amount / getBaseCapacity(storageWrapper, upgradeItem.getConfig()));
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }
}
