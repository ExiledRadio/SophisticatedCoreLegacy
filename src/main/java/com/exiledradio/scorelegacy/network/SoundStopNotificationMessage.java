/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.network;

import com.exiledradio.scorelegacy.upgrades.jukebox.ServerStorageSoundHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.UUID;

/** A client reports that a backpack's disc finished (or was stopped there). */
public class SoundStopNotificationMessage implements IMessage {
    private UUID backpackUuid;

    @SuppressWarnings("unused") // used by the network handler
    public SoundStopNotificationMessage() {
    }

    public SoundStopNotificationMessage(UUID backpackUuid) {
        this.backpackUuid = backpackUuid;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        backpackUuid = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(backpackUuid.getMostSignificantBits());
        buf.writeLong(backpackUuid.getLeastSignificantBits());
    }

    public static class Handler implements IMessageHandler<SoundStopNotificationMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SoundStopNotificationMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> ServerStorageSoundHandler.onSoundStopped(player.getServerWorld(), message.backpackUuid));
            return null;
        }
    }
}
