package com.exiledradio.scorelegacy.common.gui;

import com.exiledradio.scorelegacy.api.IStorageWrapper;
import net.minecraft.nbt.NBTTagCompound;

/**
 * A container of a storage that takes GUI changes from the client (SyncContainerClientDataMessage)
 * and gets the storage's settings from the server (SyncSettingsMessage).
 */
public interface ISyncedContainer {
    /** Server side: a change the client GUI made. */
    void handleMessage(NBTTagCompound data);

    IStorageWrapper getStorageWrapper();
}
