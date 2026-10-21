/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;

import com.exiledradio.scorelegacy.client.gui.controls.ButtonBase;
import com.exiledradio.scorelegacy.client.gui.controls.Label;
import com.exiledradio.scorelegacy.client.gui.controls.Widget;
import com.exiledradio.scorelegacy.client.gui.utils.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** A tab that opens into a panel; the panel grows to fit whatever widgets it holds. */
public abstract class SettingsTabBase extends Tab {
    private static final int RIGHT_BORDER_WIDTH = 6;
    private static final int BOTTOM_BORDER_HEIGHT = 7;

    protected final GuiContainer screen;
    protected int openWidth = 0;
    protected int openHeight = 0;
    protected boolean isOpen = false;
    private Runnable onOpen = () -> {};
    private Runnable onClose = () -> {};
    private final List<Widget> hideableChildren = new ArrayList<>();

    protected SettingsTabBase(Position position, GuiContainer screen, String tabLabelKey, Supplier<List<String>> tooltip, Function<IntConsumer, ButtonBase> getTabButton) {
        super(position, tooltip, getTabButton);
        this.screen = screen;
        addHideableChild(new Label(new Position(x + 20, y + 8), tabLabelKey));
    }

    protected <U extends Widget> U addHideableChild(U widget) {
        hideableChildren.add(widget);
        openWidth = Math.max(openWidth, widget.getX() + widget.getWidth() + RIGHT_BORDER_WIDTH - x);
        openHeight = Math.max(openHeight, widget.getY() + widget.getHeight() + BOTTOM_BORDER_HEIGHT - y);
        return widget;
    }

    public void setHandlers(Runnable onOpen, Runnable onClose, BooleanSupplier shouldRender, BooleanSupplier shouldShowTooltip) {
        this.onOpen = onOpen;
        this.onClose = onClose;
        setHandlers(shouldShowTooltip, shouldRender);
    }

    public boolean isOpen() {
        return isOpen;
    }

    @Override
    protected boolean isClosedTooltipVisible(int mouseX, int mouseY) {
        return !isOpen && super.isClosedTooltipVisible(mouseX, mouseY);
    }

    public void close() {
        setOpen(false);
    }

    protected void onTabOpen() {
        setWidth(openWidth);
        setHeight(openHeight);
        hideableChildren.forEach(this::addChild);
        onOpen.run();
    }

    protected void onTabClose() {
        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
        children.removeAll(hideableChildren);
        onClose.run();
    }

    protected void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
        if (isOpen) {
            onTabOpen();
        } else {
            onTabClose();
        }
    }

    @Override
    protected void onTabIconClicked(int button) {
        if (button == 0) {
            setOpen(!isOpen);
        }
    }
}
