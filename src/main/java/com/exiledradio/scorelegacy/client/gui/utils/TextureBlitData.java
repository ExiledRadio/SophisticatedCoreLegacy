/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.utils;

import net.minecraft.util.ResourceLocation;

/** A rectangle of a texture to draw, with an optional offset from the draw position. */
public final class TextureBlitData {
    private final ResourceLocation textureName;
    private final int xOffset;
    private final int yOffset;
    private final int textureWidth;
    private final int textureHeight;
    private final int u;
    private final int v;
    private final int width;
    private final int height;

    public TextureBlitData(ResourceLocation textureName, UV uv, Dimension dimension) {
        this(textureName, new Position(0, 0), Dimension.SQUARE_256, uv, dimension);
    }

    public TextureBlitData(ResourceLocation textureName, Dimension textureDimension, UV uv, Dimension dimension) {
        this(textureName, new Position(0, 0), textureDimension, uv, dimension);
    }

    public TextureBlitData(ResourceLocation textureName, Position offset, Dimension textureDimension, UV uv, Dimension dimension) {
        this.textureName = textureName;
        xOffset = offset.getX();
        yOffset = offset.getY();
        textureWidth = textureDimension.getWidth();
        textureHeight = textureDimension.getHeight();
        u = uv.getU();
        v = uv.getV();
        width = dimension.getWidth();
        height = dimension.getHeight();
    }

    public ResourceLocation getTextureName() {
        return textureName;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public int getU() {
        return u;
    }

    public int getV() {
        return v;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
