/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.common.gui;


import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import com.exiledradio.scorelegacy.api.IUpgradeItem;
import com.exiledradio.scorelegacy.api.IOverflowResponseUpgrade;
import com.exiledradio.scorelegacy.api.ISlotChangeResponseUpgrade;
import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.api.UpgradeSlotChangeResult;
import com.exiledradio.scorelegacy.inventory.InventoryHandler;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.network.CorePacketHandler;
import com.exiledradio.scorelegacy.network.SyncContainerClientDataMessage;
import com.exiledradio.scorelegacy.network.SyncSlotStackMessage;
import com.exiledradio.scorelegacy.util.NBTHelper;
import com.exiledradio.scorelegacy.util.SortBy;
import com.exiledradio.scorelegacy.settings.ISlotColorCategory;
import com.exiledradio.scorelegacy.settings.SettingsSync;
import com.exiledradio.scorelegacy.settings.main.MainSettingsCategory;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Slot layout: backpack slots [0, n), then the 36 player slots, then the upgrade slots, then the
 * slots of every upgrade's settings tab. Positions follow the original so its GUI textures line
 * up unchanged. When upgrades are added or removed the server reopens this GUI rather than
 * reshuffling slots under an open container.
 */
// Inventory Tweaks would sort these by clicking slots, which fights stack-upgraded slots and
// memory/no-sort slots; storages have their own sort button.
@invtweaks.api.container.IgnoreContainer
public abstract class StorageContainerBase extends Container implements ISyncedContainer {
    public static final String EMPTY_UPGRADE_SLOT_SPRITE = SophisticatedCoreLegacy.MODID + ":item/empty_upgrade_slot";
    /** Upgrade-tab slots that aren't on screen sit here. */
    public static final int DISABLED_SLOT_X_POS = -1000;
    private static final int NUMBER_OF_PLAYER_SLOTS = 36;
    private static final int HIGH_COUNT_THRESHOLD = 127;
    private static final String OPEN_TAB_ID_TAG = "openTabId";
    private static final String UPGRADE_ENABLED_TAG = "upgradeEnabled";
    private static final String UPGRADE_SLOT_TAG = "upgradeSlot";
    private static final String SORT_BY_TAG = "sortBy";
    private static final String ACTION_TAG = "action";
    private static final String SORT_ACTION = "sort";
    private static final String OPEN_SETTINGS_ACTION = "openSettings";

    protected final EntityPlayer player;
    protected final IStorageWrapper storageWrapper;
    private final SettingsSync settingsSync = new SettingsSync();
    /** The player inventory slot holding the open storage (a carried backpack), or -1. */
    private final int lockedMainInventorySlot;
    private final StorageBackgroundProperties backgroundProperties;
    private final int numberOfInventorySlots;
    private final int columnsTaken;
    private final int numberOfUpgradeSlots;
    private final Map<Integer, UpgradeContainerBase<?, ?>> upgradeContainers = new LinkedHashMap<>();
    private int lockedSlotNumber = -1;
    private boolean reopenPending = false;
    private boolean resendHighCounts = false;
    @Nullable
    private UpgradeSlotChangeResult lastUpgradeError = null;
    private long lastUpgradeErrorTime = 0;

    protected StorageContainerBase(EntityPlayer player, IStorageWrapper storageWrapper, int lockedMainInventorySlot) {
        this.player = player;
        this.storageWrapper = storageWrapper;
        this.lockedMainInventorySlot = lockedMainInventorySlot;

        InventoryHandler inventoryHandler = storageWrapper.getInventoryHandler();
        int allInventorySlots = Math.min(inventoryHandler.getSlots(), storageWrapper.getNumberOfInventorySlots());
        backgroundProperties = allInventorySlots <= 81 ? StorageBackgroundProperties.REGULAR : StorageBackgroundProperties.WIDE;
        columnsTaken = storageWrapper.getUpgradeHandler().getColumnsTaken();
        numberOfInventorySlots = allInventorySlots - storageWrapper.getNumberOfSlotRows() * columnsTaken;

        int yPosition = addBackpackInventorySlots(inventoryHandler);
        addPlayerInventorySlots(player.inventory, yPosition);
        UpgradeHandler upgradeHandler = storageWrapper.getUpgradeHandler();
        numberOfUpgradeSlots = Math.min(upgradeHandler.getSlots(), storageWrapper.getNumberOfUpgradeSlots());
        addUpgradeSlots(upgradeHandler, yPosition);
        addUpgradeSettingsContainers(upgradeHandler);
    }

    // --- layout ------------------------------------------------------------------------------

    private int addBackpackInventorySlots(InventoryHandler inventoryHandler) {
        for (int slotIndex = 0; slotIndex < numberOfInventorySlots; slotIndex++) {
            int[] position = getStorageSlotPosition(slotIndex);
            addSlotToContainer(new StorageInventorySlot(inventoryHandler, slotIndex, position[0], position[1]));
        }
        return getStorageInventoryHeight() + 18;
    }

    /**
     * Where storage slot slotIndex goes, {x, y}: rows of {@link #getSlotsOnLine()} by default.
     * Called from the constructor, so an override may only use the storage wrapper.
     */
    protected int[] getStorageSlotPosition(int slotIndex) {
        int slotsOnLine = getSlotsOnLine();
        return new int[] {8 + (slotIndex % slotsOnLine) * 18, 18 + (slotIndex / slotsOnLine) * 18};
    }

    /** Height of the storage slots area: 18 per row by default (Storage's limited barrels: a fixed 82). */
    public int getStorageInventoryHeight() {
        return getNumberOfRows() * 18;
    }

