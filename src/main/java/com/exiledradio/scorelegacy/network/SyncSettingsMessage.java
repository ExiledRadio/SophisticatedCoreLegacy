package com.exiledradio.scorelegacy.network;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

/**
 * The open storage's settings, sent whenever they change on the server, plus the player's own
 * defaults for the main settings (the client never gets player NBT otherwise).
 */
public class SyncSettingsMessage implements IMessage {
    private int windowId;
    private NBTTagCompound settings = new NBTTagCompound();
    private NBTTagCompound playerSettings = new NBTTagCompound();

    @SuppressWarnings("unused") // needed by FML to create the message before fromBytes
    public SyncSettingsMessage() {
    }

    public SyncSettingsMessage(int windowId, NBTTagCompound settings, NBTTagCompound playerSettings) {
        this.windowId = windowId;
        this.settings = settings;
        this.playerSettings = playerSettings;
    }

    public int getWindowId() {
        return windowId;
    }

    public NBTTagCompound getSettings() {
        return settings;
    }

    public NBTTagCompound getPlayerSettings() {
        return playerSettings;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        NBTTagCompound read = ByteBufUtils.readTag(buf);
        settings = read == null ? new NBTTagCompound() : read;
        read = ByteBufUtils.readTag(buf);
        playerSettings = read == null ? new NBTTagCompound() : read;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        ByteBufUtils.writeTag(buf, settings);
        ByteBufUtils.writeTag(buf, playerSettings);
    }

    public static class Handler implements IMessageHandler<SyncSettingsMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SyncSettingsMessage message, MessageContext ctx) {
            SophisticatedCoreLegacy.proxy.handleSyncSettings(message);
            return null;
        }
    }
}
