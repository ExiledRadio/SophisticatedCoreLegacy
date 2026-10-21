/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.controls;

import com.exiledradio.scorelegacy.client.gui.utils.Position;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeWidget<T extends Widget> extends Widget {
    protected final List<T> children = new ArrayList<>();

    protected CompositeWidget(Position position) {
        super(position);
    }

    @Override
    protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
        for (T child : new ArrayList<>(children)) {
            child.render(mouseX, mouseY, partialTicks);
        }
    }

    protected <U extends T> U addChild(U child) {
        children.add(child);
        return child;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        for (T child : new ArrayList<>(children)) {
            if (child.isMouseOver(mouseX, mouseY) && child.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
        for (T child : new ArrayList<>(children)) {
            if (child.isMouseOver(mouseX, mouseY) && child.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return false;
    }
}
