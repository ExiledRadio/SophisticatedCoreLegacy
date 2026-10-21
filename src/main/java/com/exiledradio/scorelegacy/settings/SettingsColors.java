package com.exiledradio.scorelegacy.settings;

import net.minecraft.item.EnumDyeColor;

/** Dye colours as RGB, the way 1.16's DyeColor.getColorValue() gives them. */
public final class SettingsColors {
    private SettingsColors() {}

    public static int rgb(EnumDyeColor color) {
        float[] c = color.getColorComponentValues();
        return ((int) (c[0] * 255.0F) << 16) | ((int) (c[1] * 255.0F) << 8) | (int) (c[2] * 255.0F);
    }

    public static EnumDyeColor next(EnumDyeColor color) {
        EnumDyeColor[] values = EnumDyeColor.values();
        return values[(color.ordinal() + 1) % values.length];
    }

    public static EnumDyeColor previous(EnumDyeColor color) {
        EnumDyeColor[] values = EnumDyeColor.values();
        return values[(color.ordinal() - 1 + values.length) % values.length];
    }
}
