/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.network;

import com.exiledradio.scorelegacy.common.gui.IFilterSlot;
import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

/** An item dragged from JEI onto a filter slot. Only ever sets filter (ghost) slots: no real items move. */
public class SetGhostSlotMessage implements IMessage {
    private int slotNumber;
    private ItemStack stack = ItemStack.EMPTY;

    @SuppressWarnings("unused") // needed by FML to create the message before fromBytes
    public SetGhostSlotMessage() {
    }

    public SetGhostSlotMessage(int slotNumber, ItemStack stack) {
        this.slotNumber = slotNumber;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slotNumber = buf.readInt();
        stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slotNumber);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    public static class Handler implements IMessageHandler<SetGhostSlotMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SetGhostSlotMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (!(player.openContainer instanceof StorageContainerBase) || message.slotNumber < 0
                        || message.slotNumber >= player.openContainer.inventorySlots.size()) {
                    return;
                }
                Slot slot = player.openContainer.getSlot(message.slotNumber);
                if (slot instanceof IFilterSlot && (message.stack.isEmpty() || slot.isItemValid(message.stack))) {
                    ItemStack ghost = message.stack.copy();
                    ghost.setCount(Math.min(1, ghost.getCount()));
                    slot.putStack(ghost);
                    player.openContainer.detectAndSendChanges();
                }
            });
            return null;
        }
    }
}
