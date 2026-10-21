/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.network;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.UUID;

/** Stop a backpack's disc. */
public class StopDiscPlaybackMessage implements IMessage {
    private UUID backpackUuid;

    @SuppressWarnings("unused") // used by the network handler
    public StopDiscPlaybackMessage() {
    }

    public StopDiscPlaybackMessage(UUID backpackUuid) {
        this.backpackUuid = backpackUuid;
    }

    public UUID getBackpackUuid() {
        return backpackUuid;
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

    public static class Handler implements IMessageHandler<StopDiscPlaybackMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(StopDiscPlaybackMessage message, MessageContext ctx) {
            SophisticatedCoreLegacy.proxy.handleStopDisc(message);
            return null;
        }
    }
}
