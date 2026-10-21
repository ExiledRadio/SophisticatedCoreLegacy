/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.common.gui;

import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.network.CorePacketHandler;
import com.exiledradio.scorelegacy.network.SyncContainerClientDataMessage;
import com.exiledradio.scorelegacy.network.SyncSlotStackMessage;
import com.exiledradio.scorelegacy.settings.ISettingsCategory;
import com.exiledradio.scorelegacy.settings.SettingsCategoryContainer;
import com.exiledradio.scorelegacy.settings.SettingsContainerRegistry;
import com.exiledradio.scorelegacy.settings.SettingsSync;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * The settings screen of a storage: its inventory shown read-only (clicks select slots for the
 * open settings tab instead of moving items), plus one small container per settings category.
 */
// Inventory Tweaks would sort these by clicking slots, which fights stack-upgraded slots and
// memory/no-sort slots; storages have their own sort button.
@invtweaks.api.container.IgnoreContainer
public abstract class SettingsContainerBase extends Container implements ISyncedContainer {
    public static final String CATEGORY_NAME_TAG = "categoryName";
    private static final String ACTION_TAG = "action";
    private static final String BACK_ACTION = "back";
    private static final int HIGH_COUNT_THRESHOLD = 127;

    protected final EntityPlayer player;
    protected final IStorageWrapper storageWrapper;
    private final StorageBackgroundProperties backgroundProperties;
    private final int slotsOnLine;
    private final int numberOfSlots;
    private final Map<String, SettingsCategoryContainer<?>> categoryContainers = new LinkedHashMap<>();
    private final SettingsSync settingsSync = new SettingsSync();

    protected SettingsContainerBase(EntityPlayer player, IStorageWrapper storageWrapper) {
        this.player = player;
        this.storageWrapper = storageWrapper;
        InventoryHandler inventory = storageWrapper.getInventoryHandler();
        int allSlots = Math.min(inventory.getSlots(), storageWrapper.getNumberOfInventorySlots());
        backgroundProperties = allSlots <= 81 ? StorageBackgroundProperties.REGULAR : StorageBackgroundProperties.WIDE;
        int columnsTaken = storageWrapper.getUpgradeHandler().getColumnsTaken();
        slotsOnLine = backgroundProperties.getSlotsOnLine() - columnsTaken;
        numberOfSlots = allSlots - storageWrapper.getNumberOfSlotRows() * columnsTaken;
        for (int slot = 0; slot < numberOfSlots; slot++) {
            int[] position = getStorageSlotPosition(slot);
            addSlotToContainer(new ViewOnlySlot(inventory, slot, position[0], position[1]));
        }
        storageWrapper.getSettingsHandler().getSettingsCategories().forEach(this::addCategoryContainer);
    }

    private void addCategoryContainer(String name, ISettingsCategory category) {
        SettingsCategoryContainer<?> container = SettingsContainerRegistry.create(this, name, category);
        if (container != null) {
            categoryContainers.put(name, container);
        }
    }

    @Override
    public IStorageWrapper getStorageWrapper() {
        return storageWrapper;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public StorageBackgroundProperties getBackgroundProperties() {
        return backgroundProperties;
    }

    public int getSlotsOnLine() {
        return slotsOnLine;
    }

    public int getNumberOfSlots() {
        return numberOfSlots;
    }

    public int getNumberOfRows() {
        return storageWrapper.getNumberOfSlotRows();
    }

    /**
     * Where storage slot slot goes, {x, y}: rows of {@link #getSlotsOnLine()} by default.
     * Called from the constructor, so an override may only use the storage wrapper.
     */
    protected int[] getStorageSlotPosition(int slot) {
        int onLine = backgroundProperties.getSlotsOnLine() - storageWrapper.getUpgradeHandler().getColumnsTaken();
        return new int[] {8 + (slot % onLine) * 18, 18 + (slot / onLine) * 18};
    }

    /** Height of the storage slots area: 18 per row by default. */
    public int getStorageInventoryHeight() {
        return getNumberOfRows() * 18;
    }

    /** Settings that make no sense for this storage get no tab (limited barrels: item display and no-sort). */
    public boolean isSettingsCategoryDisabled(String categoryName) {
        return false;
    }

    public void forEachCategoryContainer(BiConsumer<String, SettingsCategoryContainer<?>> consumer) {
        categoryContainers.forEach(consumer);
    }

    /** What a memory slot remembers, or EMPTY. */
    public ItemStack getMemorizedStack(int slotIndex) {
        MemorySettingsCategory memory = storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class);
        return memory == null ? ItemStack.EMPTY : memory.getSlotFilterStack(slotIndex);
    }

    /** The title the screen shows (client side, already translated). */
    public abstract String getStorageTitle();

    /** Server side: back to the storage's own GUI. */
    protected abstract void openStorageGuiAgain(EntityPlayerMP playerMP);

    public void backToStorage() {
        if (player.world.isRemote) {
            CorePacketHandler.CHANNEL.sendToServer(new SyncContainerClientDataMessage(NBTHelper.withString(ACTION_TAG, BACK_ACTION)));
        } else if (player instanceof EntityPlayerMP) {
            openStorageGuiAgain((EntityPlayerMP) player);
        }
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (BACK_ACTION.equals(data.getString(ACTION_TAG))) {
            backToStorage();
            return;
        }
        SettingsCategoryContainer<?> container = categoryContainers.get(data.getString(CATEGORY_NAME_TAG));
        if (container != null) {
            container.handleMessage(data);
        }
    }

    /** Nothing in the settings screen moves items. */
    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer playerIn) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void detectAndSendChanges() {
        settingsSync.sendIfChanged(player, windowId, storageWrapper);
        for (int i = 0; i < inventorySlots.size(); ++i) {
            ItemStack current = inventorySlots.get(i).getStack();
            ItemStack previous = inventoryItemStacks.get(i);
            if (!ItemStack.areItemStacksEqual(previous, current)) {
                ItemStack copy = current.isEmpty() ? ItemStack.EMPTY : current.copy();
                inventoryItemStacks.set(i, copy);
                for (IContainerListener listener : listeners) {
                    // Vanilla sends counts as a byte; stack-upgraded slots go out in full.
                    if (copy.getCount() > HIGH_COUNT_THRESHOLD && listener instanceof EntityPlayerMP) {
                        CorePacketHandler.CHANNEL.sendTo(new SyncSlotStackMessage(windowId, i, copy), (EntityPlayerMP) listener);
                    } else {
                        listener.sendSlotContents(this, i, copy);
                    }
                }
            }
        }
    }

    /** Shows the storage's items; never lets anything be taken or put. */
    private static class ViewOnlySlot extends SlotItemHandler {
        ViewOnlySlot(InventoryHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return false;
        }
    }
}
