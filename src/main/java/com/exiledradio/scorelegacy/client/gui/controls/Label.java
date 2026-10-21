/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.controls;

import com.exiledradio.scorelegacy.client.gui.utils.Position;
import net.minecraft.client.resources.I18n;

public class Label extends Widget {
    private static final int DEFAULT_GUI_TEXT_COLOR = 4210752;
    private final String translationKey;
    private final int color;

    public Label(Position position, String translationKey) {
        this(position, translationKey, DEFAULT_GUI_TEXT_COLOR);
    }

    public Label(Position position, String translationKey, int color) {
        super(position);
        this.translationKey = translationKey;
        this.color = color;
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        //noop
    }

    @Override
    protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
        font.drawString(I18n.format(translationKey), x, y, color);
    }

    @Override
    public int getWidth() {
        return font.getStringWidth(I18n.format(translationKey));
    }

    @Override
    public int getHeight() {
        return 8;
    }
}
