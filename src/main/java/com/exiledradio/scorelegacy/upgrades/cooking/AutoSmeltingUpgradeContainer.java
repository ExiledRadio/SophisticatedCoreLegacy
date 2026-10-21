/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.cooking;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.FilterLogicContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class AutoSmeltingUpgradeContainer extends UpgradeContainerBase<AutoSmeltingUpgradeWrapper, AutoSmeltingUpgradeContainer> {
    private final FilterLogicContainer<FilterLogic> inputFilterLogicContainer;
    private final FilterLogicContainer<FilterLogic> fuelFilterLogicContainer;
    private final CookingLogicContainer cookingLogicContainer;

    public AutoSmeltingUpgradeContainer(EntityPlayer player, int containerId, AutoSmeltingUpgradeWrapper wrapper, UpgradeHandler upgradeHandler,
                                        UpgradeContainerType<AutoSmeltingUpgradeWrapper, AutoSmeltingUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        inputFilterLogicContainer = new FilterLogicContainer<>(() -> getUpgradeWrapper().getInputFilterLogic(), this, slots::add);
        fuelFilterLogicContainer = new FilterLogicContainer<>(() -> getUpgradeWrapper().getFuelFilterLogic(), this, slots::add);
        cookingLogicContainer = new CookingLogicContainer(() -> getUpgradeWrapper().getCookingLogic(), slots::add);
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        // Only the input filter has buttons; the fuel filter is slots only.
        inputFilterLogicContainer.handleMessage(data);
    }

    public CookingLogicContainer getCookingLogicContainer() {
        return cookingLogicContainer;
    }

    public FilterLogicContainer<FilterLogic> getInputFilterLogicContainer() {
        return inputFilterLogicContainer;
    }

    public FilterLogicContainer<FilterLogic> getFuelFilterLogicContainer() {
        return fuelFilterLogicContainer;
    }
}
