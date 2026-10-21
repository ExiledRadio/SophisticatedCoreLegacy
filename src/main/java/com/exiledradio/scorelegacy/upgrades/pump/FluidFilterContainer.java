/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.pump;

import com.exiledradio.scorelegacy.common.gui.IServerUpdater;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/** Clicking a fluid filter slot with a filled container sets it to that fluid; empty-handed clears it. */
public class FluidFilterContainer {
    private static final String DATA_FLUID = "setFluid";
    private final EntityPlayer player;
    private final IServerUpdater serverUpdater;
    private final Supplier<FluidFilterLogic> fluidFilterLogic;

    public FluidFilterContainer(EntityPlayer player, IServerUpdater serverUpdater, Supplier<FluidFilterLogic> fluidFilterLogic) {
        this.player = player;
        this.serverUpdater = serverUpdater;
        this.fluidFilterLogic = fluidFilterLogic;
    }

    @Nullable
    public Fluid getFluid(int index) {
        return fluidFilterLogic.get().getFluid(index);
    }

    private void setFluid(int index, @Nullable Fluid fluid) {
        fluidFilterLogic.get().setFluid(index, fluid);
        serverUpdater.sendDataToServer(() -> {
            NBTTagCompound fluidData = new NBTTagCompound();
            fluidData.setInteger("index", index);
            fluidData.setString("fluid", fluid == null ? "" : fluid.getName());
            NBTTagCompound data = new NBTTagCompound();
            data.setTag(DATA_FLUID, fluidData);
            return data;
        });
    }

    public boolean handleMessage(NBTTagCompound data) {
        if (!data.hasKey(DATA_FLUID)) {
            return false;
        }
        NBTTagCompound fluidData = data.getCompoundTag(DATA_FLUID);
        String name = fluidData.getString("fluid");
        setFluid(fluidData.getInteger("index"), name.isEmpty() ? null : FluidRegistry.getFluid(name));
        return true;
    }

    public int getNumberOfFluidFilters() {
        return fluidFilterLogic.get().getNumberOfFluidFilters();
    }

    public void slotClick(int index) {
        ItemStack carried = player.inventory.getItemStack();
        if (carried.isEmpty()) {
            setFluid(index, null);
            return;
        }
        ItemStack copy = carried.copy();
        IFluidHandlerItem itemFluidHandler = copy.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
                ? copy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) : null;
        if (itemFluidHandler != null) {
            FluidStack containedFluid = itemFluidHandler.drain(Fluid.BUCKET_VOLUME, false);
            if (containedFluid != null && containedFluid.amount > 0) {
                setFluid(index, containedFluid.getFluid());
            }
        }
    }
}
