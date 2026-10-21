/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui;

import com.exiledradio.scorelegacy.client.gui.controls.ButtonBase;
import com.exiledradio.scorelegacy.client.gui.controls.CompositeWidget;
import com.exiledradio.scorelegacy.client.gui.controls.Widget;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;

import java.awt.Rectangle;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** A tab hanging off the right side of the backpack GUI. Closed it is just an icon. */
public abstract class Tab extends CompositeWidget<Widget> {
    public static final int DEFAULT_HEIGHT = 24;
    protected static final int DEFAULT_WIDTH = 21;
    private static final int TEXTURE_SIZE = 256;

    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private final Supplier<List<String>> tooltip;
    private BooleanSupplier shouldShowTooltip = () -> true;
    private BooleanSupplier shouldRender = () -> true;

    protected Tab(Position position, Supplier<List<String>> tooltip, Function<IntConsumer, ButtonBase> getTabButton) {
        super(position);
        this.tooltip = tooltip;
        addChild(getTabButton.apply(this::onTabIconClicked));
    }

    public void setHandlers(BooleanSupplier shouldShowTooltip, BooleanSupplier shouldRender) {
        this.shouldShowTooltip = shouldShowTooltip;
        this.shouldRender = shouldRender;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!shouldRender.getAsBoolean()) {
            return;
        }
        if (isClosedTooltipVisible(mouseX, mouseY)) {
            GuiHelper.setTooltipToRender(tooltip.get());
        }
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Rectangle getRectangle() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        int halfHeight = height / 2;
        GuiHelper.blit(GuiHelper.GUI_CONTROLS, x, y, TEXTURE_SIZE - width, 0, width, halfHeight);
        GuiHelper.blit(GuiHelper.GUI_CONTROLS, x, y + halfHeight, TEXTURE_SIZE - width, TEXTURE_SIZE - (height - halfHeight), width, height - halfHeight);
        GuiHelper.blit(GuiHelper.GUI_CONTROLS, x - 3, y, TEXTURE_SIZE / 2, TEXTURE_SIZE - height, 3, height);
    }

    protected boolean isClosedTooltipVisible(int mouseX, int mouseY) {
        return shouldShowTooltip.getAsBoolean() && isMouseOver(mouseX, mouseY);
    }

    public int getTopY() {
        return y;
    }

    public int getBottomY() {
        return y + getHeight();
    }

    protected abstract void onTabIconClicked(int button);
}
