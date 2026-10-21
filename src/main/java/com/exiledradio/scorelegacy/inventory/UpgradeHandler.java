/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.inventory;

import com.exiledradio.scorelegacy.api.IStorageWrapper;

import com.exiledradio.scorelegacy.api.IUpgradeItem;
import com.exiledradio.scorelegacy.api.IUpgradeAccessModifier;
import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.api.IUpgradeWrapperAccessor;
import com.exiledradio.scorelegacy.api.UpgradeType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UpgradeHandler extends ItemStackHandler {
    public static final String UPGRADE_INVENTORY_TAG = "upgradeInventory";

    private final IStorageWrapper owner;
    private final NBTTagCompound contentsNbt;
    private final Runnable markDirty;
    private final Runnable onUpgradesChanged;
    private final Map<Integer, IUpgradeWrapper> slotWrappers = new LinkedHashMap<>();
    private final Map<UpgradeType<?>, List<IUpgradeWrapper>> typeWrappers = new HashMap<>();
    @Nullable
    private IUpgradeWrapperAccessor wrapperAccessor = null;
    @Nullable
    private Runnable refreshCallBack = null;
    private boolean wrappersInitialized = false;
    private boolean typeWrappersInitialized = false;
    private boolean justSavingNbtChange = false;
    private boolean persistent = true;

    public UpgradeHandler(int numberOfUpgradeSlots, IStorageWrapper owner, NBTTagCompound contentsNbt, Runnable markDirty, Runnable onUpgradesChanged) {
        super(numberOfUpgradeSlots);
        this.owner = owner;
        this.contentsNbt = contentsNbt;
        this.markDirty = markDirty;
        this.onUpgradesChanged = onUpgradesChanged;
        deserializeNBT(contentsNbt.getCompoundTag(UPGRADE_INVENTORY_TAG));
    }

    @Override
    public void setSize(int size) {
        // The tier decides the size; a saved "Size" never shrinks the handler.
        if (stacks.size() < size) {
            super.setSize(size);
        }
    }

    public void grow(int newSize) {
        NonNullList<ItemStack> grown = NonNullList.withSize(newSize, ItemStack.EMPTY);
        for (int slot = 0; slot < stacks.size(); slot++) {
            grown.set(slot, stacks.get(slot));
        }
        stacks = grown;
        refreshUpgradeWrappers();
    }

    /** Replaces every slot from saved NBT without saving back, then rebuilds the wrappers. */
    public void reloadFromNbt(NBTTagCompound upgradeInventory) {
        for (int slot = 0; slot < stacks.size(); slot++) {
            stacks.set(slot, ItemStack.EMPTY);
        }
        deserializeNBT(upgradeInventory);
        refreshUpgradeWrappers();
    }

    public IStorageWrapper getOwner() {
        return owner;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.isEmpty() || stack.getItem() instanceof IUpgradeItem;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (persistent) {
            saveInventory();
        }
        if (!justSavingNbtChange) {
            refreshUpgradeWrappers();
        }
    }

    public void saveInventory() {
        contentsNbt.setTag(UPGRADE_INVENTORY_TAG, serializeNBT());
        markDirty.run();
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /** Set by an Inception accessor in the parent backpack, so it hears about changes here. */
    public void setRefreshCallBack(@Nullable Runnable refreshCallBack) {
        this.refreshCallBack = refreshCallBack;
    }

    private void initializeWrappers() {
        if (wrappersInitialized) {
            return;
        }
        wrappersInitialized = true;
        slotWrappers.clear();
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack upgrade = getStackInSlot(slot);
            if (upgrade.isEmpty() || !(upgrade.getItem() instanceof IUpgradeItem<?>)) {
                continue;
            }
            int finalSlot = slot;
            UpgradeType<?> type = ((IUpgradeItem<?>) upgrade.getItem()).getType();
            slotWrappers.put(slot, type.create(owner, upgrade, upgradeStack -> {
                justSavingNbtChange = true;
                setStackInSlot(finalSlot, upgradeStack);
                justSavingNbtChange = false;
            }));
        }
    }

    private void initializeTypeWrappers() {
        if (typeWrappersInitialized) {
            return;
        }
        initializeWrappers();
        typeWrappersInitialized = true;
        typeWrappers.clear();
        for (IUpgradeWrapper wrapper : slotWrappers.values()) {
            if (wrapper.isEnabled()) {
                UpgradeType<?> type = ((IUpgradeItem<?>) wrapper.getUpgradeStack().getItem()).getType();
                typeWrappers.computeIfAbsent(type, t -> new ArrayList<>()).add(wrapper);
            }
        }
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!simulate && persistent && amount > 0) {
            IUpgradeWrapper wrapper = getSlotWrappers().get(slot);
            if (wrapper != null) {
                wrapper.onBeforeRemoved();
            }
        }
        return super.extractItem(slot, amount, simulate);
    }

    /** Inventory columns the installed upgrades take, derived from the upgrades so it can't drift. */
    public int getColumnsTaken() {
        int columnsTaken = 0;
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack upgrade = getStackInSlot(slot);
            if (upgrade.getItem() instanceof IUpgradeItem) {
                columnsTaken += ((IUpgradeItem<?>) upgrade.getItem()).getInventoryColumnsTaken();
            }
        }
        return columnsTaken;
    }

    public Map<Integer, IUpgradeWrapper> getSlotWrappers() {
        initializeWrappers();
        return slotWrappers;
    }

    public <T extends IUpgradeWrapper> List<T> getTypeWrappers(UpgradeType<T> type) {
        initializeTypeWrappers();
        //noinspection unchecked
        return (List<T>) typeWrappers.getOrDefault(type, Collections.emptyList());
    }

    public boolean hasUpgrade(UpgradeType<?> type) {
        return !getTypeWrappers(type).isEmpty();
    }

    /** Enabled wrappers of this type here and, with Inception, in nested backpacks. */
    public <T> List<T> getWrappersThatImplement(Class<T> upgradeClass) {
        initializeWrappers();
        return getWrapperAccessor().getWrappersThatImplement(upgradeClass);
    }

    /** Enabled wrappers of this type in this backpack only. */
    public <T> List<T> getWrappersThatImplementFromMainBackpack(Class<T> upgradeClass) {
        initializeWrappers();
        return getWrapperAccessor().getWrappersThatImplementFromMainBackpack(upgradeClass);
    }

    public <T> List<T> getListOfWrappersThatImplement(Class<T> upgradeClass) {
        initializeWrappers();
        List<T> ret = new ArrayList<>();
        for (IUpgradeWrapper wrapper : slotWrappers.values()) {
            if (wrapper.isEnabled() && upgradeClass.isInstance(wrapper)) {
                ret.add(upgradeClass.cast(wrapper));
            }
        }
        return ret;
    }

    private IUpgradeWrapperAccessor getWrapperAccessor() {
        if (wrapperAccessor == null) {
            IUpgradeWrapperAccessor accessor = new Accessor(this);
            for (IUpgradeAccessModifier upgrade : getListOfWrappersThatImplement(IUpgradeAccessModifier.class)) {
                accessor = upgrade.wrapAccessor(accessor);
            }
            wrapperAccessor = accessor;
        }
        return wrapperAccessor;
    }

    /** An upgrade was switched on or off: same wrappers, different active set. */
    public void refreshWrappersThatImplementAndTypeWrappers() {
        typeWrappersInitialized = false;
        if (wrapperAccessor != null) {
            wrapperAccessor.clearCache();
        }
        if (refreshCallBack != null) {
            refreshCallBack.run();
        }
        onUpgradesChanged.run();
    }

    /** An upgrade was added or removed: wrappers are rebuilt from the stacks. */
    public void refreshUpgradeWrappers() {
        wrappersInitialized = false;
        typeWrappersInitialized = false;
        if (wrapperAccessor != null) {
            wrapperAccessor.onBeforeDeconstruct();
            wrapperAccessor = null;
        }
        if (refreshCallBack != null) {
            refreshCallBack.run();
        }
        onUpgradesChanged.run();
    }

    private static class Accessor implements IUpgradeWrapperAccessor {
        private final Map<Class<?>, List<?>> interfaceWrappers = new HashMap<>();
        private final UpgradeHandler upgradeHandler;

        Accessor(UpgradeHandler upgradeHandler) {
            this.upgradeHandler = upgradeHandler;
        }

        @Override
        public <T> List<T> getWrappersThatImplement(Class<T> upgradeClass) {
            //noinspection unchecked
            return (List<T>) interfaceWrappers.computeIfAbsent(upgradeClass, upgradeHandler::getListOfWrappersThatImplement);
        }

        @Override
        public <T> List<T> getWrappersThatImplementFromMainBackpack(Class<T> upgradeClass) {
            return getWrappersThatImplement(upgradeClass);
        }

        @Override
        public void clearCache() {
            interfaceWrappers.clear();
        }
    }
}
