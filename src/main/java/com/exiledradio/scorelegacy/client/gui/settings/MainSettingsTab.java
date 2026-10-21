/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.settings;

import com.exiledradio.scorelegacy.client.gui.controls.ButtonBase;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.settings.main.MainSettingsCategory;
import com.exiledradio.scorelegacy.settings.main.MainSettingsContainer;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;

import java.util.function.IntConsumer;

/**
 * Shift-click into open tab first, keep tab open, and the context button choosing whether a
 * toggle changes the player's default or only this storage.
 */
public class MainSettingsTab extends SettingsTab<MainSettingsContainer> {
    private static final ButtonDefinition.Toggle<Boolean> SHIFT_CLICK_INTO_OPEN_TAB = ButtonDefinitions.createToggleButtonDefinition(ImmutableMap.of(
            true, state(new UV(80, 32), "shift_click_open_tab.on"),
            false, state(new UV(64, 96), "shift_click_open_tab.off")));
    private static final ButtonDefinition.Toggle<Boolean> KEEP_TAB_OPEN = ButtonDefinitions.createToggleButtonDefinition(ImmutableMap.of(
            true, state(new UV(80, 80), "keep_tab_open.on"),
            false, state(new UV(80, 96), "keep_tab_open.off")));

    private static ToggleButton.StateData state(UV uv, String key) {
        return GuiHelper.getButtonStateData(uv, Dimension.SQUARE_16, new Position(1, 1),
                TranslationHelper.tooltip(TranslationHelper.translSettingsButton(key), TranslationHelper.translSettingsButton(key + ".tooltip")));
    }

    public MainSettingsTab(MainSettingsContainer container, Position position, GuiContainer screen) {
        super(container, position, screen, container.getSettingsContainer().getStorageWrapper().getMainSettingsTabKey(),
                onTabIconClicked -> getTabButton(container, position, onTabIconClicked));
        addHideableChild(new ContextButton(new Position(x + 3, y + 24), button -> container.toggleContext(), container));
        addHideableChild(new ToggleButton<>(new Position(x + 3, y + 46), SHIFT_CLICK_INTO_OPEN_TAB,
                button -> container.toggle(MainSettingsCategory.SHIFT_CLICK_INTO_OPEN_TAB_FIRST),
                () -> container.getValue(MainSettingsCategory.SHIFT_CLICK_INTO_OPEN_TAB_FIRST)));
        addHideableChild(new ToggleButton<>(new Position(x + 21, y + 46), KEEP_TAB_OPEN,
                button -> container.toggle(MainSettingsCategory.KEEP_TAB_OPEN),
                () -> container.getValue(MainSettingsCategory.KEEP_TAB_OPEN)));
    }

    private static ButtonBase getTabButton(MainSettingsContainer container, Position position, IntConsumer onTabIconClicked) {
        Position buttonPosition = new Position(position.getX() + 1, position.getY() + 4);
        net.minecraft.item.ItemStack icon = container.getSettingsContainer().getStorageWrapper().getMainSettingsTabIcon();
        if (!icon.isEmpty()) {
            return new com.exiledradio.scorelegacy.client.gui.controls.ItemButton(buttonPosition, onTabIconClicked, () -> icon);
        }
        return new com.exiledradio.scorelegacy.client.gui.controls.Button(buttonPosition,
                new ButtonDefinition(Dimension.SQUARE_16, null, null,
                        new com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(64, 48), Dimension.SQUARE_16),
                        java.util.Collections::emptyList), onTabIconClicked);
    }

    @Override
    public void handleSlotClick(int slotIndex, int mouseButton) {
        //noop
    }

    /** A wide text button: "Player" or "Backpack". */
    private static class ContextButton extends ButtonBase {
        private static final int WIDTH = 60;
        private final MainSettingsContainer container;

        ContextButton(Position position, IntConsumer onClick, MainSettingsContainer container) {
            super(position, new Dimension(WIDTH, 18), onClick);
            this.container = container;
        }

        private String getKey() {
            return container.getContext() == MainSettingsContainer.Context.PLAYER ? TranslationHelper.translSettingsButton("context_player")
                    : container.getSettingsContainer().getStorageWrapper().getStorageContextButtonKey();
        }

        @Override
        protected void renderBg(int mouseX, int mouseY) {
            GuiHelper.renderControlBackground(x, y, WIDTH, 18);
        }

        @Override
        protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
            String text = I18n.format(getKey());
            font.drawStringWithShadow(text, x + (WIDTH - font.getStringWidth(text)) / 2.0F, y + 5, isMouseOver(mouseX, mouseY) ? 0xFFFFA0 : 0xFFFFFF);
            if (isMouseOver(mouseX, mouseY)) {
                GuiHelper.setTooltipToRender(TranslationHelper.tooltip(getKey() + ".tooltip", getKey() + ".tooltip_detail").get());
            }
        }
    }
}
