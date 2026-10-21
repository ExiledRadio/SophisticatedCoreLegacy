/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.settings.memory;

import com.exiledradio.scorelegacy.common.gui.SettingsContainerBase;
import com.exiledradio.scorelegacy.settings.SettingsCategoryContainer;
import net.minecraft.nbt.NBTTagCompound;

public class MemorySettingsContainer extends SettingsCategoryContainer<MemorySettingsCategory> {
    private static final String ACTION_TAG = "action";
    private static final String SELECT_ALL_ACTION = "selectAll";
    private static final String UNSELECT_ALL_ACTION = "unselectAll";
    private static final String SELECT_SLOT_TAG = "selectSlot";
    private static final String UNSELECT_SLOT_TAG = "unselectSlot";

    public MemorySettingsContainer(SettingsContainerBase settingsContainer, String categoryName, MemorySettingsCategory category) {
        super(settingsContainer, categoryName, category);
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(ACTION_TAG)) {
            if (SELECT_ALL_ACTION.equals(data.getString(ACTION_TAG))) {
                selectAllSlots();
            } else if (UNSELECT_ALL_ACTION.equals(data.getString(ACTION_TAG))) {
                unselectAllSlots();
            }
        } else if (data.hasKey(SELECT_SLOT_TAG)) {
            selectSlot(data.getInteger(SELECT_SLOT_TAG));
        } else if (data.hasKey(UNSELECT_SLOT_TAG)) {
            unselectSlot(data.getInteger(UNSELECT_SLOT_TAG));
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

    public void selectAllSlots() {
        if (isServer()) {
            getCategory().selectSlots(0, getSettingsContainer().getNumberOfSlots());
        } else {
            sendStringToServer(ACTION_TAG, SELECT_ALL_ACTION);
        }
    }

    public void unselectAllSlots() {
        if (isServer()) {
            getCategory().unselectAllSlots();
        } else {
            sendStringToServer(ACTION_TAG, UNSELECT_ALL_ACTION);
        }
    }

    public boolean isSlotSelected(int slot) {
        return getCategory().isSlotSelected(slot);
    }
}
