package com.exiledradio.scorelegacy.inventory;

import com.exiledradio.scorelegacy.api.IIOFilterUpgrade;
import com.exiledradio.scorelegacy.api.IInventoryWrapperUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.settings.SettingsHandler;
import com.exiledradio.scorelegacy.util.SortBy;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.stack.StackUpgradeItem;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
/**
 * The single live view of one storage's contents. Every stack, tile entity and open container
 * for the same UUID goes through the same instance, so no two handlers can hold diverging copies
 * of the same items and later save over each other (the classic backpack dupe).
 */
public class StorageContents {
    private static final String SORT_BY_TAG = "sortBy";
    @Nullable
    private final UUID uuid;
    private final NBTTagCompound contentsNbt;
    private final Runnable markDirty;
    private final Policy policy;
    @Nullable
    private InventoryHandler inventoryHandler = null;
    @Nullable
    private UpgradeHandler upgradeHandler = null;
    @Nullable
    private SettingsHandler settingsHandler = null;
    // Derived views, rebuilt whenever the set of active upgrades changes.
    @Nullable
    private IItemHandlerSimpleInserter inventoryForUpgradeProcessing = null;
    @Nullable
    private IItemHandlerSimpleInserter inventoryForInputOutput = null;

    public StorageContents(@Nullable UUID uuid, NBTTagCompound contentsNbt, Runnable markDirty, Policy policy) {
        this.uuid = uuid;
        this.contentsNbt = contentsNbt;
        this.markDirty = markDirty;
        this.policy = policy;
    }

    public Policy getPolicy() {
        return policy;
    }

    /**
     * What the owning mod decides about its storage's contents: extra rules for what may go in
     * (Backpacks: backpacks only with Inception, never into themselves) and which items never
     * stack past their normal size.
     */
    public interface Policy {
        Policy DEFAULT = new Policy() {};

        default boolean isItemValid(StorageContents contents, int slot, ItemStack stack) {
            return true;
        }

        default String[] getNonStackableItems() {
            return new String[0];
        }

        /**
         * How many normal stacks one slot holds before any stack upgrade (Storage's limited
         * barrels: 8 to 160). Stack upgrades multiply on top of it.
         */
        default int getBaseStackSizeMultiplier() {
            return 1;
        }
    }

    /** Contents for a stack that has no UUID yet on the client; never stored anywhere. */
    public static StorageContents temporary(Policy policy) {
        return new StorageContents(null, new NBTTagCompound(), () -> {}, policy);
    }

    @Nullable
    public UUID getUuid() {
        return uuid;
    }

    public InventoryHandler getInventoryHandler(int minimumSlots) {
        if (inventoryHandler == null) {
            inventoryHandler = new InventoryHandler(minimumSlots, contentsNbt, markDirty, this);
            updateInventoryForUpgrades();
        } else if (inventoryHandler.getSlots() < minimumSlots) {
            // A tier upgrade only ever adds slots; the existing items keep their positions.
            inventoryHandler.grow(minimumSlots);
            updateInventoryForUpgrades();
        }
        return inventoryHandler;
    }

    public UpgradeHandler getUpgradeHandler(int minimumSlots, IStorageWrapper owner) {
        if (upgradeHandler == null) {
            upgradeHandler = new UpgradeHandler(minimumSlots, owner, contentsNbt, markDirty, this::onUpgradesChanged);
            // The inventory may already exist (settings can create it first); its limit needs the upgrades.
            updateInventoryForUpgrades();
        } else if (upgradeHandler.getSlots() < minimumSlots) {
            upgradeHandler.grow(minimumSlots);
        }
        return upgradeHandler;
    }

    /** The settings, with the categories the owning storage adds. Created on first use. */
    public SettingsHandler getSettingsHandler(IStorageWrapper owner) {
        if (settingsHandler == null) {
            settingsHandler = new SettingsHandler(contentsNbt, markDirty);
            owner.addSettingsCategories(settingsHandler);
        }
        return settingsHandler;
    }

    /** The settings if a wrapper has already created them; memory slot rules are skipped until then. */
    @Nullable
    public SettingsHandler getExistingSettingsHandler() {
        return settingsHandler;
    }

