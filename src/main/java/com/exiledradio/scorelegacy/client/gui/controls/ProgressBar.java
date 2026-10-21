/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.controls;

import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import net.minecraft.client.renderer.GlStateManager;

import java.util.function.Supplier;

/** Draws part of a texture according to a 0-1 progress value (furnace arrow and flame). */
public class ProgressBar extends Widget {
    private final TextureBlitData progressTexture;
    private final Supplier<Float> getProgress;
    private final ProgressDirection direction;

    public ProgressBar(Position position, TextureBlitData progressTexture, Supplier<Float> getProgress, ProgressDirection direction) {
        super(position);
        this.progressTexture = progressTexture;
        this.getProgress = getProgress;
        this.direction = direction;
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        //noop
    }

    @Override
    protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
        float progress = getProgress.get();
        if (progress <= 0) {
            return;
        }
        int width = progressTexture.getWidth();
        int height = progressTexture.getHeight();
        int yOffset = 0;
        if (direction == ProgressDirection.BOTTOM_UP) {
            yOffset = (int) (height * (1 - progress));
            height -= yOffset;
        } else {
            width = (int) (width * progress);
        }
        minecraft.getTextureManager().bindTexture(progressTexture.getTextureName());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawModalRectWithCustomSizedTexture(x, y + yOffset, progressTexture.getU(), progressTexture.getV() + yOffset, width, height,
                progressTexture.getTextureWidth(), progressTexture.getTextureHeight());
    }

    @Override
    public int getWidth() {
        return progressTexture.getWidth();
    }

    @Override
    public int getHeight() {
        return progressTexture.getHeight();
    }

    public enum ProgressDirection {
        BOTTOM_UP,
        LEFT_RIGHT
    }
}
