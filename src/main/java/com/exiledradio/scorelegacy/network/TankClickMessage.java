/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.network;

import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.tank.TankUpgradeWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** The player clicked a tank in the backpack GUI holding a fluid container: fill it, or empty it in. */
public class TankClickMessage implements IMessage {
    private int upgradeSlot;

    @SuppressWarnings("unused") // used by the network handler
    public TankClickMessage() {
    }

    public TankClickMessage(int upgradeSlot) {
        this.upgradeSlot = upgradeSlot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        upgradeSlot = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(upgradeSlot);
    }

    public static class Handler implements IMessageHandler<TankClickMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(TankClickMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> handle(player, message.upgradeSlot));
            return null;
        }

        private static void handle(EntityPlayerMP player, int upgradeSlot) {
            if (!(player.openContainer instanceof StorageContainerBase)) {
                return;
            }
            UpgradeContainerBase<?, ?> upgradeContainer = ((StorageContainerBase) player.openContainer).getUpgradeContainers().get(upgradeSlot);
            ItemStack cursorStack = player.inventory.getItemStack();
            if (!(upgradeContainer instanceof TankUpgradeContainer) || cursorStack.getCount() != 1
                    || !cursorStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                return;
            }
            IFluidHandlerItem fluidHandler = cursorStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandler == null) {
                return;
            }
            TankUpgradeWrapper tank = ((TankUpgradeContainer) upgradeContainer).getUpgradeWrapper();
            Consumer<ItemStack> setCursor = stack -> {
                player.inventory.setItemStack(stack);
                player.connection.sendPacket(new SPacketSetSlot(-1, -1, stack));
            };
            if (tank.getContents() == null || !tank.fillHandler(fluidHandler, setCursor)) {
                tank.drainHandler(fluidHandler, setCursor);
            }
        }
    }
}
