/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings;

import com.exiledradio.scorelegacy.common.gui.SettingsContainerBase;
import com.exiledradio.scorelegacy.network.CorePacketHandler;
import com.exiledradio.scorelegacy.network.SyncContainerClientDataMessage;
import net.minecraft.nbt.NBTTagCompound;

/**
 * The container half of one settings tab. On the client, changes are sent to the server, which
 * applies them and sends the new settings back (SyncSettingsMessage); the client doesn't change
 * the settings itself, so both sides can never disagree.
 */
public abstract class SettingsCategoryContainer<C extends ISettingsCategory> {
    private final SettingsContainerBase settingsContainer;
    private final String categoryName;
    private final C category;

    protected SettingsCategoryContainer(SettingsContainerBase settingsContainer, String categoryName, C category) {
        this.settingsContainer = settingsContainer;
        this.categoryName = categoryName;
        this.category = category;
    }

    protected C getCategory() {
        return category;
    }

    public SettingsContainerBase getSettingsContainer() {
        return settingsContainer;
    }

    protected boolean isServer() {
        return !settingsContainer.getPlayer().world.isRemote;
    }

    protected void sendIntToServer(String key, int value) {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger(key, value);
        sendDataToServer(data);
    }

    protected void sendStringToServer(String key, String value) {
        NBTTagCompound data = new NBTTagCompound();
        data.setString(key, value);
        sendDataToServer(data);
    }

    protected void sendDataToServer(NBTTagCompound data) {
        if (isServer()) {
            return;
        }
        data.setString(SettingsContainerBase.CATEGORY_NAME_TAG, categoryName);
        CorePacketHandler.CHANNEL.sendToServer(new SyncContainerClientDataMessage(data));
    }

    public abstract void handleMessage(NBTTagCompound data);
}
