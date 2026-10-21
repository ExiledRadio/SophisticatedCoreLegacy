/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.api;

import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.inventory.StorageFluidHandler;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.settings.SettingsHandler;
import com.exiledradio.scorelegacy.settings.main.MainSettingsCategory;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import com.exiledradio.scorelegacy.settings.nosort.NoSortSettingsCategory;
import com.exiledradio.scorelegacy.util.InventorySorter;
import com.exiledradio.scorelegacy.util.SortBy;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeWrapper;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * One storage (a backpack stack, a barrel, a chest...): its inventory, upgrades and colours.
 * Upgrades, containers and screens only ever talk to this interface, so the same upgrade code
 * works in every mod built on Sophisticated Core Legacy.
 */
public interface IStorageWrapper {

    InventoryHandler getInventoryHandler();

    UpgradeHandler getUpgradeHandler();

    /** The inventory upgrades work on (Backpacks: plus nested backpacks with Inception). */
    IItemHandlerSimpleInserter getInventoryForUpgradeProcessing();

    /** What hoppers, pipes and other mods see: the upgrade inventory behind any filter upgrades. */
    IItemHandlerSimpleInserter getInventoryForInputOutput();

    void refreshInventoryForInputOutput();

    void refreshInventoryForUpgradeProcessing();

    /** The upgrade tab that was open last, or -1. */
    int getOpenTabId();

    void setOpenTabId(int openTabId);

    int getNumberOfInventorySlots();

    int getNumberOfUpgradeSlots();

    /** Rows of the storage GUI: 9 slots to a row up to 81 slots, 12 above that. */
    default int getNumberOfSlotRows() {
        int slots = getNumberOfInventorySlots();
        return (int) Math.ceil(slots <= 81 ? slots / 9.0 : slots / 12.0);
    }

    @Nullable
    UUID getContentsUuid();

    int getMainColor();

    int getAccentColor();

    /** Settings (memory, no-sort, ...), kept with the contents. */
    SettingsHandler getSettingsHandler();

    /**
     * Adds this storage's settings categories, in tab order. The default gives every storage the
     * main, no-sort and memory categories; storages add more (Backpacks: item display).
     */
    default void addSettingsCategories(SettingsHandler handler) {
        handler.addCategory(MainSettingsCategory.NAME, (nbt, save) -> new MainSettingsCategory(getPlayerSettingsTagName(), nbt, save));
        handler.addCategory(NoSortSettingsCategory.NAME, NoSortSettingsCategory::new);
        handler.addCategory(MemorySettingsCategory.NAME, (nbt, save) -> new MemorySettingsCategory(this::getInventoryHandler, nbt, save));
    }

    /*
     * Lang keys for the settings screen's own wording, which differs per mod (the original
     * Backpacks says "Backpack Settings", Storage just "Settings"). Core's lang has the backpack
     * wording, so a storage mod only overrides these to use keys from its own lang file.
     */

    /** Whether the item display can go on another face than the front (the original's IDisplaySideStorage: double chests). */
    default boolean canChangeDisplaySide() {
        return false;
    }

    default String getSettingsTitleKey() {
        return "gui." + com.exiledradio.scorelegacy.SophisticatedCoreLegacy.MODID + ".settings.title";
    }

    default String getSettingsTooltipKey() {
        return "gui." + com.exiledradio.scorelegacy.SophisticatedCoreLegacy.MODID + ".settings.tooltip";
    }

    default String getBackToStorageTooltipKey() {
        return "gui." + com.exiledradio.scorelegacy.SophisticatedCoreLegacy.MODID + ".back_to_backpack.tooltip";
    }

    /** Tooltip of the contents filters' "match what this storage holds" state ("Match Backpack Contents" / "Match Storage Contents"). */
    default String getMatchContentsButtonKey() {
        return "gui." + com.exiledradio.scorelegacy.SophisticatedCoreLegacy.MODID + ".upgrades.buttons.match_backpack_contents";
    }

    /** The storage type that per-storage upgrade limits are configured for ("barrel", "chest"...); empty for none. */
    default String getStorageType() {
        return "";
    }

    /** The main settings tab's label; ".tooltip" is appended for its tooltip (the original Storage says "Storage"). */
    default String getMainSettingsTabKey() {
        return "gui." + com.exiledradio.scorelegacy.SophisticatedCoreLegacy.MODID + ".settings.backpack";
    }

    /** The main settings tab's icon; empty for Core's backpack icon (the original Storage shows the storage itself). */
    default net.minecraft.item.ItemStack getMainSettingsTabIcon() {
        return net.minecraft.item.ItemStack.EMPTY;
    }

    /** The main settings' "this storage" context button; ".tooltip" and ".tooltip_detail" are appended for its tooltip. */
    default String getStorageContextButtonKey() {
        return "gui." + com.exiledradio.scorelegacy.SophisticatedCoreLegacy.MODID + ".settings.buttons.context_backpack";
    }

    /** Where this mod keeps a player's own defaults for the main settings. */
    default String getPlayerSettingsTagName() {
        return "sophisticatedStorageSettings";
    }

    SortBy getSortBy();

    void setSortBy(SortBy sortBy);

    /** Sorts the inventory, leaving no-sort and memory slots where they are. */
    /**
     * An upgrade was added, removed or saved its own state (a tank's fluid, a battery's charge),
     * after the wrappers are up to date. Backpacks update what their model shows from it.
     */
    default void onUpgradeContentsChanged() {
    }

    default void sort() {
        InventorySorter.sort(this);
    }

    /** The item stack this storage is, or EMPTY for a placed block. */
    default ItemStack getWrappedStorageStack() {
        return ItemStack.EMPTY;
    }

    /** All tanks as one fluid handler, or null without a tank upgrade. */
    @Nullable
    default StorageFluidHandler getFluidHandler() {
        StorageFluidHandler handler = new StorageFluidHandler(this);
        return handler.hasTanks() ? handler : null;
    }

    /** The battery upgrade, or null without one. */
    @Nullable
    default IEnergyStorage getEnergyStorage() {
        List<BatteryUpgradeWrapper> batteries = getUpgradeHandler().getTypeWrappers(BatteryUpgradeItem.TYPE);
        return batteries.isEmpty() ? null : batteries.get(0);
    }
}
