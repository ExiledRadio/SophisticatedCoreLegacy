/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.common.gui;

import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Supplier;

public interface IServerUpdater {
    void sendBooleanToServer(String key, boolean value);

    void sendDataToServer(Supplier<NBTTagCompound> supplyData);
}
