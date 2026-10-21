/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeSettingsTab;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.upgrades.pump.PumpUpgradeContainer;

import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.createToggleButtonDefinition;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.getBooleanStateData;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.icon;

/** Input/output for both pumps; the advanced one adds world and hand toggles and the fluid filter. */
public class PumpUpgradeTab extends UpgradeSettingsTab<PumpUpgradeContainer> {
    private static final ButtonDefinition.Toggle<Boolean> IS_INPUT = createToggleButtonDefinition(
            getBooleanStateData(icon(144, 0, "pump_input"), icon(160, 0, "pump_output")));
    private static final ButtonDefinition.Toggle<Boolean> INTERACT_WITH_WORLD = createToggleButtonDefinition(
            getBooleanStateData(icon(176, 0, "interact_with_world"), icon(192, 0, "do_not_interact_with_world")));
    private static final ButtonDefinition.Toggle<Boolean> INTERACT_WITH_HAND = createToggleButtonDefinition(
            getBooleanStateData(icon(208, 0, "interact_with_hand"), icon(224, 0, "do_not_interact_with_hand")));

    public PumpUpgradeTab(PumpUpgradeContainer container, Position position, StorageScreenBase screen, boolean advanced) {
        super(container, position, screen, advanced ? "advanced_pump" : "pump");
        addHideableChild(new ToggleButton<>(new Position(x + 3, y + 24), IS_INPUT,
                button -> container.setIsInput(!container.isInput()), container::isInput));
        if (advanced) {
            addHideableChild(new ToggleButton<>(new Position(x + 21, y + 24), INTERACT_WITH_WORLD,
                    button -> container.setInteractWithWorld(!container.shouldInteractWithWorld()), container::shouldInteractWithWorld));
            addHideableChild(new ToggleButton<>(new Position(x + 39, y + 24), INTERACT_WITH_HAND,
                    button -> container.setInteractWithHand(!container.shouldInteractWithHand()), container::shouldInteractWithHand));
            addHideableChild(new FluidFilterControl(new Position(x + 3, y + 44), container.getFluidFilterContainer()));
        }
    }

    @Override
    protected void moveSlotsToTab() {
        //noop
    }
}
