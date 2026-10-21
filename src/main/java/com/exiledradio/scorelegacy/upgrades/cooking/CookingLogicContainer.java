/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.cooking;

import com.exiledradio.scorelegacy.common.gui.SlotSuppliedHandler;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** The furnace's three slots and the timing values the GUI's progress bars read. */
public class CookingLogicContainer {
    private final Supplier<CookingLogic> supplyCookingLogic;
    private final List<Slot> cookingSlots = new ArrayList<>();

    public CookingLogicContainer(Supplier<CookingLogic> supplyCookingLogic, Consumer<Slot> addSlot) {
        this.supplyCookingLogic = supplyCookingLogic;
        addCookingSlot(addSlot, new SlotSuppliedHandler(() -> supplyCookingLogic.get().getCookingInventory(), CookingLogic.COOK_INPUT_SLOT, -100, -100));
        addCookingSlot(addSlot, new SlotSuppliedHandler(() -> supplyCookingLogic.get().getCookingInventory(), CookingLogic.FUEL_SLOT, -100, -100));
        addCookingSlot(addSlot, new SlotSuppliedHandler(() -> supplyCookingLogic.get().getCookingInventory(), CookingLogic.COOK_OUTPUT_SLOT, -100, -100) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return false;
            }
        });
    }

    private void addCookingSlot(Consumer<Slot> addSlot, Slot slot) {
        addSlot.accept(slot);
        cookingSlots.add(slot);
    }

    public int getBurnTimeTotal() {
        return supplyCookingLogic.get().getBurnTimeTotal();
    }

    public long getBurnTimeFinish() {
        return supplyCookingLogic.get().getBurnTimeFinish();
    }

    public long getCookTimeFinish() {
        return supplyCookingLogic.get().getCookTimeFinish();
    }

    public int getCookTimeTotal() {
        return supplyCookingLogic.get().getCookTimeTotal();
    }

    public boolean isCooking() {
        return supplyCookingLogic.get().isCooking();
    }

    public boolean isBurning(World world) {
        return supplyCookingLogic.get().isBurning(world);
    }

    public List<Slot> getCookingSlots() {
        return cookingSlots;
    }
}
