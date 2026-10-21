/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.upgrades.xppump.XpPumpUpgradeItem;
import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeSettingsTab;
import com.exiledradio.scorelegacy.client.gui.controls.Button;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.controls.Widget;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.upgrades.xppump.AutomationDirection;
import com.exiledradio.scorelegacy.upgrades.xppump.XpPumpUpgradeContainer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.createToggleButtonDefinition;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.getBooleanStateData;
import static com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions.icon;

/**
 * Direction and stop level (scroll to change), Mending repair, and buttons to take all, take
 * N levels, store N levels (scroll either to change N) and store all.
 */
public class XpPumpUpgradeTab extends UpgradeSettingsTab<XpPumpUpgradeContainer> {
    private static final ButtonDefinition.Toggle<AutomationDirection> DIRECTION = createToggleButtonDefinition(ImmutableMap.of(
            AutomationDirection.INPUT, icon(144, 0, "xp_pump_input"),
            AutomationDirection.OUTPUT, icon(128, 16, "xp_pump_output"),
            AutomationDirection.OFF, icon(240, 0, "xp_pump_off")));
    private static final ButtonDefinition.Toggle<Boolean> MEND_ITEMS = createToggleButtonDefinition(
            getBooleanStateData(icon(144, 32, "mend_items"), icon(160, 32, "do_not_mend_items")));
    private static final ButtonDefinition STORE_ALL = button(192, 16, "store_all_experience");
    private static final ButtonDefinition TAKE_ALL = button(144, 16, "take_all_experience");
    private static final ButtonDefinition TAKE = button(160, 16, null);
    private static final ButtonDefinition STORE = button(176, 16, null);

    private static ButtonDefinition button(int u, int v, String tooltipKey) {
        TextureBlitData foreground = new TextureBlitData(GuiHelper.ICONS, new Position(1, 1), Dimension.SQUARE_256, new UV(u, v), Dimension.SQUARE_16);
        Supplier<List<String>> tooltip = tooltipKey == null ? ImmutableList::of : TranslationHelper.tooltip(TranslationHelper.translUpgradeButton(tooltipKey));
        return new ButtonDefinition(Dimension.SQUARE_18, GuiHelper.DEFAULT_BUTTON_BACKGROUND, GuiHelper.DEFAULT_BUTTON_HOVERED_BACKGROUND, foreground, tooltip);
    }

    public XpPumpUpgradeTab(XpPumpUpgradeContainer container, Position position, StorageScreenBase screen) {
        super(container, position, screen, "xp_pump");
        int currentYOffset = 24;
        addHideableChild(new ToggleButton<>(new Position(x + 3, y + currentYOffset), DIRECTION,
                button -> container.setDirection(container.getDirection().next()), container::getDirection));
        addHideableChild(new LevelSelector(new Position(x + 21, y + currentYOffset), () -> String.valueOf(container.getLevel()),
                delta -> container.setLevel(container.getLevel() + (delta > 0 ? 1 : -1))));
        currentYOffset += 20;
        if (((XpPumpUpgradeItem) container.getUpgradeStack().getItem()).getConfig().mendingOn) {
            addHideableChild(new ToggleButton<>(new Position(x + 3, y + currentYOffset), MEND_ITEMS,
                    button -> container.setMendItems(!container.shouldMendItems()), container::shouldMendItems));
            currentYOffset += 20;
        }
        addHideableChild(new Button(new Position(x + 3, y + currentYOffset), TAKE_ALL, button -> container.takeAllExperience()));
        addHideableChild(new ScrollableButton(new Position(x + 21, y + currentYOffset), TAKE, button -> container.takeLevels(),
                delta -> container.setLevelsToTake(container.getLevelsToTake() + (delta > 0 ? 1 : -1)),
                () -> levelsTooltip("take_levels", container.getLevelsToTake(), TextFormatting.GREEN)));
        addHideableChild(new ScrollableButton(new Position(x + 39, y + currentYOffset), STORE, button -> container.storeLevels(),
                delta -> container.setLevelsToStore(container.getLevelsToStore() + (delta > 0 ? 1 : -1)),
                () -> levelsTooltip("store_levels", container.getLevelsToStore(), TextFormatting.RED)));
        addHideableChild(new Button(new Position(x + 57, y + currentYOffset), STORE_ALL, button -> container.storeAllExperience()));
    }

    private static List<String> levelsTooltip(String key, int levels, TextFormatting levelsColor) {
        return ImmutableList.of(
                I18n.format(TranslationHelper.translUpgradeButton(key), levelsColor.toString() + levels + TextFormatting.RESET),
                TextFormatting.DARK_GRAY.toString() + TextFormatting.ITALIC + I18n.format(TranslationHelper.translUpgradeButton(key + ".controls")));
    }

    @Override
    protected void moveSlotsToTab() {
        //noop
    }

    /** A button whose tooltip shows a count the mouse wheel changes. */
    private static class ScrollableButton extends Button {
        private final IntConsumer onScroll;

        ScrollableButton(Position position, ButtonDefinition definition, IntConsumer onClick, IntConsumer onScroll, Supplier<List<String>> tooltip) {
            super(position, definition, onClick);
            this.onScroll = onScroll;
            setTooltip(tooltip);
        }

        @Override
        public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
            onScroll.accept(delta);
            return true;
        }
    }

    /** The level the pump stops at, changed with the mouse wheel. */
    private static class LevelSelector extends Widget {
        private final Supplier<String> getText;
        private final IntConsumer onScroll;

        LevelSelector(Position position, Supplier<String> getText, IntConsumer onScroll) {
            super(position);
            this.getText = getText;
            this.onScroll = onScroll;
        }

        @Override
        protected void renderBg(int mouseX, int mouseY) {
            GuiHelper.renderControlBackground(x, y, 54, 18);
        }

        @Override
        protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
            String text = TextFormatting.GRAY + I18n.format("gui.scorelegacy.upgrades.controls.xp_level_select", TextFormatting.WHITE + getText.get() + TextFormatting.GRAY);
            int xOffset = (getWidth() - font.getStringWidth(text)) / 2;
            int yOffset = (int) Math.ceil((getHeight() - font.FONT_HEIGHT) / 2d);
            font.drawString(text, x + xOffset, y + yOffset, 0);
            if (isMouseOver(mouseX, mouseY)) {
                GuiHelper.setTooltipToRender(ImmutableList.of(
                        I18n.format("gui.scorelegacy.upgrades.controls.xp_level_select.tooltip"),
                        TextFormatting.DARK_GRAY.toString() + TextFormatting.ITALIC + I18n.format("gui.scorelegacy.upgrades.controls.xp_level_select.tooltip.controls")));
            }
        }

        @Override
        public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
            onScroll.accept(delta);
            return true;
        }

        @Override
        public int getWidth() {
            return 54;
        }

        @Override
        public int getHeight() {
            return 18;
        }
    }
}
