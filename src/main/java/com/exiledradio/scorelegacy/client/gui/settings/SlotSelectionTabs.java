/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.settings;

import com.exiledradio.scorelegacy.client.gui.controls.Button;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.settings.itemdisplay.DisplaySide;
import com.google.common.collect.ImmutableMap;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.settings.SettingsColors;
import com.exiledradio.scorelegacy.settings.itemdisplay.ItemDisplaySettingsCategory;
import com.exiledradio.scorelegacy.settings.itemdisplay.ItemDisplaySettingsContainer;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsContainer;
import com.exiledradio.scorelegacy.settings.nosort.NoSortSettingsCategory;
import com.exiledradio.scorelegacy.settings.nosort.NoSortSettingsContainer;
import net.minecraft.client.gui.inventory.GuiContainer;

import javax.annotation.Nullable;

/** The settings tabs that work by clicking slots: no-sort, memory and item display. */
public final class SlotSelectionTabs {
    private static final ButtonDefinition SELECT_ALL_SLOTS = SettingsTab.iconButton(new UV(16, 80), TranslationHelper.translSettingsButton("select_all_slots"));
    private static final ButtonDefinition UNSELECT_ALL_SLOTS = SettingsTab.iconButton(new UV(48, 80), TranslationHelper.translSettingsButton("unselect_all_slots"));
    private static final ButtonDefinition ROTATE = SettingsTab.iconButton(new UV(128, 64), TranslationHelper.translSettingsButton("rotate"),
            TranslationHelper.translSettingsButton("rotate_detail"));
    private static final ButtonDefinition.Toggle<DisplaySide> DISPLAY_SIDE = ButtonDefinitions.createToggleButtonDefinition(
            ImmutableMap.of(
                    DisplaySide.FRONT, GuiHelper.getButtonStateData(new UV(144, 64), TranslationHelper.translSettingsButton("display_side_front"), Dimension.SQUARE_16, new Position(1, 1)),
                    DisplaySide.LEFT, GuiHelper.getButtonStateData(new UV(160, 64), TranslationHelper.translSettingsButton("display_side_left"), Dimension.SQUARE_16, new Position(1, 1)),
                    DisplaySide.RIGHT, GuiHelper.getButtonStateData(new UV(176, 64), TranslationHelper.translSettingsButton("display_side_right"), Dimension.SQUARE_16, new Position(1, 1))));
    private static final int OVERLAY_ALPHA = 80 << 24;

    private SlotSelectionTabs() {}

    /** Left click selects a slot, right click unselects it. */
    public static class NoSortTab extends SettingsTab<NoSortSettingsContainer> {
        public NoSortTab(NoSortSettingsContainer container, Position position, GuiContainer screen) {
            super(container, position, screen, NoSortSettingsCategory.NAME, new UV(32, 80), true);
            addHideableChild(new Button(new Position(x + 3, y + 24), SELECT_ALL_SLOTS, button -> container.selectAllSlots()));
            addHideableChild(new Button(new Position(x + 21, y + 24), UNSELECT_ALL_SLOTS, button -> container.unselectAllSlots()));
            addHideableChild(new ColorToggleButton(new Position(x + 39, y + 24), container::getColor, container::setColor));
        }

        @Nullable
        @Override
        public Integer getSlotOverlayColor(int slotIndex) {
            return getContainer().isSlotSelected(slotIndex) ? SettingsColors.rgb(getContainer().getColor()) | OVERLAY_ALPHA : null;
        }

        @Override
        public void handleSlotClick(int slotIndex, int mouseButton) {
            if (mouseButton == 0) {
                getContainer().selectSlot(slotIndex);
            } else if (mouseButton == 1) {
                getContainer().unselectSlot(slotIndex);
            }
        }
    }

    /** Left click memorizes what is in a slot, right click forgets it. */
    public static class MemoryTab extends SettingsTab<MemorySettingsContainer> {
        public MemoryTab(MemorySettingsContainer container, Position position, GuiContainer screen) {
            super(container, position, screen, MemorySettingsCategory.NAME, new UV(128, 32), true);
            addHideableChild(new Button(new Position(x + 3, y + 24), SELECT_ALL_SLOTS, button -> container.selectAllSlots()));
            addHideableChild(new Button(new Position(x + 21, y + 24), UNSELECT_ALL_SLOTS, button -> container.unselectAllSlots()));
        }

        @Override
        public void handleSlotClick(int slotIndex, int mouseButton) {
            if (mouseButton == 0) {
                getContainer().selectSlot(slotIndex);
            } else if (mouseButton == 1) {
                getContainer().unselectSlot(slotIndex);
            }
        }
    }

    /** Left click picks the shown slot, right click clears it; the rotate button turns the item. */
    public static class ItemDisplayTab extends SettingsTab<ItemDisplaySettingsContainer> {
        /** The slot the rotate button turns: the last one clicked, as in the original. */
        private int currentSelectedSlot = -1;

        public ItemDisplayTab(ItemDisplaySettingsContainer container, Position position, GuiContainer screen) {
            super(container, position, screen, ItemDisplaySettingsCategory.NAME, new UV(112, 64), true);
            addHideableChild(new Button(new Position(x + 3, y + 24), ROTATE, button -> {
                int slot = getRotatedSlot();
                if (slot < 0) {
                    return;
                }
                if (button == 0) {
                    container.rotate(slot, true);
                } else if (button == 1) {
                    container.rotate(slot, false);
                }
            }));
            addHideableChild(new ColorToggleButton(new Position(x + 21, y + 24), container::getColor, container::setColor));
            if (container.canChangeDisplaySide()) {
                addHideableChild(new ToggleButton<>(new Position(x + 39, y + 24), DISPLAY_SIDE, button -> {
                    if (button == 0) {
                        container.setDisplaySide(container.getDisplaySide().next());
                    } else if (button == 1) {
                        container.setDisplaySide(container.getDisplaySide().previous());
                    }
                }, container::getDisplaySide));
            }
        }

        private int getRotatedSlot() {
            return currentSelectedSlot >= 0 && getContainer().isSlotSelected(currentSelectedSlot) ? currentSelectedSlot : getContainer().getFirstSelectedSlot();
        }

        @Nullable
        @Override
        public Integer getSlotOverlayColor(int slotIndex) {
            return getContainer().isSlotSelected(slotIndex) ? SettingsColors.rgb(getContainer().getColor()) | OVERLAY_ALPHA : null;
        }

        @Override
        public int getItemRotation(int slotIndex) {
            return getContainer().isSlotSelected(slotIndex) ? getContainer().getRotation(slotIndex) : 0;
        }

        @Override
        public void handleSlotClick(int slotIndex, int mouseButton) {
            if (mouseButton == 0) {
                getContainer().selectSlot(slotIndex);
                currentSelectedSlot = slotIndex;
            } else if (mouseButton == 1) {
                getContainer().unselectSlot(slotIndex);
                if (currentSelectedSlot == slotIndex) {
                    currentSelectedSlot = -1;
                }
            }
        }
    }
}