    private void addPlayerInventorySlots(InventoryPlayer playerInventory, int yPosition) {
        int xOffset = backgroundProperties.getPlayerInventoryXOffset();
        int lockedIndex = lockedMainInventorySlot;
        int y = yPosition + 14;
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addPlayerSlot(playerInventory, column + row * 9 + 9, xOffset + 8 + column * 18, y, lockedIndex);
            }
            y += 18;
        }
        y += 4;
        for (int column = 0; column < 9; ++column) {
            addPlayerSlot(playerInventory, column, xOffset + 8 + column * 18, y, lockedIndex);
        }
    }

    private void addPlayerSlot(InventoryPlayer playerInventory, int index, int x, int y, int lockedIndex) {
        if (index == lockedIndex) {
            Slot slot = addSlotToContainer(new LockedSlot(playerInventory, index, x, y));
            lockedSlotNumber = slot.slotNumber;
        } else {
            addSlotToContainer(new Slot(playerInventory, index, x, y));
        }
    }

    private void addUpgradeSlots(UpgradeHandler upgradeHandler, int lastInventoryRowY) {
        int y = lastInventoryRowY - 22 * numberOfUpgradeSlots;
        for (int slotIndex = 0; slotIndex < numberOfUpgradeSlots; slotIndex++) {
            addSlotToContainer(new StorageUpgradeSlot(upgradeHandler, slotIndex, -15, y));
            y += 22;
        }
    }

    private void addUpgradeSettingsContainers(UpgradeHandler upgradeHandler) {
        for (Map.Entry<Integer, IUpgradeWrapper> entry : upgradeHandler.getSlotWrappers().entrySet()) {
            UpgradeContainerBase<?, ?> container = UpgradeContainerRegistry.instantiateContainer(player, entry.getKey(), entry.getValue(), upgradeHandler);
            if (container != null) {
                upgradeContainers.put(entry.getKey(), container);
                for (Slot slot : container.getSlots()) {
                    slot.xPos = DISABLED_SLOT_X_POS;
                    addSlotToContainer(slot);
                }
            }
        }
        UpgradeContainerBase<?, ?> openContainer = upgradeContainers.get(storageWrapper.getOpenTabId());
        if (openContainer != null) {
            openContainer.setIsOpen(true);
        }
    }

    // --- accessors ---------------------------------------------------------------------------

    @Override
    public IStorageWrapper getStorageWrapper() {
        return storageWrapper;
    }

    public StorageBackgroundProperties getBackgroundProperties() {
        return backgroundProperties;
    }

    public int getNumberOfInventorySlots() {
        return numberOfInventorySlots;
    }

    public int getNumberOfUpgradeSlots() {
        return numberOfUpgradeSlots;
    }

    public int getNumberOfRows() {
        return storageWrapper.getNumberOfSlotRows();
    }

    public int getSlotsOnLine() {
        return backgroundProperties.getSlotsOnLine() - columnsTaken;
    }

    public int getColumnsTaken() {
        return columnsTaken;
    }

    public Map<Integer, UpgradeContainerBase<?, ?>> getUpgradeContainers() {
        return upgradeContainers;
    }

    private int getFirstPlayerSlot() {
        return numberOfInventorySlots;
    }

    public int getFirstUpgradeSlot() {
        return numberOfInventorySlots + NUMBER_OF_PLAYER_SLOTS;
    }

    private int getFirstUpgradeSettingsSlot() {
        return getFirstUpgradeSlot() + numberOfUpgradeSlots;
    }

    public boolean isBackpackInventorySlot(int slotNumber) {
        return slotNumber >= 0 && slotNumber < numberOfInventorySlots;
    }

    private boolean isUpgradeSlot(int slotNumber) {
        return slotNumber >= getFirstUpgradeSlot() && slotNumber < getFirstUpgradeSettingsSlot();
    }

    /** The last refusal to add or remove an upgrade, for the GUI to show for a few seconds. */
    @Nullable
    public UpgradeSlotChangeResult getRecentUpgradeError() {
        if (lastUpgradeError != null && player.world.getTotalWorldTime() - lastUpgradeErrorTime > 60) {
            lastUpgradeError = null;
        }
        return lastUpgradeError;
    }

    private void reportUpgradeError(UpgradeSlotChangeResult result) {
        if (!result.isSuccessful() && player.world.isRemote) {
            lastUpgradeError = result;
            lastUpgradeErrorTime = player.world.getTotalWorldTime();
        }
    }

    // --- validity ----------------------------------------------------------------------------

    /**
     * Checked by the server every tick; returning false closes the GUI. Implementations check
     * that the storage is still exactly where it was opened from (the same stack instance, the
     * same tile entity), so nothing can be moved into a storage that is gone.
     */
    @Override
    public abstract boolean canInteractWith(@Nonnull EntityPlayer playerIn);

    /** Where the storage is placed, or null when it is carried (an item). */
    @Nullable
    public abstract BlockPos getStorageBlockPos();

    /** The title the screen shows (client side, already translated). */
    public abstract String getStorageTitle();

    /** Server side: opens this storage's GUI again, after the set of upgrades changed. */
    protected abstract void openGuiAgain(EntityPlayerMP playerMP);

    // --- clicks ------------------------------------------------------------------------------

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer playerIn) {
        return clampForClickVerification(slotClickInner(slotId, dragType, clickType, playerIn));
    }

    /**
     * The client sends the stack its click returned so the server can check it predicted the
     * same thing, and vanilla writes that count as a byte. Without clamping, every click on a
     * slot holding more than 127 would look like a misprediction and trigger a full resync.
     */
    private static ItemStack clampForClickVerification(ItemStack result) {
        if (result.getCount() <= HIGH_COUNT_THRESHOLD) {
            return result;
        }
        ItemStack clamped = result.copy();
        clamped.setCount(HIGH_COUNT_THRESHOLD);
        return clamped;
    }

    /**
     * Vanilla calls this with false right before it resends the whole window after a click it
     * didn't predict. That resend cuts counts to a byte, so the big stacks go out again after.
     */
    @Override
    public void setCanCraft(EntityPlayer player, boolean canCraft) {
        super.setCanCraft(player, canCraft);
        if (!canCraft) {
            resendHighCounts = true;
        }
    }

    private ItemStack slotClickInner(int slotId, int dragType, ClickType clickType, EntityPlayer playerIn) {
        if (isLockedSlotAction(slotId, dragType, clickType)) {
            return ItemStack.EMPTY;
        }
        Slot slot = slotId >= 0 && slotId < inventorySlots.size() ? inventorySlots.get(slotId) : null;
        if (slot instanceof IFilterSlot) {
            return clickFilterSlot(slot, clickType, playerIn);
        }
        if (isBackpackInventorySlot(slotId) && slot != null) {
            // Order matters: "works in GUI" voiding eats what won't fit, then the rest is merged.
            if ((clickType == ClickType.PICKUP || clickType == ClickType.SWAP) && handleOverflow(slotId, clickType, dragType, playerIn)) {
                return ItemStack.EMPTY;
            }
            ItemStack merged = mergeCursorIntoSlot(slot, dragType, clickType, playerIn);
            if (merged != null) {
                return merged;
            }
            ItemStack handled = clickHighCountSlot(slot, dragType, clickType, playerIn);
            if (handled != null) {
                return handled;
            }
        }

        List<Item> upgradesBefore = getUpgradeItems();
        ItemStack ret = super.slotClick(slotId, dragType, clickType, playerIn);
        if (!playerIn.world.isRemote && !upgradesBefore.equals(getUpgradeItems())) {
            reopenPending = true;
        }
        return ret;
    }

    private boolean isLockedSlotAction(int slotId, int dragType, ClickType clickType) {
        if (lockedSlotNumber < 0) {
            return false;
        }
        if (slotId == lockedSlotNumber) {
            return true;
        }
        // Number keys swap the hovered slot with that hotbar slot.
        return clickType == ClickType.SWAP && dragType == lockedMainInventorySlot;
    }

    /** Ghost slots take a copy of the cursor item (one of it) and never move real items. */
    private ItemStack clickFilterSlot(Slot slot, ClickType clickType, EntityPlayer playerIn) {
        if (clickType != ClickType.PICKUP) {
            return ItemStack.EMPTY;
        }
        ItemStack cursorStack = playerIn.inventory.getItemStack().copy();
        if (cursorStack.getCount() > 1) {
            cursorStack.setCount(1);
        }
        if (cursorStack.isEmpty() || slot.isItemValid(cursorStack)) {
            slot.putStack(cursorStack);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Clicking a matching stack onto a backpack slot. Vanilla 1.12 caps that merge at the item's
     * normal stack size, so with stack upgrades a slot could never be filled past 64 by hand.
     * Returns null when this isn't such a click.
     */
    @Nullable
    private ItemStack mergeCursorIntoSlot(Slot slot, int dragType, ClickType clickType, EntityPlayer playerIn) {
        ItemStack cursor = playerIn.inventory.getItemStack();
        ItemStack slotStack = slot.getStack();
        if (clickType != ClickType.PICKUP || cursor.isEmpty() || slotStack.isEmpty() || !slot.isItemValid(cursor)
                || slotStack.getItem() != cursor.getItem() || slotStack.getMetadata() != cursor.getMetadata() || !ItemStack.areItemStackTagsEqual(slotStack, cursor)) {
            return null;
        }
        ItemStack before = slotStack.copy();
        int toMove = Math.min(dragType == 0 ? cursor.getCount() : 1, slot.getItemStackLimit(cursor) - slotStack.getCount());
        if (toMove > 0) {
            cursor.shrink(toMove);
            slotStack.grow(toMove);
            slot.onSlotChanged();
        }
        return before;
    }

    /**
     * Stack-upgraded slots can hold far more than one stack. Vanilla would happily put all of it
     * on the cursor or into one hotbar slot; these cap every such move at a normal stack.
     * Returns null when vanilla handling is fine.
     */
    @Nullable
    private ItemStack clickHighCountSlot(Slot slot, int dragType, ClickType clickType, EntityPlayer playerIn) {
        ItemStack slotStack = slot.getStack();
        int maxStackSize = slotStack.getMaxStackSize();
        if (slotStack.getCount() <= maxStackSize || !slot.canTakeStack(playerIn)) {
            return null;
        }
        InventoryPlayer inventory = playerIn.inventory;
        switch (clickType) {
            case PICKUP:
                if (!inventory.getItemStack().isEmpty()) {
                    return null;
                }
                int toTake = dragType == 0 ? maxStackSize : (maxStackSize + 1) / 2;
                inventory.setItemStack(slot.decrStackSize(toTake));
                slot.onTake(playerIn, inventory.getItemStack());
                return ItemStack.EMPTY;
            case SWAP:
                if (dragType < 0 || dragType > 8 || !inventory.getStackInSlot(dragType).isEmpty()) {
                    return ItemStack.EMPTY;
                }
                ItemStack swapped = slot.decrStackSize(maxStackSize);
                inventory.setInventorySlotContents(dragType, swapped);
                slot.onTake(playerIn, swapped);
                return ItemStack.EMPTY;
            case THROW:
                if (!inventory.getItemStack().isEmpty()) {
                    return ItemStack.EMPTY;
                }
                ItemStack dropped = slot.decrStackSize(dragType == 0 ? 1 : maxStackSize);
                slot.onTake(playerIn, dropped);
                playerIn.dropItem(dropped, true);
                return ItemStack.EMPTY;
            default:
                return null;
        }
    }

    /** "Works in GUI" void upgrades eat what is placed into a matching, already full backpack slot. */
    private boolean handleOverflow(int slotId, ClickType clickType, int dragType, EntityPlayer playerIn) {
        InventoryPlayer inventory = playerIn.inventory;
        ItemStack cursorStack = clickType == ClickType.SWAP ? (dragType >= 0 && dragType < 9 ? inventory.getStackInSlot(dragType) : ItemStack.EMPTY) : inventory.getItemStack();
        Slot slot = inventorySlots.get(slotId);
        if (cursorStack.isEmpty() || !slot.isItemValid(cursorStack)) {
            return false;
        }
        ItemStack slotStack = slot.getStack();
        if (slotStack.getItem() != cursorStack.getItem() || !ItemStack.areItemStackTagsEqual(slotStack, cursorStack)) {
            return false;
        }
        int space = slot.getItemStackLimit(cursorStack) - slotStack.getCount();
        if (space >= cursorStack.getCount()) {
            return false;
        }
        ItemStack overflow = cursorStack.copy();
        int overflowCount = cursorStack.getCount() - Math.max(0, space);
        overflow.setCount(overflowCount);
        ItemStack result = overflow;
        for (IOverflowResponseUpgrade upgrade : storageWrapper.getUpgradeHandler().getWrappersThatImplement(IOverflowResponseUpgrade.class)) {
            if (upgrade.worksInGui()) {
                result = upgrade.onOverflow(result);
                if (result.isEmpty()) {
                    break;
                }
            }
        }
        if (result.getCount() < overflowCount) {
            cursorStack.shrink(overflowCount - result.getCount());
            if (cursorStack.isEmpty()) {
                if (clickType == ClickType.SWAP) {
                    inventory.setInventorySlotContents(dragType, ItemStack.EMPTY);
                } else {
                    inventory.setItemStack(ItemStack.EMPTY);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canDragIntoSlot(Slot slotIn) {
        return !(slotIn instanceof IFilterSlot) && !(slotIn instanceof StorageUpgradeSlot);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
        // Double-click collecting must never pull from a crafting result (it would craft).
        return !(slotIn instanceof IFilterSlot) && !(slotIn instanceof SlotCrafting);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack() || !slot.canTakeStack(playerIn) || slot instanceof IFilterSlot) {
            return ItemStack.EMPTY;
        }
        if (index >= getFirstUpgradeSettingsSlot()) {
            return transferFromUpgradeTabSlot(playerIn, slot);
        }
        ItemStack slotStack = slot.getStack();
        ItemStack original = slotStack.copy();

        boolean intoOpenTab = !isUpgradeSlot(index) && getMainSetting(MainSettingsCategory.SHIFT_CLICK_INTO_OPEN_TAB_FIRST) && mergeIntoOpenTab(slotStack);
        if (intoOpenTab) {
            // Handled below like any other move.
        } else if (index < getFirstPlayerSlot() || isUpgradeSlot(index)) {
            if (isUpgradeSlot(index) && !canRemoveUpgrade(slot)) {
                return ItemStack.EMPTY;
            }
            if (!mergeItemStack(slotStack, getFirstPlayerSlot(), getFirstUpgradeSlot(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = slotStack.getItem() instanceof IUpgradeItem && mergeIntoUpgradeSlots(slotStack);
            if (!moved && !mergeIntoBackpack(slotStack)) {
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        if (slotStack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(playerIn, slotStack);
        return original;
    }

    /**
     * Shift-click on a slot inside an upgrade tab (crafting grid, crafting result, furnace
     * slots): the tab decides whether the backpack or the player's inventory is tried first.
     * The result-slot handling mirrors vanilla's crafting table.
     */
    private ItemStack transferFromUpgradeTabSlot(EntityPlayer playerIn, Slot slot) {
        UpgradeContainerBase<?, ?> owner = null;
        for (UpgradeContainerBase<?, ?> container : upgradeContainers.values()) {
            if (container.getSlots().contains(slot)) {
                owner = container;
                break;
            }
        }
        if (owner == null) {
            return ItemStack.EMPTY;
        }
        ItemStack slotStack = slot.getStack();
        ItemStack original = slotStack.copy();
        boolean isCraftingResult = slot instanceof SlotCrafting;
        if (isCraftingResult) {
            slotStack.getItem().onCreated(slotStack, playerIn.world, playerIn);
        }
        boolean moved;
        if (owner.mergeIntoBackpackFirst(slot)) {
            moved = mergeIntoBackpack(slotStack);
            moved |= !slotStack.isEmpty() && mergeItemStack(slotStack, getFirstPlayerSlot(), getFirstUpgradeSlot(), true);
        } else {
            moved = mergeItemStack(slotStack, getFirstPlayerSlot(), getFirstUpgradeSlot(), true);
            moved |= !slotStack.isEmpty() && mergeIntoBackpack(slotStack);
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        slot.onSlotChange(slotStack, original);
        if (slotStack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        if (slotStack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = slot.onTake(playerIn, slotStack);
        if (isCraftingResult) {
            // What didn't fit of a crafted result can't go back into the grid.
            playerIn.dropItem(taken, false);
        }
        return original;
    }

    /**
     * Vanilla's merge, except that a stack put into an empty slot is also capped at the item's
     * own max stack size. Vanilla only caps at the slot's limit (64), which is fine for vanilla
     * stacks but lets a stack-upgraded slot of 6 unstackable compasses or 64 ender pearls land
     * in one player slot.
     */
    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean merged = false;
        if (stack.isStackable()) {
            int i = reverseDirection ? endIndex - 1 : startIndex;
            while (!stack.isEmpty() && (reverseDirection ? i >= startIndex : i < endIndex)) {
                Slot slot = inventorySlots.get(i);
                ItemStack inSlot = slot.getStack();
                if (!inSlot.isEmpty() && inSlot.getItem() == stack.getItem()
                        && (!stack.getHasSubtypes() || stack.getMetadata() == inSlot.getMetadata())
                        && ItemStack.areItemStackTagsEqual(stack, inSlot)) {
                    int limit = Math.min(slot.getItemStackLimit(stack), stack.getMaxStackSize());
                    int toMove = Math.min(stack.getCount(), limit - inSlot.getCount());
                    if (toMove > 0) {
                        stack.shrink(toMove);
                        inSlot.grow(toMove);
                        slot.onSlotChanged();
                        merged = true;
                    }
                }
                i += reverseDirection ? -1 : 1;
            }
        }
        if (!stack.isEmpty()) {
            int i = reverseDirection ? endIndex - 1 : startIndex;
            while (!stack.isEmpty() && (reverseDirection ? i >= startIndex : i < endIndex)) {
                Slot slot = inventorySlots.get(i);
                if (!slot.getHasStack() && slot.isItemValid(stack)) {
                    int limit = Math.min(slot.getItemStackLimit(stack), stack.getMaxStackSize());
                    slot.putStack(stack.splitStack(Math.min(stack.getCount(), limit)));
                    slot.onSlotChanged();
                    merged = true;
                    // One stack per call, as vanilla; its shift-click loop calls again for the rest.
                    break;
                }
                i += reverseDirection ? -1 : 1;
            }
        }
        return merged;
    }

    private boolean mergeIntoUpgradeSlots(ItemStack stack) {
        for (int slotNumber = getFirstUpgradeSlot(); slotNumber < getFirstUpgradeSettingsSlot(); slotNumber++) {
            Slot slot = inventorySlots.get(slotNumber);
            if (!slot.getHasStack() && slot.isItemValid(stack)) {
                slot.putStack(stack.splitStack(1));
                return true;
            }
        }
        return false;
    }

    /** Shift-click into the backpack: onto matching stacks first, then empty slots, up to the stack-upgrade limit. */
    /**
     * Shift-click into the open upgrade tab's real slots (crafting grid, furnace slots, tank IO
     * slots), onto matching stacks first. Filter slots and result slots never take items.
     */
    private boolean mergeIntoOpenTab(ItemStack stack) {
        UpgradeContainerBase<?, ?> open = null;
        for (UpgradeContainerBase<?, ?> container : upgradeContainers.values()) {
            if (container.isOpen()) {
                open = container;
                break;
            }
        }
        if (open == null) {
            return false;
        }
        int before = stack.getCount();
        for (int pass = 0; pass < 2 && !stack.isEmpty(); pass++) {
            for (Slot slot : open.getSlots()) {
                if (stack.isEmpty()) {
                    break;
                }
                if (slot instanceof IFilterSlot || slot instanceof SlotCrafting || !slot.isItemValid(stack)) {
                    continue;
                }
                ItemStack inSlot = slot.getStack();
                boolean skip = pass == 0 ? inSlot.isEmpty() || !ItemHandlerHelper.canItemStacksStack(inSlot, stack) : !inSlot.isEmpty();
                if (skip) {
                    continue;
                }
                int limit = Math.min(slot.getItemStackLimit(stack), stack.getMaxStackSize());
                int toMove = Math.min(stack.getCount(), limit - inSlot.getCount());
                if (toMove <= 0) {
                    continue;
                }
                if (inSlot.isEmpty()) {
                    slot.putStack(stack.splitStack(toMove));
                } else {
                    inSlot.grow(toMove);
                    stack.shrink(toMove);
                    slot.onSlotChanged();
                }
            }
        }
        return stack.getCount() != before;
    }

    private boolean mergeIntoBackpack(ItemStack stack) {
        InventoryHandler handler = storageWrapper.getInventoryHandler();
        int before = stack.getCount();
        for (int pass = 0; pass < 2 && !stack.isEmpty(); pass++) {
            for (int slot = 0; slot < numberOfInventorySlots && !stack.isEmpty(); slot++) {
                boolean slotEmpty = handler.getStackInSlot(slot).isEmpty();
                if ((pass == 0) == slotEmpty) {
                    continue;
                }
                ItemStack remaining = handler.insertItemFromGui(slot, stack, false);
                if (remaining.getCount() != stack.getCount()) {
                    stack.setCount(remaining.getCount());
                    onBackpackSlotChangedByPlayer(slot);
                }
            }
        }
        return stack.getCount() != before;
    }

    /** A tank or battery needs the last columns of slots empty, because it takes their place. */
    private UpgradeSlotChangeResult checkInventoryColumnsFree(ItemStack newUpgrade, ItemStack currentUpgrade) {
        int newColumns = ((IUpgradeItem<?>) newUpgrade.getItem()).getInventoryColumnsTaken();
        int currentColumns = currentUpgrade.getItem() instanceof IUpgradeItem ? ((IUpgradeItem<?>) currentUpgrade.getItem()).getInventoryColumnsTaken() : 0;
        if (newColumns <= currentColumns) {
            return UpgradeSlotChangeResult.SUCCESS;
        }
        int slotsToCheck = (newColumns - currentColumns) * getNumberOfRows();
        InventoryHandler handler = storageWrapper.getInventoryHandler();
        Set<Integer> errorSlots = new HashSet<>();
        for (int slot = numberOfInventorySlots - 1; slot >= Math.max(0, numberOfInventorySlots - slotsToCheck); slot--) {
            if (!handler.getStackInSlot(slot).isEmpty()) {
                errorSlots.add(slot);
            }
        }
        if (!errorSlots.isEmpty()) {
            return UpgradeSlotChangeResult.fail("add.needs_occupied_inventory_slots", Collections.emptySet(), errorSlots, slotsToCheck, newUpgrade.getDisplayName());
        }
        return UpgradeSlotChangeResult.SUCCESS;
    }

    private boolean canRemoveUpgrade(Slot slot) {
        ItemStack upgrade = slot.getStack();
        if (!(upgrade.getItem() instanceof IUpgradeItem)) {
            return true;
        }
        if (upgrade.getItem() instanceof com.exiledradio.scorelegacy.upgrades.infinity.InfinityUpgradeItem
                && !((com.exiledradio.scorelegacy.upgrades.infinity.InfinityUpgradeItem) upgrade.getItem()).canBeHandledBy(player)) {
            reportUpgradeError(UpgradeSlotChangeResult.fail("remove.infinity_upgrade_only_admin", java.util.Collections.emptySet(), java.util.Collections.emptySet()));
            return false;
        }
        UpgradeSlotChangeResult result = ((IUpgradeItem<?>) upgrade.getItem()).canRemoveUpgradeFrom(storageWrapper);
        reportUpgradeError(result);
        return result.isSuccessful();
    }

    private List<Item> getUpgradeItems() {
        List<Item> items = new ArrayList<>();
        UpgradeHandler handler = storageWrapper.getUpgradeHandler();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            items.add(handler.getStackInSlot(slot).getItem());
        }
        return items;
    }

    private void onBackpackSlotChangedByPlayer(int slot) {
        if (player.world.isRemote) {
            return;
        }
        InventoryHandler handler = storageWrapper.getInventoryHandler();
        for (ISlotChangeResponseUpgrade upgrade : storageWrapper.getUpgradeHandler().getWrappersThatImplementFromMainBackpack(ISlotChangeResponseUpgrade.class)) {
            upgrade.onSlotChange(handler, slot);
        }
    }

    // --- settings messages -------------------------------------------------------------------

    /** Applies a settings change; called on the server for messages from the client GUI. */
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(UpgradeContainerBase.CONTAINER_ID_TAG)) {
            UpgradeContainerBase<?, ?> container = upgradeContainers.get(data.getInteger(UpgradeContainerBase.CONTAINER_ID_TAG));
            if (container != null) {
                container.handleMessage(data);
            }
        } else if (data.hasKey(OPEN_TAB_ID_TAG)) {
            setOpenTabId(data.getInteger(OPEN_TAB_ID_TAG));
        } else if (data.hasKey(UPGRADE_ENABLED_TAG)) {
            setUpgradeEnabled(data.getInteger(UPGRADE_SLOT_TAG), data.getBoolean(UPGRADE_ENABLED_TAG));
        } else if (data.hasKey(SORT_BY_TAG)) {
            setSortBy(SortBy.fromName(data.getString(SORT_BY_TAG)));
        } else if (SORT_ACTION.equals(data.getString(ACTION_TAG))) {
            sort();
        } else if (OPEN_SETTINGS_ACTION.equals(data.getString(ACTION_TAG))) {
            openSettings();
        }
    }

    // --- sorting and settings ----------------------------------------------------------------

    public SortBy getSortBy() {
        return storageWrapper.getSortBy();
    }

    public void setSortBy(SortBy sortBy) {
        if (player.world.isRemote) {
            sendToServer(NBTHelper.withString(SORT_BY_TAG, sortBy.getName()));
        }
        storageWrapper.setSortBy(sortBy);
    }

    /** Sorting happens on the server; the client sees the result through normal slot sync. */
    public void sort() {
        if (player.world.isRemote) {
            sendToServer(NBTHelper.withString(ACTION_TAG, SORT_ACTION));
            return;
        }
        storageWrapper.sort();
    }

    public void openSettings() {
        if (player.world.isRemote) {
            sendToServer(NBTHelper.withString(ACTION_TAG, OPEN_SETTINGS_ACTION));
            return;
        }
        if (player instanceof EntityPlayerMP) {
            ItemStack carried = player.inventory.getItemStack();
            player.inventory.setItemStack(ItemStack.EMPTY);
            openSettingsGui((EntityPlayerMP) player);
            // Nothing can be held in the settings screen: the cursor stack goes back to the player.
            if (!carried.isEmpty() && !player.inventory.addItemStackToInventory(carried)) {
                player.dropItem(carried, false);
            }
        }
    }

    /** Server side: opens the settings screen for this storage. */
    protected abstract void openSettingsGui(EntityPlayerMP playerMP);

    /** The upgrade tab container whose tab is open, or null. */
    @Nullable
    public UpgradeContainerBase<?, ?> getOpenUpgradeContainer() {
        for (UpgradeContainerBase<?, ?> container : upgradeContainers.values()) {
            if (container.isOpen()) {
                return container;
            }
        }
        return null;
    }

    /** The storage's own inventory slots followed by the player's: what recipe transfer may take from. */
    public List<Slot> getStorageAndPlayerSlots() {
        return new ArrayList<>(inventorySlots.subList(0, getFirstUpgradeSlot()));
    }

    /** What a memory slot remembers, for drawing it while the slot is empty, or EMPTY. */
    public ItemStack getMemorizedStack(int slotNumber) {
        if (!isBackpackInventorySlot(slotNumber)) {
            return ItemStack.EMPTY;
        }
        MemorySettingsCategory memory = storageWrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class);
        return memory == null ? ItemStack.EMPTY : memory.getSlotFilterStack(inventorySlots.get(slotNumber).getSlotIndex());
    }

    /** Colours settings categories mark this slot with (no-sort, item display). */
    public List<Integer> getSlotOverlayColors(int slotNumber) {
        List<Integer> colors = new ArrayList<>();
        if (isBackpackInventorySlot(slotNumber)) {
            int index = inventorySlots.get(slotNumber).getSlotIndex();
            for (ISlotColorCategory category : storageWrapper.getSettingsHandler().getCategoriesThatImplement(ISlotColorCategory.class)) {
                Integer color = category.getSlotColor(index);
                if (color != null) {
                    colors.add(color);
                }
            }
        }
        return colors;
    }

    private boolean getMainSetting(MainSettingsCategory.Setting setting) {
        MainSettingsCategory main = storageWrapper.getSettingsHandler().getTypeCategory(MainSettingsCategory.class);
        return main == null || main.getValue(player, setting);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!playerIn.world.isRemote && !getMainSetting(MainSettingsCategory.KEEP_TAB_OPEN) && storageWrapper.getOpenTabId() >= 0) {
            storageWrapper.setOpenTabId(-1);
        }
    }

    private void sendToServer(NBTTagCompound data) {
        CorePacketHandler.CHANNEL.sendToServer(new SyncContainerClientDataMessage(data));
    }

    public void setOpenTabId(int tabId) {
        if (player.world.isRemote) {
            sendToServer(NBTHelper.withInt(OPEN_TAB_ID_TAG, tabId));
        }
        for (Map.Entry<Integer, UpgradeContainerBase<?, ?>> entry : upgradeContainers.entrySet()) {
            entry.getValue().setIsOpen(entry.getKey() == tabId);
        }
        storageWrapper.setOpenTabId(tabId);
    }

    public boolean canDisableUpgrade(int upgradeSlot) {
        IUpgradeWrapper wrapper = storageWrapper.getUpgradeHandler().getSlotWrappers().get(upgradeSlot);
        return wrapper != null && wrapper.canBeDisabled();
    }

    public boolean getUpgradeEnabled(int upgradeSlot) {
        IUpgradeWrapper wrapper = storageWrapper.getUpgradeHandler().getSlotWrappers().get(upgradeSlot);
        return wrapper != null && wrapper.isEnabled();
    }

    public void setUpgradeEnabled(int upgradeSlot, boolean enabled) {
        IUpgradeWrapper wrapper = storageWrapper.getUpgradeHandler().getSlotWrappers().get(upgradeSlot);
        if (wrapper == null || !wrapper.canBeDisabled()) {
            return;
        }
        if (player.world.isRemote) {
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean(UPGRADE_ENABLED_TAG, enabled);
            data.setInteger(UPGRADE_SLOT_TAG, upgradeSlot);
            sendToServer(data);
        }
        wrapper.setEnabled(enabled);
    }

    // --- syncing -----------------------------------------------------------------------------

    @Override
    public void detectAndSendChanges() {
        settingsSync.sendIfChanged(player, windowId, storageWrapper);
        if (reopenPending && player instanceof EntityPlayerMP) {
            reopenPending = false;
            reopen((EntityPlayerMP) player);
            return;
        }
        for (int i = 0; i < inventorySlots.size(); ++i) {
            ItemStack current = inventorySlots.get(i).getStack();
            ItemStack previous = inventoryItemStacks.get(i);
            boolean forceHighCount = resendHighCounts && isBackpackInventorySlot(i) && current.getCount() > HIGH_COUNT_THRESHOLD;
            if (!ItemStack.areItemStacksEqual(previous, current) || forceHighCount) {
                boolean clientStackChanged = forceHighCount || !ItemStack.areItemStacksEqualUsingNBTShareTag(previous, current);
                previous = current.isEmpty() ? ItemStack.EMPTY : current.copy();
                inventoryItemStacks.set(i, previous);
                if (clientStackChanged) {
                    sendSlot(i, previous);
                }
            }
        }
        resendHighCounts = false;
    }

    private void sendSlot(int slotNumber, ItemStack stack) {
        for (IContainerListener listener : listeners) {
            if (isBackpackInventorySlot(slotNumber) && stack.getCount() > HIGH_COUNT_THRESHOLD && listener instanceof EntityPlayerMP) {
                CorePacketHandler.CHANNEL.sendTo(new SyncSlotStackMessage(windowId, slotNumber, stack), (EntityPlayerMP) listener);
            } else if ((slotNumber >= getFirstUpgradeSlot() || isPartSlot(slotNumber)) && listener instanceof EntityPlayerMP) {
                // Upgrade stacks and upgrade tab slots change on the server in ways the client can't
                // predict (a tank filling, a furnace finishing), and so do slots an upgrade runs
                // (taking ingots from a compression slot changes the block and nugget slots too).
                // Vanilla drops slot updates noticed while it handles a click
                // (isChangingQuantityOnly), so these always go out directly.
                ((EntityPlayerMP) listener).connection.sendPacket(new SPacketSetSlot(windowId, slotNumber, stack));
            } else {
                listener.sendSlotContents(this, slotNumber, stack);
            }
        }
    }

    /** A storage slot an upgrade has taken over (compression). */
    private boolean isPartSlot(int slotNumber) {
        return slotNumber < numberOfInventorySlots && storageWrapper.getInventoryHandler().getPartSlots().contains(slotNumber);
    }

    /**
     * Called whenever vanilla sends the whole window (on open and after a click it didn't
     * predict). Those packets cut counts to a byte, so the big stacks go out again right after.
     */
    @Nonnull
    @Override
    public NonNullList<ItemStack> getInventory() {
        resendHighCounts = true;
        return super.getInventory();
    }

    /**
     * Rebuilds the GUI after the set of upgrades changed. The held stack is set aside first,
     * otherwise closing the old container would drop it.
     */
    private void reopen(EntityPlayerMP playerMP) {
        ItemStack carried = playerMP.inventory.getItemStack();
        playerMP.inventory.setItemStack(ItemStack.EMPTY);
        openGuiAgain(playerMP);
        playerMP.inventory.setItemStack(carried);
        playerMP.connection.sendPacket(new SPacketSetSlot(-1, -1, carried));
    }

    // --- slots -------------------------------------------------------------------------------

    /** The player slot holding the open backpack: it can't be taken, swapped, dropped or replaced. */
    private static class LockedSlot extends Slot {
        LockedSlot(InventoryPlayer inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }

    /**
     * Vanilla click handling often grows or shrinks the slot's stack in place and then calls
     * onSlotChanged. A plain SlotItemHandler ignores that, so the change would never be saved.
     * Limits come straight from the handler instead of SlotItemHandler's insert simulation,
     * which would run upgrade hooks (void, compacting) on every hover.
     */
    public class StorageInventorySlot extends SlotItemHandler {
        private final InventoryHandler handler;
        private final int index;

        StorageInventorySlot(InventoryHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
            this.handler = handler;
            this.index = index;
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return !stack.isEmpty() && handler.isItemValid(index, stack);
        }

        @Override
        public int getItemStackLimit(@Nonnull ItemStack stack) {
            return handler.getMaxStackSizeFor(index, stack);
        }

        @Override
        public int getSlotStackLimit() {
            return handler.getSlotLimit(index);
        }

        @Override
        public void putStack(@Nonnull ItemStack stack) {
            handler.setStackInSlot(index, stack);
            onBackpackSlotChangedByPlayer(index);
        }

        /**
         * The stack last handed out. Container code grows or shrinks it and then calls
         * onSlotChanged; for a normal slot it is the stored stack itself, but a slot an upgrade
         * runs (compression) hands out a copy, so the change is written back from it.
         */
        @Nullable
        private ItemStack lastStack = null;
        /** A copy of lastStack as handed out, to tell whether the caller changed it. */
        private ItemStack lastStackAsHandedOut = ItemStack.EMPTY;

        @Nonnull
        @Override
        public ItemStack getStack() {
            lastStack = handler.getStackInSlot(index);
            lastStackAsHandedOut = lastStack.copy();
            if (player.world.isRemote && lastStack.getCount() == Integer.MAX_VALUE) {
                // An infinity upgrade's endless slot: drawn as one item with the screen's "∞" over it.
                ItemStack one = lastStack.copy();
                one.setCount(1);
                return one;
            }
            return lastStack;
        }

        /** Client: a slot an infinity upgrade makes endless (its synced count is Integer.MAX_VALUE). */
        public boolean isInfinite() {
            return handler.getStackInSlot(index).getCount() == Integer.MAX_VALUE;
        }

        /**
         * Only a change made to the handed-out stack is written back: vanilla also calls this
         * after taking items through decrStackSize, when that copy is out of date and writing it
         * would put the taken items back.
         */
        @Override
        public void onSlotChanged() {
            if (lastStack != null && !ItemStack.areItemStacksEqual(lastStack, lastStackAsHandedOut)) {
                handler.setStackInSlot(index, lastStack);
            } else {
                handler.setStackInSlot(index, handler.getStackInSlot(index));
            }
            lastStack = null;
            onBackpackSlotChangedByPlayer(index);
        }
    }

    public class StorageUpgradeSlot extends SlotItemHandler {
        private final UpgradeHandler handler;
        private final int index;

        StorageUpgradeSlot(UpgradeHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
            this.handler = handler;
            this.index = index;
            setBackgroundName(EMPTY_UPGRADE_SLOT_SPRITE);
        }

        public int getUpgradeSlotIndex() {
            return index;
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            if (stack.isEmpty() || !(stack.getItem() instanceof IUpgradeItem)) {
                return false;
            }
            ItemStack current = getStack();
            UpgradeSlotChangeResult result = current.getItem() instanceof IUpgradeItem
                    ? ((IUpgradeItem<?>) current.getItem()).canSwapUpgradeFor(stack, storageWrapper)
                    : ((IUpgradeItem<?>) stack.getItem()).canAddUpgradeTo(storageWrapper, stack, true);
            if (result.isSuccessful()) {
                result = checkInventoryColumnsFree(stack, current);
            }
            reportUpgradeError(result);
            return result.isSuccessful();
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return canRemoveUpgrade(this);
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }

        @Override
        public int getItemStackLimit(@Nonnull ItemStack stack) {
            return 1;
        }

        @Override
        public void putStack(@Nonnull ItemStack stack) {
            handler.setStackInSlot(index, stack);
        }

        @Override
        public void onSlotChanged() {
            handler.setStackInSlot(index, handler.getStackInSlot(index));
        }
    }
}
