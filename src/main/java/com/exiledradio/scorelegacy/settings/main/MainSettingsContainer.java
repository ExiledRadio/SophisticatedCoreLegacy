/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings.main;

import com.exiledradio.scorelegacy.common.gui.SettingsContainerBase;
import com.exiledradio.scorelegacy.settings.SettingsCategoryContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * The main settings tab. "Context" chooses what a toggle changes: the player's own default
 * (all their storages), or an override for this storage only.
 */
public class MainSettingsContainer extends SettingsCategoryContainer<MainSettingsCategory> {
    private static final String CONTEXT_TAG = "context";
    private static final String SETTING_TAG = "setting";
    private static final String VALUE_TAG = "value";

    public enum Context {
        PLAYER,
        STORAGE
    }

    private Context context = Context.PLAYER;

    public MainSettingsContainer(SettingsContainerBase settingsContainer, String categoryName, MainSettingsCategory category) {
        super(settingsContainer, categoryName, category);
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(CONTEXT_TAG)) {
            context = data.getInteger(CONTEXT_TAG) == Context.STORAGE.ordinal() ? Context.STORAGE : Context.PLAYER;
        } else if (data.hasKey(SETTING_TAG)) {
            for (MainSettingsCategory.Setting setting : MainSettingsCategory.SETTINGS) {
                if (setting.getName().equals(data.getString(SETTING_TAG))) {
                    setValue(setting, data.getBoolean(VALUE_TAG));
                }
            }
        }
    }

    public Context getContext() {
        return context;
    }

    public void toggleContext() {
        context = context == Context.PLAYER ? Context.STORAGE : Context.PLAYER;
        sendIntToServer(CONTEXT_TAG, context.ordinal());
    }

    public boolean getValue(MainSettingsCategory.Setting setting) {
        EntityPlayer player = getSettingsContainer().getPlayer();
        return context == Context.PLAYER ? getCategory().getPlayerValue(player, setting) : getCategory().getValue(player, setting);
    }

    public void toggle(MainSettingsCategory.Setting setting) {
        boolean value = !getValue(setting);
        if (isServer()) {
            setValue(setting, value);
        } else {
            NBTTagCompound data = new NBTTagCompound();
            data.setString(SETTING_TAG, setting.getName());
            data.setBoolean(VALUE_TAG, value);
            sendDataToServer(data);
        }
    }

    private void setValue(MainSettingsCategory.Setting setting, boolean value) {
        EntityPlayer player = getSettingsContainer().getPlayer();
        if (context == Context.PLAYER) {
            getCategory().setPlayerValue(player, setting, value);
        } else {
            getCategory().setStorageValue(player, setting, value);
        }
    }
}
