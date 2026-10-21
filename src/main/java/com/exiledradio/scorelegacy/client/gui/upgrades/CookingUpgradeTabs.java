/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.config.AutoCookingUpgradeConfig;
import com.exiledradio.scorelegacy.upgrades.cooking.AutoSmeltingUpgradeItem;
import com.exiledradio.scorelegacy.upgrades.cooking.SmeltingUpgradeItem;
import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeSettingsTab;
import com.exiledradio.scorelegacy.client.gui.filter.FilterLogicControl;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.FilterLogicContainer;
import com.exiledradio.scorelegacy.upgrades.cooking.AutoSmeltingUpgradeContainer;
import com.exiledradio.scorelegacy.upgrades.cooking.SmeltingUpgradeContainer;

/** The smelting tab (a furnace) and the auto-smelting tab (input filter, furnace, fuel filter). */
public final class CookingUpgradeTabs {
    private CookingUpgradeTabs() {}

    public static class SmeltingTab extends UpgradeSettingsTab<SmeltingUpgradeContainer> {
        private final CookingLogicControl cookingLogicControl;

        public SmeltingTab(SmeltingUpgradeContainer container, Position position, StorageScreenBase screen) {
            super(container, position, screen, ((SmeltingUpgradeItem) container.getUpgradeStack().getItem()).getCookingType().getName());
            cookingLogicControl = addHideableChild(new CookingLogicControl(new Position(x + 3, y + 24), container.getCookingLogicContainer()));
        }

        @Override
        protected void moveSlotsToTab() {
            cookingLogicControl.moveSlotsToView(screen.getGuiLeft(), screen.getGuiTop());
        }
    }

    public static class AutoSmeltingTab extends UpgradeSettingsTab<AutoSmeltingUpgradeContainer> {
        private final FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>> inputFilterLogicControl;
        private final FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>> fuelFilterLogicControl;
        private final CookingLogicControl cookingLogicControl;

        public AutoSmeltingTab(AutoSmeltingUpgradeContainer container, Position position, StorageScreenBase screen) {
            super(container, position, screen, "auto_" + ((AutoSmeltingUpgradeItem) container.getUpgradeStack().getItem()).getCookingType().getName());
            AutoCookingUpgradeConfig config = ((AutoSmeltingUpgradeItem) container.getUpgradeStack().getItem()).getConfig();
            inputFilterLogicControl = addHideableChild(new FilterLogicControl.Advanced(screen, new Position(x + 3, y + 24),
                    container.getInputFilterLogicContainer(), config.inputFilterSlotsInRow));
            cookingLogicControl = addHideableChild(new CookingLogicControl(new Position(x + 3, y + 84), container.getCookingLogicContainer()));
            fuelFilterLogicControl = addHideableChild(new FilterLogicControl<>(screen, new Position(x + 3, y + 142),
                    container.getFuelFilterLogicContainer(), config.fuelFilterSlotsInRow));
        }

        @Override
        protected void moveSlotsToTab() {
            inputFilterLogicControl.moveSlotsToView();
            cookingLogicControl.moveSlotsToView(screen.getGuiLeft(), screen.getGuiTop());
            fuelFilterLogicControl.moveSlotsToView();
        }
    }
}
