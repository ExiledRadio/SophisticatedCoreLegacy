/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.network;

import com.exiledradio.scorelegacy.common.gui.SettingsContainerBase;
import com.exiledradio.scorelegacy.settings.itemdisplay.ItemDisplaySettingsCategory;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

/** An item dragged from JEI onto a storage slot while the settings screen's memory tab is open. */
public class SetMemorySlotMessage implements IMessage {
    private int slotIndex;
    private ItemStack stack = ItemStack.EMPTY;

    @SuppressWarnings("unused") // needed by FML to create the message before fromBytes
    public SetMemorySlotMessage() {
    }

    public SetMemorySlotMessage(int slotIndex, ItemStack stack) {
        this.slotIndex = slotIndex;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slotIndex = buf.readInt();
        stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slotIndex);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    public static class Handler implements IMessageHandler<SetMemorySlotMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SetMemorySlotMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (!(player.openContainer instanceof SettingsContainerBase) || message.stack.isEmpty()) {
                    return;
                }
                SettingsContainerBase container = (SettingsContainerBase) player.openContainer;
                if (message.slotIndex < 0 || message.slotIndex >= container.getNumberOfSlots()) {
                    return;
                }
                MemorySettingsCategory memory = container.getStorageWrapper().getSettingsHandler().getTypeCategory(MemorySettingsCategory.class);
                if (memory == null) {
                    return;
                }
                memory.setFilter(message.slotIndex, message.stack);
                ItemDisplaySettingsCategory itemDisplay = container.getStorageWrapper().getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class);
                if (itemDisplay != null) {
                    itemDisplay.itemChanged(message.slotIndex);
                }
                container.detectAndSendChanges();
            });
            return null;
        }
    }
}
