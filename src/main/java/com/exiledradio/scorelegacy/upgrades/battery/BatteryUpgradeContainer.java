/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.battery;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.common.gui.UpgradeIOSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class BatteryUpgradeContainer extends UpgradeContainerBase<BatteryUpgradeWrapper, BatteryUpgradeContainer> {
    public static final String EMPTY_BATTERY_INPUT_SLOT_SPRITE = SophisticatedCoreLegacy.MODID + ":item/empty_battery_input_slot";
    public static final String EMPTY_BATTERY_OUTPUT_SLOT_SPRITE = SophisticatedCoreLegacy.MODID + ":item/empty_battery_output_slot";

    public BatteryUpgradeContainer(EntityPlayer player, int containerId, BatteryUpgradeWrapper wrapper, UpgradeHandler upgradeHandler,
                                   UpgradeContainerType<BatteryUpgradeWrapper, BatteryUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        slots.add(new UpgradeIOSlot(() -> getUpgradeWrapper().getInventory(), BatteryUpgradeWrapper.INPUT_SLOT, EMPTY_BATTERY_INPUT_SLOT_SPRITE,
                "gui.scorelegacy.upgrades.slots.battery_input.tooltip"));
        slots.add(new UpgradeIOSlot(() -> getUpgradeWrapper().getInventory(), BatteryUpgradeWrapper.OUTPUT_SLOT, EMPTY_BATTERY_OUTPUT_SLOT_SPRITE,
                "gui.scorelegacy.upgrades.slots.battery_output.tooltip"));
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        //noop
    }

    public int getEnergyStored() {
        return getUpgradeWrapper().getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return getUpgradeWrapper().getMaxEnergyStored();
    }
}
