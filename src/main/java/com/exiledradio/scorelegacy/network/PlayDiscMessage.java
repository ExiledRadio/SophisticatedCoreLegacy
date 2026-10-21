/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.network;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.UUID;

/** Start a backpack's disc, either at a block or following an entity. */
public class PlayDiscMessage implements IMessage {
    private boolean blockBackpack;
    private UUID backpackUuid;
    private int discItemId;
    private int entityId;
    private BlockPos pos = BlockPos.ORIGIN;

    @SuppressWarnings("unused") // used by the network handler
    public PlayDiscMessage() {
    }

    public PlayDiscMessage(UUID backpackUuid, int discItemId, BlockPos pos) {
        blockBackpack = true;
        this.backpackUuid = backpackUuid;
        this.discItemId = discItemId;
        this.pos = pos;
    }

    public PlayDiscMessage(UUID backpackUuid, int discItemId, int entityId) {
        blockBackpack = false;
        this.backpackUuid = backpackUuid;
        this.discItemId = discItemId;
        this.entityId = entityId;
    }

    public boolean isBlockBackpack() {
        return blockBackpack;
    }

    public UUID getBackpackUuid() {
        return backpackUuid;
    }

    public int getDiscItemId() {
        return discItemId;
    }

    public int getEntityId() {
        return entityId;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        blockBackpack = buf.readBoolean();
        backpackUuid = new UUID(buf.readLong(), buf.readLong());
        discItemId = buf.readInt();
        if (blockBackpack) {
            pos = BlockPos.fromLong(buf.readLong());
        } else {
            entityId = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(blockBackpack);
        buf.writeLong(backpackUuid.getMostSignificantBits());
        buf.writeLong(backpackUuid.getLeastSignificantBits());
        buf.writeInt(discItemId);
        if (blockBackpack) {
            buf.writeLong(pos.toLong());
        } else {
            buf.writeInt(entityId);
        }
    }

    public static class Handler implements IMessageHandler<PlayDiscMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(PlayDiscMessage message, MessageContext ctx) {
            SophisticatedCoreLegacy.proxy.handlePlayDisc(message);
            return null;
        }
    }
}
