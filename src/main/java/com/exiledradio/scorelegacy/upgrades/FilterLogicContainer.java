/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import com.exiledradio.scorelegacy.common.gui.FilterSlotItemHandler;
import com.exiledradio.scorelegacy.common.gui.IServerUpdater;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FilterLogicContainer<T extends FilterLogic> extends FilterLogicContainerBase<T, FilterLogicContainer.FilterLogicSlot> {
    public FilterLogicContainer(Supplier<T> filterLogic, IServerUpdater serverUpdater, Consumer<Slot> addSlot) {
        super(serverUpdater, filterLogic, addSlot);
        int slots = filterLogic.get().getFilterHandler().getSlots();
        for (int slot = 0; slot < slots; slot++) {
            FilterLogicSlot filterSlot = new FilterLogicSlot(() -> filterLogic.get().getFilterHandler(), slot);
            addSlot.accept(filterSlot);
            filterSlots.add(filterSlot);
        }
    }

    public static class FilterLogicSlot extends FilterSlotItemHandler {
        private boolean enabled = true;

        public FilterLogicSlot(Supplier<net.minecraftforge.items.IItemHandler> filterHandler, int slot) {
            super(filterHandler, slot, -100, -100);
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @SideOnly(Side.CLIENT)
        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
