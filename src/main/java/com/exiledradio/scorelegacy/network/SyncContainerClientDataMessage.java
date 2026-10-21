/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.network;

import com.exiledradio.scorelegacy.common.gui.ISyncedContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

/** A settings change made in the open backpack GUI (button toggles, tab switches and so on). */
public class SyncContainerClientDataMessage implements IMessage {
    private NBTTagCompound data = new NBTTagCompound();

    @SuppressWarnings("unused") // needed by FML to create the message before fromBytes
    public SyncContainerClientDataMessage() {
    }

    public SyncContainerClientDataMessage(NBTTagCompound data) {
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound read = ByteBufUtils.readTag(buf);
        data = read == null ? new NBTTagCompound() : read;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<SyncContainerClientDataMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SyncContainerClientDataMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                // Only ever applied to the backpack this player has open right now.
                if (player.openContainer instanceof ISyncedContainer) {
                    ((ISyncedContainer) player.openContainer).handleMessage(message.data);
                }
            });
            return null;
        }
    }
}
