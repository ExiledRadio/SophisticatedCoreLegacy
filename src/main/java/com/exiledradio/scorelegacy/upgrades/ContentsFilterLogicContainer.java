/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import com.exiledradio.scorelegacy.common.gui.IServerUpdater;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ContentsFilterLogicContainer extends FilterLogicContainer<ContentsFilterLogic> {
    private static final String DATA_CONTENTS_FILTER_TYPE = "contentsFilterType";

    public ContentsFilterLogicContainer(Supplier<ContentsFilterLogic> filterLogic, IServerUpdater serverUpdater, Consumer<Slot> addSlot) {
        super(filterLogic, serverUpdater, addSlot);
        if (getFilterType() == ContentsFilterType.BACKPACK) {
            getFilterSlots().forEach(s -> s.setEnabled(false));
        }
    }

    public void setFilterType(ContentsFilterType filterType) {
        filterLogic.get().setDepositFilterType(filterType);
        sendDataToServer(() -> NBTHelper.withString(DATA_CONTENTS_FILTER_TYPE, filterType.getSerializedName()));
    }

    @Override
    public boolean handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_CONTENTS_FILTER_TYPE) && !isOtherFilterMessage(data)) {
            setFilterType(ContentsFilterType.fromName(data.getString(DATA_CONTENTS_FILTER_TYPE)));
            return true;
        }
        return super.handleMessage(data);
    }

    public ContentsFilterType getFilterType() {
        return filterLogic.get().getFilterType();
    }
}
