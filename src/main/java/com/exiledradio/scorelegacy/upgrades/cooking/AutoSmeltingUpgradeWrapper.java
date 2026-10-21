/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.cooking;

import com.exiledradio.scorelegacy.config.AutoCookingUpgradeConfig;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import com.exiledradio.scorelegacy.util.InventoryHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** A furnace that feeds itself from the backpack and puts the results back into it. */
public class AutoSmeltingUpgradeWrapper extends UpgradeWrapperBase<AutoSmeltingUpgradeWrapper, AutoSmeltingUpgradeItem> implements ITickableUpgrade, ICookingUpgrade {
    private static final int NOTHING_TO_DO_COOLDOWN = 10;
    private static final int NO_INVENTORY_SPACE_COOLDOWN = 60;
    private final FilterLogic inputFilterLogic;
    private final FilterLogic fuelFilterLogic;
    private final CookingLogic cookingLogic;
    private final Predicate<ItemStack> isValidInput;
    private final Predicate<ItemStack> isValidFuel;
    private int outputCooldown = 0;
    private int fuelCooldown = 0;
    private int inputCooldown = 0;

    public AutoSmeltingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        AutoCookingUpgradeConfig config = upgradeItem.getConfig();
        inputFilterLogic = new FilterLogic(upgrade, upgradeSaveHandler, config.inputFilterSlots,
                s -> !CookingLogic.getSmeltingResult(s).isEmpty(), "inputFilter");
        fuelFilterLogic = new FilterLogic(upgrade, upgradeSaveHandler, config.fuelFilterSlots,
                s -> CookingLogic.getBurnTime(s) > 0, "fuelFilter");
        fuelFilterLogic.setAllowByDefault();
        fuelFilterLogic.setEmptyAllowListMatchesEverything();
        isValidInput = s -> !CookingLogic.getSmeltingResult(s).isEmpty() && inputFilterLogic.matchesFilter(s);
        isValidFuel = s -> CookingLogic.getBurnTime(s) > 0 && fuelFilterLogic.matchesFilter(s);
        cookingLogic = new CookingLogic(upgrade, upgradeSaveHandler, isValidFuel, isValidInput, config.cookingSpeedMultiplier, config.fuelEfficiencyMultiplier);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (isInCooldown(world)) {
            return;
        }
        tryPushingOutput();
        tryPullingFuel();
        tryPullingInput();
        if (!cookingLogic.tick(world) && outputCooldown <= 0 && fuelCooldown <= 0 && inputCooldown <= 0) {
            setCooldown(world, NOTHING_TO_DO_COOLDOWN);
        }
    }

    private void tryPushingOutput() {
        if (outputCooldown > 0) {
            outputCooldown--;
            return;
        }
        IItemHandlerSimpleInserter inventory = storageWrapper.getInventoryForUpgradeProcessing();
        ItemStack output = cookingLogic.getCookOutput();
        if (!output.isEmpty() && inventory.insertItem(output, true).getCount() < output.getCount()) {
            ItemStack remaining = inventory.insertItem(output.copy(), false);
            cookingLogic.getCookingInventory().extractItem(CookingLogic.COOK_OUTPUT_SLOT, output.getCount() - remaining.getCount(), false);
        } else {
            outputCooldown = NO_INVENTORY_SPACE_COOLDOWN;
        }
        // What's left in the fuel slot after burning (an empty bucket) goes back too.
        ItemStack fuel = cookingLogic.getFuel();
        if (!fuel.isEmpty() && CookingLogic.getBurnTime(fuel) <= 0 && inventory.insertItem(fuel, true).getCount() < fuel.getCount()) {
            ItemStack remaining = inventory.insertItem(fuel.copy(), false);
            cookingLogic.getCookingInventory().extractItem(CookingLogic.FUEL_SLOT, fuel.getCount() - remaining.getCount(), false);
        }
    }

    private void tryPullingInput() {
        if (inputCooldown > 0) {
            inputCooldown--;
            return;
        }
        if (tryPullingGetUnsuccessful(cookingLogic.getCookInput(), cookingLogic::setCookInput, isValidInput)) {
            inputCooldown = NO_INVENTORY_SPACE_COOLDOWN;
        }
    }

    private void tryPullingFuel() {
        if (fuelCooldown > 0) {
            fuelCooldown--;
            return;
        }
        if (tryPullingGetUnsuccessful(cookingLogic.getFuel(), cookingLogic::setFuel, isValidFuel)) {
            fuelCooldown = NO_INVENTORY_SPACE_COOLDOWN;
        }
    }

    /** Tops the slot up to a full stack from the backpack; true when nothing could be pulled. */
    private boolean tryPullingGetUnsuccessful(ItemStack stack, Consumer<ItemStack> setSlot, Predicate<ItemStack> isItemValid) {
        IItemHandler inventory = storageWrapper.getInventoryForUpgradeProcessing();
        ItemStack toExtract;
        if (stack.isEmpty()) {
            toExtract = ItemStack.EMPTY;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack candidate = inventory.getStackInSlot(slot);
                if (!candidate.isEmpty() && isItemValid.test(candidate)) {
                    toExtract = candidate.copy();
                    break;
                }
            }
            if (toExtract.isEmpty()) {
                return true;
            }
            toExtract.setCount(toExtract.getMaxStackSize());
        } else if (stack.getCount() == stack.getMaxStackSize() || !isItemValid.test(stack)) {
            return true;
        } else {
            toExtract = stack.copy();
            toExtract.setCount(stack.getMaxStackSize() - stack.getCount());
        }
        if (InventoryHelper.extractFromInventory(toExtract, inventory, true).isEmpty()) {
            return true;
        }
        ItemStack toSet = InventoryHelper.extractFromInventory(toExtract, inventory, false);
        toSet.grow(stack.getCount());
        setSlot.accept(toSet);
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            cookingLogic.pause();
        }
        super.setEnabled(enabled);
    }

    @Override
    public void onBeforeRemoved() {
        cookingLogic.pause();
    }

    @Override
    public CookingLogic getCookingLogic() {
        return cookingLogic;
    }

    public FilterLogic getInputFilterLogic() {
        return inputFilterLogic;
    }

    public FilterLogic getFuelFilterLogic() {
        return fuelFilterLogic;
    }
}
