/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings.itemdisplay;

import com.exiledradio.scorelegacy.common.gui.SettingsContainerBase;
import com.exiledradio.scorelegacy.settings.SettingsCategoryContainer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;

public class ItemDisplaySettingsContainer extends SettingsCategoryContainer<ItemDisplaySettingsCategory> {
    private static final String COLOR_TAG = "color";
    private static final String DISPLAY_SIDE_TAG = "displaySide";
    private static final String SELECT_SLOT_TAG = "selectSlot";
    private static final String UNSELECT_SLOT_TAG = "unselectSlot";
    private static final String ROTATE_CLOCKWISE_TAG = "rotateClockwise";
    private static final String ROTATE_COUNTER_CLOCKWISE_TAG = "rotateCounterClockwise";

    public ItemDisplaySettingsContainer(SettingsContainerBase settingsContainer, String categoryName, ItemDisplaySettingsCategory category) {
        super(settingsContainer, categoryName, category);
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(SELECT_SLOT_TAG)) {
            selectSlot(data.getInteger(SELECT_SLOT_TAG));
        } else if (data.hasKey(UNSELECT_SLOT_TAG)) {
            unselectSlot(data.getInteger(UNSELECT_SLOT_TAG));
        } else if (data.hasKey(ROTATE_CLOCKWISE_TAG)) {
            rotate(data.getInteger(ROTATE_CLOCKWISE_TAG), true);
        } else if (data.hasKey(ROTATE_COUNTER_CLOCKWISE_TAG)) {
            rotate(data.getInteger(ROTATE_COUNTER_CLOCKWISE_TAG), false);
        } else if (data.hasKey(COLOR_TAG)) {
            setColor(EnumDyeColor.byMetadata(data.getInteger(COLOR_TAG)));
        } else if (data.hasKey(DISPLAY_SIDE_TAG)) {
            setDisplaySide(DisplaySide.fromName(data.getString(DISPLAY_SIDE_TAG)));
        }
    }

    public void selectSlot(int slot) {
        if (isSlotSelected(slot)) {
            return;
        }
        if (isServer()) {
            getCategory().selectSlot(slot);
        } else {
            sendIntToServer(SELECT_SLOT_TAG, slot);
        }
    }

    public void unselectSlot(int slot) {
        if (!isSlotSelected(slot)) {
            return;
        }
        if (isServer()) {
            getCategory().unselectSlot(slot);
        } else {
            sendIntToServer(UNSELECT_SLOT_TAG, slot);
        }
    }

    public void rotate(int slot, boolean clockwise) {
        if (isServer()) {
            getCategory().rotate(slot, clockwise);
        } else {
            sendIntToServer(clockwise ? ROTATE_CLOCKWISE_TAG : ROTATE_COUNTER_CLOCKWISE_TAG, slot);
        }
    }

    public void setColor(EnumDyeColor color) {
        if (isServer()) {
            getCategory().setColor(color);
        } else {
            sendIntToServer(COLOR_TAG, color.getMetadata());
        }
    }

    public void setDisplaySide(DisplaySide displaySide) {
        if (isServer()) {
            if (displaySide == DisplaySide.FRONT || getSettingsContainer().getStorageWrapper().canChangeDisplaySide()) {
                getCategory().setDisplaySide(displaySide);
            }
        } else {
            sendStringToServer(DISPLAY_SIDE_TAG, displaySide.getName());
        }
    }

    public DisplaySide getDisplaySide() {
        return getCategory().getDisplaySide();
    }

    public boolean canChangeDisplaySide() {
        return getSettingsContainer().getStorageWrapper().canChangeDisplaySide();
    }

    public boolean isSlotSelected(int slot) {
        return getCategory().isSlotSelected(slot);
    }

    /** The first shown slot, or -1. */
    public int getFirstSelectedSlot() {
        return getCategory().getSlot();
    }

    public int getRotation(int slot) {
        return getCategory().getRotation(slot);
    }

    public EnumDyeColor getColor() {
        return getCategory().getColor();
    }
}
