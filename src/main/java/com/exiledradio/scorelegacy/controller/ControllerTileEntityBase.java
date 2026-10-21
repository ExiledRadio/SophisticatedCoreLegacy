/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.controller;

import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import com.exiledradio.scorelegacy.util.ItemStackKey;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A storage controller (the original's ControllerBlockEntityBase): the storages touching it, or
 * touching those, within 15 blocks, plus blocks linked with the storage tool, seen as one item
 * handler. Inserts go where the original sends them: storages already holding that exact stack,
 * then ones holding the item with room, then ones memorising it, then any with an empty slot.
 *
 * <p>The original keeps that routing index up to date with listeners on every storage's
 * inventory and settings. This port rebuilds a storage's entry when it is first asked for in a
 * tick, or after the controller itself changed that storage, which needs no listeners; routing
 * may use a view up to one tick old, while the storages themselves decide what fits. Inserts
 * that found no room are remembered for the rest of the tick (the original's
 * CachedFailedInsertInventoryHandler), since a hopper tries every slot in turn.
 */
public abstract class ControllerTileEntityBase extends TileEntity implements IItemHandlerModifiable {
    public static final int SEARCH_RANGE = 15;
    private static final String STORAGE_POSITIONS_TAG = "storagePositions";
    private static final String LINKED_BLOCKS_TAG = "linkedBlocks";

    private List<BlockPos> storagePositions = new ArrayList<>();
    private Set<BlockPos> linkedBlocks = new LinkedHashSet<>();

    /** Slot layout: storage i's slots end before baseIndexes[i]. Rebuilt once a tick. */
    private int[] baseIndexes = new int[0];
    private int totalSlots = 0;
    private long slotLayoutTime = -1;

    private final Map<BlockPos, StorageSummary> summaries = new HashMap<>();
    private long summaryTime = -1;
    private final Set<ItemStackKey> failedInserts = new HashSet<>();
    private long failedInsertsTime = -1;

    // ---- Network ---------------------------------------------------------------------------

    public List<BlockPos> getStoragePositions() {
        return Collections.unmodifiableList(storagePositions);
    }

    public Set<BlockPos> getLinkedBlocks() {
        return Collections.unmodifiableSet(linkedBlocks);
    }

    public boolean isStorageConnected(BlockPos storagePos) {
        return storagePositions.contains(storagePos);
    }

    /** Placed: takes in every storage around it. */
    public void searchAndAddStorages() {
        Set<BlockPos> positionsToCheck = new LinkedHashSet<>();
        for (EnumFacing direction : EnumFacing.values()) {
            positionsToCheck.add(pos.offset(direction));
        }
        searchAndAddStorages(positionsToCheck, false);
    }

    /** A storage was placed next to the network (or is being re-added). */
    public void addStorage(BlockPos storagePos) {
        if (storagePositions.contains(storagePos)) {
            storagePositions.remove(storagePos);
        }
        if (isWithinRange(storagePos)) {
            Set<BlockPos> positionsToCheck = new LinkedHashSet<>();
            positionsToCheck.add(storagePos);
            searchAndAddStorages(positionsToCheck, false);
        }
    }

    public boolean addLinkedBlock(BlockPos linkedPos) {
        if (world == null || world.isRemote || !isWithinRange(linkedPos) || linkedBlocks.contains(linkedPos) || storagePositions.contains(linkedPos)) {
            return false;
        }
        linkedBlocks.add(linkedPos);
        TileEntity te = world.getTileEntity(linkedPos);
        if (te instanceof ILinkable) {
            ILinkable linkable = (ILinkable) te;
            if (linkable.connectLinkedSelf()) {
                Set<BlockPos> self = new LinkedHashSet<>();
                self.add(linkedPos);
                searchAndAddStorages(self, true);
            }
            searchAndAddStorages(new LinkedHashSet<>(linkable.getConnectablePositions()), false);
        }
        onNetworkChanged();
        return true;
    }

    public void removeLinkedBlock(BlockPos linkedPos) {
        linkedBlocks.remove(linkedPos);
        verifyStoragesConnected();
        onNetworkChanged();
    }

    public void removeStorage(BlockPos storagePos) {
        removeStorageAndUnregister(storagePos);
        verifyStoragesConnected();
        onNetworkChanged();
    }

    private void searchAndAddStorages(Set<BlockPos> positionsToCheck, boolean addingLinkedSelf) {
        Set<BlockPos> positionsChecked = new HashSet<>();
        boolean first = true;
        while (!positionsToCheck.isEmpty()) {
            Iterator<BlockPos> it = positionsToCheck.iterator();
            BlockPos posToCheck = it.next();
            it.remove();
            positionsChecked.add(posToCheck);
            TileEntity te = world.isBlockLoaded(posToCheck) ? world.getTileEntity(posToCheck) : null;
            if (te instanceof IControllableStorage) {
                IControllableStorage storage = (IControllableStorage) te;
                if (storage.canBeConnected() || (addingLinkedSelf && first)) {
                    if (storage instanceof ILinkable && ((ILinkable) storage).isLinked() && (!addingLinkedSelf || !first)) {
                        // A linked storage reached through neighbours joins as a neighbour instead.
                        linkedBlocks.remove(posToCheck);
                        ((ILinkable) storage).setNotLinked();
                    } else if (!storagePositions.contains(posToCheck)) {
                        storagePositions.add(posToCheck);
                        storage.registerController(this);
                    }
                    if (storage.canConnectStorages()) {
                        addUncheckedPositionsAround(positionsToCheck, positionsChecked, posToCheck);
                    }
                }
            }
            first = false;
        }
        onNetworkChanged();
    }

    private void addUncheckedPositionsAround(Set<BlockPos> positionsToCheck, Set<BlockPos> positionsChecked, BlockPos currentPos) {
        for (EnumFacing direction : EnumFacing.values()) {
            BlockPos next = currentPos.offset(direction);
            if (!positionsChecked.contains(next) && (!storagePositions.contains(next) || linkedBlocks.contains(next)) && isWithinRange(next)) {
                positionsToCheck.add(next);
            }
        }
    }

    private boolean isWithinRange(BlockPos other) {
        return Math.abs(other.getX() - pos.getX()) <= SEARCH_RANGE && Math.abs(other.getY() - pos.getY()) <= SEARCH_RANGE
                && Math.abs(other.getZ() - pos.getZ()) <= SEARCH_RANGE;
    }

    private void removeStorageAndUnregister(BlockPos storagePos) {
        if (!storagePositions.remove(storagePos)) {
            return;
        }
        linkedBlocks.remove(storagePos);
        summaries.remove(storagePos);
        TileEntity te = world.isBlockLoaded(storagePos) ? world.getTileEntity(storagePos) : null;
        if (te instanceof IControllableStorage) {
            ((IControllableStorage) te).unregisterController();
        }
    }

    /** Drops storages no longer reachable from the controller or a linked block. */
    private void verifyStoragesConnected() {
        Set<BlockPos> toVerify = new HashSet<>(storagePositions);
        Set<BlockPos> positionsToCheck = new HashSet<>();
        for (EnumFacing direction : EnumFacing.values()) {
            BlockPos next = pos.offset(direction);
            if (toVerify.contains(next)) {
                positionsToCheck.add(next);
            }
        }
        Set<BlockPos> positionsChecked = new HashSet<>();
        verifyDirectlyConnected(toVerify, positionsToCheck, positionsChecked);
        for (BlockPos linkedPos : linkedBlocks) {
            TileEntity te = world.isBlockLoaded(linkedPos) ? world.getTileEntity(linkedPos) : null;
            if (te instanceof ILinkable) {
                ILinkable linkable = (ILinkable) te;
                if (linkable.connectLinkedSelf() && toVerify.contains(linkedPos)) {
                    positionsToCheck.add(linkedPos);
                }
                for (BlockPos connectable : linkable.getConnectablePositions()) {
                    if (toVerify.contains(connectable)) {
                        positionsToCheck.add(connectable);
                    }
                }
            }
        }
        verifyDirectlyConnected(toVerify, positionsToCheck, positionsChecked);
        for (BlockPos unreachable : toVerify) {
            removeStorageAndUnregister(unreachable);
        }
    }

    private void verifyDirectlyConnected(Set<BlockPos> toVerify, Set<BlockPos> positionsToCheck, Set<BlockPos> positionsChecked) {
        while (!positionsToCheck.isEmpty()) {
            Iterator<BlockPos> it = positionsToCheck.iterator();
            BlockPos posToCheck = it.next();
            it.remove();
            positionsChecked.add(posToCheck);
            TileEntity te = world.isBlockLoaded(posToCheck) ? world.getTileEntity(posToCheck) : null;
            if (!(te instanceof IControllableStorage)) {
                continue;
            }
            toVerify.remove(posToCheck);
            if (((IControllableStorage) te).canConnectStorages()) {
                for (EnumFacing direction : EnumFacing.values()) {
                    BlockPos next = posToCheck.offset(direction);
                    if (!positionsChecked.contains(next) && toVerify.contains(next)) {
                        positionsToCheck.add(next);
                    }
                }
            }
        }
    }

    /** The controller is going away: every storage and link forgets it. */
    public void detachFromStoragesAndUnlinkBlocks() {
        for (BlockPos storagePos : new ArrayList<>(storagePositions)) {
            TileEntity te = world.isBlockLoaded(storagePos) ? world.getTileEntity(storagePos) : null;
            if (te instanceof IControllableStorage) {
                ((IControllableStorage) te).unregisterController();
            }
        }
        for (BlockPos linkedPos : new ArrayList<>(linkedBlocks)) {
            TileEntity te = world.isBlockLoaded(linkedPos) ? world.getTileEntity(linkedPos) : null;
            if (te instanceof ILinkable) {
                ((ILinkable) te).unlinkFromController();
            }
        }
    }

    private void onNetworkChanged() {
        slotLayoutTime = -1;
        summaryTime = -1;
        failedInsertsTime = -1;
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    // ---- Storages --------------------------------------------------------------------------

    @Nullable
    protected IControllableStorage getStorage(BlockPos storagePos) {
        if (world == null || !world.isBlockLoaded(storagePos)) {
            return null;
        }
        TileEntity te = world.getTileEntity(storagePos);
        return te instanceof IControllableStorage && ((IControllableStorage) te).hasStorageData() ? (IControllableStorage) te : null;
    }

    @Nullable
    private IItemHandlerSimpleInserter getHandler(BlockPos storagePos) {
        IControllableStorage storage = getStorage(storagePos);
        return storage == null ? null : storage.getStorageWrapper().getInventoryForInputOutput();
    }

    private long getTime() {
        return world == null ? 0 : world.getTotalWorldTime();
    }

    /** What a storage holds, for routing: its distinct stacks, items, and whether a slot is free. */
    private static final class StorageSummary {
        private final Set<ItemStackKey> stacks = new HashSet<>();
        private final Set<Item> items = new HashSet<>();
        private boolean hasEmptySlots;
    }

    @Nullable
    private StorageSummary getSummary(BlockPos storagePos) {
        long time = getTime();
        if (summaryTime != time) {
            summaries.clear();
            summaryTime = time;
        }
        StorageSummary summary = summaries.get(storagePos);
        if (summary == null) {
            IItemHandlerSimpleInserter handler = getHandler(storagePos);
            if (handler == null) {
                return null;
            }
            summary = new StorageSummary();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    summary.hasEmptySlots = true;
                } else {
                    summary.stacks.add(new ItemStackKey(stack));
                    summary.items.add(stack.getItem());
                }
            }
            summaries.put(storagePos, summary);
        }
        return summary;
    }

    private boolean isMemorizedIn(BlockPos storagePos, ItemStack stack) {
        IControllableStorage storage = getStorage(storagePos);
        if (storage == null) {
            return false;
        }
        MemorySettingsCategory memory = storage.getStorageWrapper().getSettingsHandler().getTypeCategory(MemorySettingsCategory.class);
        return memory != null && memory.isMemorized(stack);
    }

    /** Whether any storage holds this item (any NBT). */
    protected boolean hasItem(Item item) {
        for (BlockPos storagePos : storagePositions) {
            StorageSummary summary = getSummary(storagePos);
            if (summary != null && summary.items.contains(item)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isMemorizedItem(ItemStack stack) {
        for (BlockPos storagePos : storagePositions) {
            if (isMemorizedIn(storagePos, stack)) {
                return true;
            }
        }
        return false;
    }

    // ---- Inserting -------------------------------------------------------------------------

    /**
     * The original's routing order.
     *
     * @param insertIntoAnyEmpty also into storages that have nothing to do with the item (pipes:
     *                           yes; depositing by hand at the controller: no)
     */
    public ItemStack insertItem(ItemStack stack, boolean simulate, boolean insertIntoAnyEmpty) {
        if (stack.isEmpty() || world == null || world.isRemote) {
            return stack;
        }
        ItemStackKey key = new ItemStackKey(stack);
        ItemStack remaining = stack;
        for (BlockPos storagePos : new ArrayList<>(storagePositions)) {
            StorageSummary summary = getSummary(storagePos);
            if (summary != null && summary.stacks.contains(key)) {
                remaining = insertIntoStorage(storagePos, remaining, simulate);
                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        for (BlockPos storagePos : new ArrayList<>(storagePositions)) {
            StorageSummary summary = getSummary(storagePos);
            if (summary != null && summary.hasEmptySlots && summary.items.contains(remaining.getItem())) {
                remaining = insertIntoStorage(storagePos, remaining, simulate);
                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        for (BlockPos storagePos : new ArrayList<>(storagePositions)) {
            if (isMemorizedIn(storagePos, remaining)) {
                remaining = insertIntoStorage(storagePos, remaining, simulate);
                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (insertIntoAnyEmpty) {
            for (BlockPos storagePos : new ArrayList<>(storagePositions)) {
                StorageSummary summary = getSummary(storagePos);
                if (summary != null && summary.hasEmptySlots) {
                    remaining = insertIntoStorage(storagePos, remaining, simulate);
                    if (remaining.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return remaining;
    }

    private ItemStack insertIntoStorage(BlockPos storagePos, ItemStack stack, boolean simulate) {
        IItemHandlerSimpleInserter handler = getHandler(storagePos);
        if (handler == null) {
            return stack;
        }
        ItemStack remaining = handler.insertItem(stack, simulate);
        if (!simulate && remaining.getCount() != stack.getCount()) {
            summaries.remove(storagePos);
        }
        return remaining;
    }

    // ---- IItemHandler ----------------------------------------------------------------------

    private void updateSlotLayout() {
        long time = getTime();
        if (slotLayoutTime == time) {
            return;
        }
        slotLayoutTime = time;
        baseIndexes = new int[storagePositions.size()];
        totalSlots = 0;
        for (int i = 0; i < storagePositions.size(); i++) {
            IItemHandlerSimpleInserter handler = getHandler(storagePositions.get(i));
            totalSlots += handler == null ? 0 : handler.getSlots();
            baseIndexes[i] = totalSlots;
        }
    }

    @Override
    public int getSlots() {
        updateSlotLayout();
        return totalSlots;
    }

    /** Slots of the storage at this index in connection order (the original's getSlots(int)). */
    public int getSlots(int storageIndex) {
        updateSlotLayout();
        if (storageIndex < 0 || storageIndex >= baseIndexes.length) {
            return 0;
        }
        return baseIndexes[storageIndex] - (storageIndex == 0 ? 0 : baseIndexes[storageIndex - 1]);
    }

    private int getIndexForSlot(int slot) {
        updateSlotLayout();
        if (slot < 0) {
            return -1;
        }
        for (int i = 0; i < baseIndexes.length; i++) {
            if (slot < baseIndexes[i]) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    private IItemHandlerSimpleInserter getHandlerForSlot(int slot) {
        int index = getIndexForSlot(slot);
        return index < 0 || index >= storagePositions.size() ? null : getHandler(storagePositions.get(index));
    }

    private int getLocalSlot(int slot) {
        int index = getIndexForSlot(slot);
        return index <= 0 ? slot : slot - baseIndexes[index - 1];
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        IItemHandlerSimpleInserter handler = getHandlerForSlot(slot);
        int local = getLocalSlot(slot);
        return handler == null || local >= handler.getSlots() ? ItemStack.EMPTY : handler.getStackInSlot(local);
    }

    /** Pipes and hoppers name a slot; like the original the controller routes the stack itself. */
    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot < 0 || slot >= getSlots() || stack.isEmpty()) {
            return stack;
        }
        long time = getTime();
        if (failedInsertsTime != time) {
            failedInserts.clear();
            failedInsertsTime = time;
        }
        ItemStackKey key = new ItemStackKey(stack);
        if (failedInserts.contains(key)) {
            return stack;
        }
        ItemStack remaining = insertItem(stack, simulate, true);
        if (remaining.getCount() == stack.getCount()) {
            failedInserts.add(key);
        }
        return remaining;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        IItemHandlerSimpleInserter handler = getHandlerForSlot(slot);
        int local = getLocalSlot(slot);
        if (handler == null || local >= handler.getSlots()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = handler.extractItem(local, amount, simulate);
        if (!simulate && !extracted.isEmpty()) {
            summaries.remove(storagePositions.get(getIndexForSlot(slot)));
            failedInsertsTime = -1;
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        IItemHandlerSimpleInserter handler = getHandlerForSlot(slot);
        int local = getLocalSlot(slot);
        return handler == null || local >= handler.getSlots() ? 0 : handler.getSlotLimit(local);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        IItemHandlerSimpleInserter handler = getHandlerForSlot(slot);
        int local = getLocalSlot(slot);
        return handler != null && local < handler.getSlots() && handler.isItemValid(local, stack);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        IItemHandlerSimpleInserter handler = getHandlerForSlot(slot);
        int local = getLocalSlot(slot);
        if (handler != null && local < handler.getSlots()) {
            handler.setStackInSlot(local, stack);
            summaries.remove(storagePositions.get(getIndexForSlot(slot)));
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, facing);
    }

    // ---- Saving and syncing ----------------------------------------------------------------

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        return writePositions(compound);
    }

    private NBTTagCompound writePositions(NBTTagCompound compound) {
        compound.setTag(STORAGE_POSITIONS_TAG, toList(storagePositions));
        compound.setTag(LINKED_BLOCKS_TAG, toList(linkedBlocks));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readPositions(compound);
    }

    private void readPositions(NBTTagCompound compound) {
        storagePositions = new ArrayList<>(fromList(compound.getTagList(STORAGE_POSITIONS_TAG, Constants.NBT.TAG_LONG)));
        linkedBlocks = new LinkedHashSet<>(fromList(compound.getTagList(LINKED_BLOCKS_TAG, Constants.NBT.TAG_LONG)));
        slotLayoutTime = -1;
        summaryTime = -1;
    }

    private static NBTTagList toList(Iterable<BlockPos> positions) {
        NBTTagList list = new NBTTagList();
        for (BlockPos position : positions) {
            list.appendTag(new NBTTagLong(position.toLong()));
        }
        return list;
    }

    private static List<BlockPos> fromList(NBTTagList list) {
        List<BlockPos> positions = new ArrayList<>();
        for (int i = 0; i < list.tagCount(); i++) {
            positions.add(BlockPos.fromLong(((NBTTagLong) list.get(i)).getLong()));
        }
        return positions;
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        return writePositions(super.getUpdateTag());
    }

    @Override
    public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        readPositions(tag);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, writePositions(new NBTTagCompound()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readPositions(packet.getNbtCompound());
    }
}
