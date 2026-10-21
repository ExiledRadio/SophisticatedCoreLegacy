/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.controls;

import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class Button extends ButtonBase {
    @Nullable
    private final TextureBlitData backgroundTexture;
    @Nullable
    private final TextureBlitData hoveredBackgroundTexture;
    @Nullable
    private final TextureBlitData foregroundTexture;
    private Supplier<List<String>> tooltip;

    public Button(Position position, ButtonDefinition buttonDefinition, IntConsumer onClick) {
        super(position, buttonDefinition.getDimension(), onClick);
        backgroundTexture = buttonDefinition.getBackgroundTexture();
        foregroundTexture = buttonDefinition.getForegroundTexture();
        hoveredBackgroundTexture = buttonDefinition.getHoveredBackgroundTexture();
        tooltip = buttonDefinition.getTooltip();
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        if (isMouseOver(mouseX, mouseY)) {
            if (hoveredBackgroundTexture != null) {
                GuiHelper.blit(x, y, hoveredBackgroundTexture);
            }
        } else if (backgroundTexture != null) {
            GuiHelper.blit(x, y, backgroundTexture);
        }
    }

    @Override
    protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
        if (foregroundTexture != null) {
            GuiHelper.blit(x, y, foregroundTexture);
        }
        if (isMouseOver(mouseX, mouseY)) {
            List<String> lines = getTooltip();
            if (!lines.isEmpty()) {
                GuiHelper.setTooltipToRender(lines);
            }
        }
    }

    protected List<String> getTooltip() {
        return tooltip.get();
    }

    public void setTooltip(Supplier<List<String>> tooltip) {
        this.tooltip = tooltip;
    }
}
