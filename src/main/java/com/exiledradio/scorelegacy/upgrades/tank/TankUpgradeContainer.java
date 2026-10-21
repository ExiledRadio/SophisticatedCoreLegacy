/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.tank;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.common.gui.UpgradeIOSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class TankUpgradeContainer extends UpgradeContainerBase<TankUpgradeWrapper, TankUpgradeContainer> {
    public static final String EMPTY_TANK_INPUT_SLOT_SPRITE = SophisticatedCoreLegacy.MODID + ":item/empty_tank_input_slot";
    public static final String EMPTY_TANK_OUTPUT_SLOT_SPRITE = SophisticatedCoreLegacy.MODID + ":item/empty_tank_output_slot";

    public TankUpgradeContainer(EntityPlayer player, int containerId, TankUpgradeWrapper wrapper, UpgradeHandler upgradeHandler,
                                UpgradeContainerType<TankUpgradeWrapper, TankUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        slots.add(new UpgradeIOSlot(() -> getUpgradeWrapper().getInventory(), TankUpgradeWrapper.INPUT_SLOT, EMPTY_TANK_INPUT_SLOT_SPRITE,
                "gui.scorelegacy.upgrades.slots.tank_input.tooltip"));
        slots.add(new UpgradeIOSlot(() -> getUpgradeWrapper().getInventory(), TankUpgradeWrapper.OUTPUT_SLOT, EMPTY_TANK_OUTPUT_SLOT_SPRITE,
                "gui.scorelegacy.upgrades.slots.tank_output.tooltip"));
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        //noop
    }

    @Nullable
    public FluidStack getContents() {
        return getUpgradeWrapper().getContents();
    }

    public int getTankCapacity() {
        return getUpgradeWrapper().getTankCapacity();
    }
}
