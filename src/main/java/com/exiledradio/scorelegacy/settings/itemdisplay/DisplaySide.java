/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings.itemdisplay;

/** Which face shows the display items: the front, or a double chest's left or right end (the original 1.20's DisplaySide). */
public enum DisplaySide {
    FRONT("front"), LEFT("left"), RIGHT("right");

    private static final DisplaySide[] VALUES = values();
    private final String name;

    DisplaySide(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public DisplaySide next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public DisplaySide previous() {
        return VALUES[(ordinal() + VALUES.length - 1) % VALUES.length];
    }

    public static DisplaySide fromName(String name) {
        for (DisplaySide side : VALUES) {
            if (side.name.equals(name)) {
                return side;
            }
        }
        return FRONT;
    }
}
