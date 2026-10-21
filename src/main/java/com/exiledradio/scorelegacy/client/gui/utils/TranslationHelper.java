/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.utils;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Lang keys used by the GUI. Tooltips are kept as suppliers and translated when drawn, so a
 * language change applies straight away. Lang values store line breaks as a literal \n.
 */
public final class TranslationHelper {
    private static final String GUI_PREFIX = "gui." + SophisticatedCoreLegacy.MODID + ".";
    private static final String GUI_UPGRADE_PREFIX = GUI_PREFIX + "upgrades.";
    private static final String UPGRADE_BUTTONS_PREFIX = GUI_UPGRADE_PREFIX + "buttons.";
    private static final String BUTTONS_PREFIX = GUI_PREFIX + "buttons.";

    private TranslationHelper() {}

    public static String translUpgradeKey(String upgradeName) {
        return GUI_UPGRADE_PREFIX + upgradeName;
    }

    public static String translUpgradeButton(String buttonName) {
        return UPGRADE_BUTTONS_PREFIX + buttonName;
    }

    public static String translButton(String buttonName) {
        return BUTTONS_PREFIX + buttonName;
    }

    public static String translGui(String key) {
        return GUI_PREFIX + key;
    }

    public static String translSettings(String name) {
        return GUI_PREFIX + "settings." + name;
    }

    public static String translSettingsButton(String name) {
        return GUI_PREFIX + "settings.buttons." + name;
    }

    public static String translErrorKey(String key) {
        return GUI_PREFIX + "error." + key;
    }

    public static List<String> translateLines(String key, Object... params) {
        return new ArrayList<>(Arrays.asList(I18n.format(key, params).split("\\\\n")));
    }

    /** One tooltip: the first key in normal colour, any following keys as grey detail lines. */
    public static Supplier<List<String>> tooltip(String key, String... detailKeys) {
        return () -> {
            List<String> lines = translateLines(key);
            for (String detailKey : detailKeys) {
                for (String line : translateLines(detailKey)) {
                    lines.add(TextFormatting.GRAY + line);
                }
            }
            return lines;
        };
    }
}
