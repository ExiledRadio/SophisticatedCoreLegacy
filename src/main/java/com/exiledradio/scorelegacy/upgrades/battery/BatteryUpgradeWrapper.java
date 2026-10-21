/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.battery;

import com.exiledradio.scorelegacy.config.BatteryUpgradeConfig;
import com.exiledradio.scorelegacy.api.IStackableContentsUpgrade;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Forge Energy storage. Size and transfer rate grow with rows and stack upgrades. An item in
 * the input slot is discharged into the battery, one in the output slot is charged from it.
 */
public class BatteryUpgradeWrapper extends UpgradeWrapperBase<BatteryUpgradeWrapper, BatteryUpgradeItem>
        implements IEnergyStorage, ITickableUpgrade, IStackableContentsUpgrade {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final String ENERGY_STORED_TAG = "energyStored";
    private static final String INVENTORY_TAG = "inventory";
    private final ItemStackHandler inventory;
    private int energyStored;

    public BatteryUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
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
                return (slot == INPUT_SLOT || slot == OUTPUT_SLOT) && isValidEnergyItem(stack, slot == OUTPUT_SLOT);
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
        energyStored = getEnergyStored(upgrade);
    }

    public static int getEnergyStored(ItemStack upgrade) {
        return NBTHelper.getInt(upgrade, ENERGY_STORED_TAG, 0);
    }

    @Nullable
    private static IEnergyStorage getEnergyStorage(ItemStack stack) {
        return stack.isEmpty() || !stack.hasCapability(CapabilityEnergy.ENERGY, null) ? null : stack.getCapability(CapabilityEnergy.ENERGY, null);
    }

    private static boolean isValidEnergyItem(ItemStack stack, boolean isOutput) {
        IEnergyStorage storage = getEnergyStorage(stack);
        return storage != null && (isOutput || storage.getEnergyStored() > 0);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = Math.max(0, Math.min(getMaxEnergyStored() - energyStored, Math.min(getMaxInOut(), maxReceive)));
        if (!simulate && received > 0) {
            energyStored += received;
            serializeEnergyStored();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.max(0, Math.min(energyStored, Math.min(getMaxInOut(), maxExtract)));
        if (!simulate && extracted > 0) {
            energyStored -= extracted;
            serializeEnergyStored();
        }
        return extracted;
    }

    private void serializeEnergyStored() {
        NBTHelper.setInt(upgrade, ENERGY_STORED_TAG, energyStored);
        save();
    }

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return getMaxEnergyStored(storageWrapper, upgradeItem.getConfig());
    }

    public static int getMaxEnergyStored(IStorageWrapper storageWrapper, BatteryUpgradeConfig config) {
        int stackMultiplier = getAdjustedStackMultiplier(storageWrapper, config);
        int maxEnergyBase = getMaxEnergyBase(storageWrapper, config);
        return Integer.MAX_VALUE / stackMultiplier < maxEnergyBase ? Integer.MAX_VALUE : maxEnergyBase * stackMultiplier;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public static int getAdjustedStackMultiplier(IStorageWrapper storageWrapper, BatteryUpgradeConfig config) {
        return 1 + (int) (config.stackMultiplierRatio * (storageWrapper.getInventoryHandler().getStackSizeMultiplier() - 1));
    }

    private static int getMaxEnergyBase(IStorageWrapper storageWrapper, BatteryUpgradeConfig config) {
        return config.energyPerSlotRow * storageWrapper.getNumberOfSlotRows();
    }

    private int getMaxInOut() {
        return upgradeItem.getConfig().maxInputOutput * storageWrapper.getNumberOfSlotRows() * getAdjustedStackMultiplier(storageWrapper, upgradeItem.getConfig());
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (energyStored < getMaxEnergyStored()) {
            IEnergyStorage input = getEnergyStorage(inventory.getStackInSlot(INPUT_SLOT));
            if (input != null) {
                int toReceive = input.extractEnergy(receiveEnergy(getMaxInOut(), true), true);
                if (toReceive > 0) {
                    receiveEnergy(input.extractEnergy(toReceive, false), false);
                }
            }
        }
        if (energyStored > 0) {
            IEnergyStorage output = getEnergyStorage(inventory.getStackInSlot(OUTPUT_SLOT));
            if (output != null) {
                int toExtract = output.receiveEnergy(extractEnergy(getMaxInOut(), true), true);
                if (toExtract > 0) {
                    output.receiveEnergy(extractEnergy(toExtract, false), false);
                }
            }
        }
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    @Override
    public int getMinimumMultiplierRequired() {
        return (int) Math.ceil((float) energyStored / getMaxEnergyBase(storageWrapper, upgradeItem.getConfig()));
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }
}
