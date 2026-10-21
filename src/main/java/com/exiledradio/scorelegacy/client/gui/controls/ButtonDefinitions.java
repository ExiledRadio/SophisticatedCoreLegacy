/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.controls;

import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.upgrades.PrimaryMatch;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.Map;

import static com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper.translUpgradeButton;

/** The shared buttons; icons come from the original's icons.png. */
public final class ButtonDefinitions {
    private static final Position ICON_OFFSET = new Position(1, 1);

    private ButtonDefinitions() {}

    public static Map<Boolean, ToggleButton.StateData> getBooleanStateData(ToggleButton.StateData onStateData, ToggleButton.StateData offStateData) {
        return ImmutableMap.of(true, onStateData, false, offStateData);
    }

    public static <T> ButtonDefinition.Toggle<T> createToggleButtonDefinition(Map<T, ToggleButton.StateData> stateData) {
        return new ButtonDefinition.Toggle<>(Dimension.SQUARE_18, GuiHelper.DEFAULT_BUTTON_BACKGROUND, stateData, GuiHelper.DEFAULT_BUTTON_HOVERED_BACKGROUND);
    }

    public static ToggleButton.StateData icon(int u, int v, String buttonKey) {
        return GuiHelper.getButtonStateData(new UV(u, v), translUpgradeButton(buttonKey), Dimension.SQUARE_16, ICON_OFFSET);
    }

    public static ToggleButton.StateData iconWithDetail(int u, int v, String buttonKey) {
        return GuiHelper.getButtonStateData(new UV(u, v), Dimension.SQUARE_16, ICON_OFFSET,
                TranslationHelper.tooltip(translUpgradeButton(buttonKey), translUpgradeButton(buttonKey + ".detail")));
    }

    public static final ButtonDefinition.Toggle<Boolean> ALLOW_LIST = createToggleButtonDefinition(
            getBooleanStateData(icon(0, 0, "allow"), icon(16, 0, "block")));

    public static final ButtonDefinition.Toggle<Boolean> MATCH_DURABILITY = createToggleButtonDefinition(
            getBooleanStateData(icon(0, 16, "match_durability"), icon(16, 16, "ignore_durability")));

    public static final ButtonDefinition.Toggle<Boolean> MATCH_NBT = createToggleButtonDefinition(
            getBooleanStateData(icon(32, 0, "match_nbt"), icon(48, 0, "ignore_nbt")));

    public static final ButtonDefinition.Toggle<PrimaryMatch> PRIMARY_MATCH = createToggleButtonDefinition(
            ImmutableMap.of(
                    PrimaryMatch.ITEM, icon(48, 16, "match_item"),
                    PrimaryMatch.MOD, icon(32, 16, "match_mod"),
                    PrimaryMatch.TAGS, icon(64, 0, "match_tags")));

    private static final TextureBlitData UPGRADE_SWITCH_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(65, 0), Dimension.RECTANGLE_6_12);
    private static final TextureBlitData UPGRADE_SWITCH_HOVERED_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(71, 0), Dimension.RECTANGLE_6_12);
    public static final ButtonDefinition.Toggle<Boolean> UPGRADE_SWITCH = new ButtonDefinition.Toggle<>(Dimension.RECTANGLE_6_12, UPGRADE_SWITCH_BACKGROUND, ImmutableMap.of(
            true, GuiHelper.getButtonStateData(new UV(4, 128), Dimension.RECTANGLE_4_10, ICON_OFFSET,
                    () -> Collections.singletonList(TextFormatting.GREEN + TranslationHelper.translateLines(TranslationHelper.translButton("upgrade_switch_enabled")).get(0))),
            false, GuiHelper.getButtonStateData(new UV(0, 128), Dimension.RECTANGLE_4_10, ICON_OFFSET,
                    () -> Collections.singletonList(TextFormatting.RED + TranslationHelper.translateLines(TranslationHelper.translButton("upgrade_switch_disabled")).get(0)))
    ), UPGRADE_SWITCH_HOVERED_BACKGROUND);

    public static final ButtonDefinition.Toggle<Boolean> WORK_IN_GUI = createToggleButtonDefinition(
            getBooleanStateData(icon(0, 48, "works_in_gui"), icon(16, 48, "only_automatic")));

    public static final ButtonDefinition.Toggle<Boolean> SHIFT_CLICK_TARGET = createToggleButtonDefinition(
            getBooleanStateData(icon(32, 48, "shift_click_into_backpack"), icon(48, 48, "shift_click_into_inventory")));

    public static final ButtonDefinition.Toggle<Boolean> MATCH_ANY_TAG = createToggleButtonDefinition(
            getBooleanStateData(icon(0, 80, "match_any_tag"), icon(16, 80, "match_all_tags")));

    private static final TextureBlitData ADD_TAG_FOREGROUND = new TextureBlitData(GuiHelper.ICONS, ICON_OFFSET, Dimension.SQUARE_256, new UV(96, 32), Dimension.SQUARE_16);
    public static final ButtonDefinition ADD_TAG = new ButtonDefinition(Dimension.SQUARE_18, GuiHelper.DEFAULT_BUTTON_BACKGROUND, GuiHelper.DEFAULT_BUTTON_HOVERED_BACKGROUND, ADD_TAG_FOREGROUND, Collections::emptyList);

    private static final TextureBlitData REMOVE_TAG_FOREGROUND = new TextureBlitData(GuiHelper.ICONS, ICON_OFFSET, Dimension.SQUARE_256, new UV(112, 32), Dimension.SQUARE_16);
    public static final ButtonDefinition REMOVE_TAG = new ButtonDefinition(Dimension.SQUARE_18, GuiHelper.DEFAULT_BUTTON_BACKGROUND, GuiHelper.DEFAULT_BUTTON_HOVERED_BACKGROUND, REMOVE_TAG_FOREGROUND, Collections::emptyList);
}
