/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.utils;

public final class Dimension {
    public static final Dimension SQUARE_12 = new Dimension(12, 12);
    public static final Dimension SQUARE_16 = new Dimension(16, 16);
    public static final Dimension SQUARE_18 = new Dimension(18, 18);
    public static final Dimension SQUARE_256 = new Dimension(256, 256);
    public static final Dimension RECTANGLE_6_12 = new Dimension(6, 12);
    public static final Dimension RECTANGLE_4_10 = new Dimension(4, 10);

    private final int width;
    private final int height;

    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
