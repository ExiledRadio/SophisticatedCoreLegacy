/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.settings;

import com.exiledradio.scorelegacy.client.gui.controls.ButtonBase;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.settings.SettingsColors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumDyeColor;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Cycles a dye colour: left click next, right click previous. */
public class ColorToggleButton extends ButtonBase {
    private final Supplier<EnumDyeColor> getColor;

    public ColorToggleButton(Position position, Supplier<EnumDyeColor> getColor, Consumer<EnumDyeColor> setColor) {
        super(position, Dimension.SQUARE_18, button -> {
            if (button == 0) {
                setColor.accept(SettingsColors.next(getColor.get()));
            } else if (button == 1) {
                setColor.accept(SettingsColors.previous(getColor.get()));
            }
        });
        this.getColor = getColor;
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        GuiHelper.blit(x, y, isMouseOver(mouseX, mouseY) ? GuiHelper.DEFAULT_BUTTON_HOVERED_BACKGROUND : GuiHelper.DEFAULT_BUTTON_BACKGROUND);
    }

    @Override
    protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
        int color = SettingsColors.rgb(getColor.get()) | (200 << 24);
        GlStateManager.disableDepth();
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(x + 3, y + 3, x + 15, y + 15, color, color);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableDepth();
        if (isMouseOver(mouseX, mouseY)) {
            GuiHelper.setTooltipToRender(TranslationHelper.tooltip(TranslationHelper.translSettingsButton("toggle_color"),
                    TranslationHelper.translSettingsButton("toggle_color_detail")).get());
        }
    }
}
