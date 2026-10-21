/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.controls;

import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.util.function.IntConsumer;

public abstract class ButtonBase extends Widget {
    protected final int width;
    protected final int height;
    protected IntConsumer onClick;

    protected ButtonBase(Position position, Dimension dimension, IntConsumer onClick) {
        super(position);
        width = dimension.getWidth();
        height = dimension.getHeight();
        this.onClick = onClick;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        onClick.accept(button);
        minecraft.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return true;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
