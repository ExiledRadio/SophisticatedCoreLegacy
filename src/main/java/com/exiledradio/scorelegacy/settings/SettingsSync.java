package com.exiledradio.scorelegacy.settings;

import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.network.CorePacketHandler;
import com.exiledradio.scorelegacy.network.SyncSettingsMessage;
import com.exiledradio.scorelegacy.settings.main.MainSettingsCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/** Server side, per open container: sends the storage's settings (and the player's defaults) when they change. */
public final class SettingsSync {
    @Nullable
    private NBTTagCompound lastSent = null;

    public void sendIfChanged(EntityPlayer player, int windowId, IStorageWrapper wrapper) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        SettingsHandler settings = wrapper.getSettingsHandler();
        MainSettingsCategory main = settings.getTypeCategory(MainSettingsCategory.class);
        NBTTagCompound playerSettings = main == null ? new NBTTagCompound() : main.getPlayerSettingsTag(player);
        NBTTagCompound current = new NBTTagCompound();
        current.setTag("settings", settings.getNbt());
        current.setTag("player", playerSettings);
        if (current.equals(lastSent)) {
            return;
        }
        lastSent = current.copy();
        CorePacketHandler.CHANNEL.sendTo(new SyncSettingsMessage(windowId, settings.getNbt().copy(), playerSettings.copy()), (EntityPlayerMP) player);
    }
}
