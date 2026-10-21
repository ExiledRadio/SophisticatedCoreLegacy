/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.controls;

import com.exiledradio.scorelegacy.client.gui.utils.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

/** Base of every GUI element on the backpack screen (1.16's BackpackWidget). */
public abstract class Widget extends Gui {
    protected final int x;
    protected final int y;
    protected final Minecraft minecraft;
    protected final FontRenderer font;
    private boolean visible = true;

    protected Widget(Position position) {
        x = position.getX();
        y = position.getY();
        minecraft = Minecraft.getMinecraft();
        font = minecraft.fontRenderer;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        renderBg(mouseX, mouseY);
        renderWidget(mouseX, mouseY, partialTicks);
    }

    protected abstract void renderBg(int mouseX, int mouseY);

    protected abstract void renderWidget(int mouseX, int mouseY, float partialTicks);

    public abstract int getWidth();

    public abstract int getHeight();

    public boolean isMouseOver(int mouseX, int mouseY) {
        return visible && mouseX >= x && mouseX < x + getWidth() && mouseY >= y && mouseY < y + getHeight();
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
        return false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
