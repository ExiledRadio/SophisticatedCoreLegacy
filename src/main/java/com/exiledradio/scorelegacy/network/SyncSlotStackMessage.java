/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.network;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * One backpack slot's stack with its full count. Vanilla slot packets write the count as a byte,
 * which breaks for stack-upgraded slots holding more than 127.
 */
public class SyncSlotStackMessage implements IMessage {
    private int windowId;
    private int slotNumber;
    private ItemStack stack = ItemStack.EMPTY;

    @SuppressWarnings("unused") // needed by FML to create the message before fromBytes
    public SyncSlotStackMessage() {
    }

    public SyncSlotStackMessage(int windowId, int slotNumber, ItemStack stack) {
        this.windowId = windowId;
        this.slotNumber = slotNumber;
        this.stack = stack;
    }

    public int getWindowId() {
        return windowId;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public ItemStack getStack() {
        return stack;
    }

    /** Applied by the client proxy, so this class never names a client-only type. */
    public static class Handler implements IMessageHandler<SyncSlotStackMessage, IMessage> {
        @Override
        public IMessage onMessage(SyncSlotStackMessage message, MessageContext ctx) {
            SophisticatedCoreLegacy.proxy.handleSyncSlotStack(message);
            return null;
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        slotNumber = buf.readInt();
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        if (tag != null) {
            stack = new ItemStack(tag);
            stack.setCount(buf.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeInt(slotNumber);
        ByteBufUtils.writeTag(buf, stack.writeToNBT(new NBTTagCompound()));
        buf.writeInt(stack.getCount());
    }
}
