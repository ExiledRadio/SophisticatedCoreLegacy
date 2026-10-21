/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.settings;

import com.exiledradio.scorelegacy.client.gui.SettingsTabBase;
import com.exiledradio.scorelegacy.client.gui.controls.Button;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.settings.SettingsCategoryContainer;
import net.minecraft.client.gui.inventory.GuiContainer;

import javax.annotation.Nullable;
import java.util.Collections;

/** One settings category's tab in the settings screen. Slot clicks go to the open tab. */
public abstract class SettingsTab<C extends SettingsCategoryContainer<?>> extends SettingsTabBase {
    private final C container;

    protected SettingsTab(C container, Position position, GuiContainer screen, String categoryName, UV iconUv, boolean hasOpenDetail) {
        super(position, screen, TranslationHelper.translSettings(categoryName),
                hasOpenDetail ? TranslationHelper.tooltip(TranslationHelper.translSettings(categoryName) + ".tooltip", TranslationHelper.translSettings(categoryName) + ".tooltip_detail")
                        : TranslationHelper.tooltip(TranslationHelper.translSettings(categoryName) + ".tooltip"),
                onTabIconClicked -> new Button(new Position(position.getX() + 1, position.getY() + 4),
                        new ButtonDefinition(Dimension.SQUARE_16, null, null, new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, iconUv, Dimension.SQUARE_16),
                                Collections::emptyList), onTabIconClicked));
        this.container = container;
    }

    /** A tab with its own label key (".tooltip" appended for the tooltip) and tab button. */
    protected SettingsTab(C container, Position position, GuiContainer screen, String labelKey,
                          java.util.function.Function<java.util.function.IntConsumer, com.exiledradio.scorelegacy.client.gui.controls.ButtonBase> getTabButton) {
        super(position, screen, labelKey, TranslationHelper.tooltip(labelKey + ".tooltip"), getTabButton);
        this.container = container;
    }

    protected C getContainer() {
        return container;
    }

    /** ARGB colour this tab paints over the slot, or null. */
    @Nullable
    public Integer getSlotOverlayColor(int slotIndex) {
        return null;
    }

    /** Rotation in degrees this tab wants the slot's item drawn at (item display). */
    public int getItemRotation(int slotIndex) {
        return 0;
    }

    public abstract void handleSlotClick(int slotIndex, int mouseButton);

    /** A plain icon button from the icons texture, with a tooltip. */
    protected static ButtonDefinition iconButton(UV uv, String tooltipKey, String... detailKeys) {
        return new ButtonDefinition(Dimension.SQUARE_16, GuiHelper.DEFAULT_BUTTON_BACKGROUND, GuiHelper.DEFAULT_BUTTON_HOVERED_BACKGROUND,
                new TextureBlitData(GuiHelper.ICONS, new Position(1, 1), Dimension.SQUARE_256, uv, Dimension.SQUARE_16),
                TranslationHelper.tooltip(tooltipKey, detailKeys));
    }
}
