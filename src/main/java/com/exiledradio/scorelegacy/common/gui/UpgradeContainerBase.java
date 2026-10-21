/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.common.gui;

import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.network.CorePacketHandler;
import com.exiledradio.scorelegacy.network.SyncContainerClientDataMessage;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The server-and-client half of one upgrade's settings tab: its slots (filters and so on) and
 * the settings changes it sends to the server. The id is the upgrade's slot index.
 */
public abstract class UpgradeContainerBase<W extends IUpgradeWrapper, C extends UpgradeContainerBase<W, C>> implements IServerUpdater {
    public static final String CONTAINER_ID_TAG = "containerId";

    protected final List<Slot> slots = new ArrayList<>();
    private final int upgradeContainerId;
    private final UpgradeHandler upgradeHandler;
    private final W initialWrapper;
    protected final EntityPlayer player;
    private final UpgradeContainerType<W, C> type;
    private boolean isOpen = false;

    protected UpgradeContainerBase(EntityPlayer player, int upgradeContainerId, W upgradeWrapper, UpgradeHandler upgradeHandler, UpgradeContainerType<W, C> type) {
        this.upgradeContainerId = upgradeContainerId;
        this.initialWrapper = upgradeWrapper;
        this.upgradeHandler = upgradeHandler;
        this.player = player;
        this.type = type;
    }

    /**
     * The upgrade's current wrapper. Wrappers are rebuilt when the upgrade stack changes (on the
     * client, every time the server syncs the slot), so this looks it up rather than keep one.
     */
    @SuppressWarnings("unchecked")
    public W getUpgradeWrapper() {
        IUpgradeWrapper current = upgradeHandler.getSlotWrappers().get(upgradeContainerId);
        return current != null && current.getClass() == initialWrapper.getClass() ? (W) current : initialWrapper;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public UpgradeContainerType<W, C> getType() {
        return type;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public int getUpgradeContainerId() {
        return upgradeContainerId;
    }

    public ItemStack getUpgradeStack() {
        return getUpgradeWrapper().getUpgradeStack();
    }

    @Override
    public void sendBooleanToServer(String key, boolean value) {
        sendDataToServer(() -> NBTHelper.withBoolean(key, value));
    }

    @Override
    public void sendDataToServer(Supplier<NBTTagCompound> supplyData) {
        if (!player.world.isRemote) {
            return;
        }
        NBTTagCompound data = supplyData.get();
        data.setInteger(CONTAINER_ID_TAG, upgradeContainerId);
        CorePacketHandler.CHANNEL.sendToServer(new SyncContainerClientDataMessage(data));
    }

    /** Where a shift-click on one of this tab's slots sends the stack first: the backpack, or the player's inventory. */
    public boolean mergeIntoBackpackFirst(Slot slot) {
        return true;
    }

    public abstract void handleMessage(NBTTagCompound data);
}