    public SortBy getSortBy() {
        return SortBy.fromName(contentsNbt.getString(SORT_BY_TAG));
    }

    public void setSortBy(SortBy sortBy) {
        contentsNbt.setString(SORT_BY_TAG, sortBy.getName());
        markDirty.run();
    }

    /** Client side: replaces the local copy of the upgrade inventory with the server's. */
    public void applyUpgradeInventoryFromServer(NBTTagCompound upgradeInventory) {
        contentsNbt.setTag(UpgradeHandler.UPGRADE_INVENTORY_TAG, upgradeInventory);
        if (upgradeHandler != null) {
            upgradeHandler.reloadFromNbt(upgradeInventory);
        }
    }

    /** The upgrade handler if a wrapper has already created it; inventory hooks are skipped until then. */
    @Nullable
    public UpgradeHandler getExistingUpgradeHandler() {
        return upgradeHandler;
    }

    private void onUpgradesChanged() {
        inventoryForUpgradeProcessing = null;
        inventoryForInputOutput = null;
        updateInventoryForUpgrades();
    }

    private void updateInventoryForUpgrades() {
        if (inventoryHandler != null && upgradeHandler != null) {
            long limit = (long) StackUpgradeItem.getInventorySlotLimit(upgradeHandler) * Math.max(1, policy.getBaseStackSizeMultiplier());
            inventoryHandler.setSlotLimit((int) Math.min(Integer.MAX_VALUE, limit));
            inventoryHandler.refreshParts();
            int slots = inventoryHandler.getSlots();
            inventoryHandler.setUsableSlots(slots - getNumberOfRows(slots) * upgradeHandler.getColumnsTaken());
        }
    }

    /** Rows of the backpack GUI: 9 slots to a row up to 81 slots, 12 above that. */
    public static int getNumberOfRows(int inventorySlots) {
        return (int) Math.ceil(inventorySlots <= 81 ? inventorySlots / 9.0 : inventorySlots / 12.0);
    }

    /** The inventory upgrades work on: the backpack, plus nested backpacks with Inception. */
    public IItemHandlerSimpleInserter getInventoryForUpgradeProcessing(IStorageWrapper wrapper) {
        if (inventoryForUpgradeProcessing == null) {
            IItemHandlerSimpleInserter wrapped = wrapper.getInventoryHandler();
            for (IInventoryWrapperUpgrade upgrade : wrapper.getUpgradeHandler().getWrappersThatImplement(IInventoryWrapperUpgrade.class)) {
                wrapped = upgrade.wrapInventory(wrapped);
            }
            inventoryForUpgradeProcessing = wrapped;
        }
        return inventoryForUpgradeProcessing;
    }

    /** What hoppers, pipes and other mods see: the upgrade inventory behind any filter upgrades. */
    public IItemHandlerSimpleInserter getInventoryForInputOutput(IStorageWrapper wrapper) {
        if (inventoryForInputOutput == null) {
            List<FilterLogic> inputFilters = new ArrayList<>();
            List<FilterLogic> outputFilters = new ArrayList<>();
            for (IIOFilterUpgrade upgrade : wrapper.getUpgradeHandler().getWrappersThatImplement(IIOFilterUpgrade.class)) {
                if (upgrade.getInputFilter() != null) {
                    inputFilters.add(upgrade.getInputFilter());
                }
                if (upgrade.getOutputFilter() != null) {
                    outputFilters.add(upgrade.getOutputFilter());
                }
            }
            IItemHandlerSimpleInserter processing = getInventoryForUpgradeProcessing(wrapper);
            inventoryForInputOutput = inputFilters.isEmpty() && outputFilters.isEmpty() ? processing : new FilteredItemHandler(processing, inputFilters, outputFilters);
        }
        return inventoryForInputOutput;
    }

    public void refreshInventoryForInputOutput() {
        inventoryForInputOutput = null;
    }

    public void refreshInventoryForUpgradeProcessing() {
        inventoryForUpgradeProcessing = null;
        inventoryForInputOutput = null;
    }
}
