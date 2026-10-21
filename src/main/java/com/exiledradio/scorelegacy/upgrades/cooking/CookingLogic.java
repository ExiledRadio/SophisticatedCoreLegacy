/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.cooking;

import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A furnace kept in the upgrade stack. Times are stored as world-time finish points rather
 * than counters, so a backpack that isn't ticking (in a chest, unloaded) doesn't need
 * catching up; pause() turns them back into durations when the upgrade is disabled.
 * 1.12 furnace recipes all take 200 ticks, the same as TileEntityFurnace; the smoking and
 * blasting types take half that and get half a fuel's burn time (see {@link CookingType}).
 */
public class CookingLogic {
    public static final int COOK_INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int COOK_OUTPUT_SLOT = 2;
    private static final String COOKING_INVENTORY_TAG = "cookingInventory";

    private final ItemStack upgrade;
    private final Consumer<ItemStack> saveHandler;
    private final Predicate<ItemStack> isFuel;
    private final Predicate<ItemStack> isInput;
    private final double cookingSpeedMultiplier;
    private final double fuelEfficiencyMultiplier;
    private final CookingType cookingType;
    private ItemStackHandler cookingInventory = null;
    private ItemStack cookingResult = ItemStack.EMPTY;
    private boolean cookingResultInitialized = false;
    private boolean paused = false;
    private long remainingCookTime = 0;
    private long remainingBurnTime = 0;

    public CookingLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, double cookingSpeedMultiplier, double fuelEfficiencyMultiplier) {
        this(upgrade, saveHandler, CookingType.SMELTING, cookingSpeedMultiplier, fuelEfficiencyMultiplier);
    }

    public CookingLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, CookingType cookingType, double cookingSpeedMultiplier, double fuelEfficiencyMultiplier) {
        this(upgrade, saveHandler, s -> getBurnTime(s) > 0, s -> !cookingType.getResult(s).isEmpty(), cookingType, cookingSpeedMultiplier, fuelEfficiencyMultiplier);
    }

    public CookingLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, Predicate<ItemStack> isFuel, Predicate<ItemStack> isInput,
                        double cookingSpeedMultiplier, double fuelEfficiencyMultiplier) {
        this(upgrade, saveHandler, isFuel, isInput, CookingType.SMELTING, cookingSpeedMultiplier, fuelEfficiencyMultiplier);
    }

    public CookingLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, Predicate<ItemStack> isFuel, Predicate<ItemStack> isInput,
                        CookingType cookingType, double cookingSpeedMultiplier, double fuelEfficiencyMultiplier) {
        this.cookingType = cookingType;
        this.upgrade = upgrade;
        this.saveHandler = saveHandler;
        this.isFuel = isFuel;
        this.isInput = isInput;
        this.cookingSpeedMultiplier = cookingSpeedMultiplier;
        this.fuelEfficiencyMultiplier = fuelEfficiencyMultiplier;
    }

    public CookingType getCookingType() {
        return cookingType;
    }

    public static ItemStack getSmeltingResult(ItemStack input) {
        return input.isEmpty() ? ItemStack.EMPTY : FurnaceRecipes.instance().getSmeltingResult(input);
    }

    public static int getBurnTime(ItemStack fuel) {
        return fuel.isEmpty() ? 0 : TileEntityFurnace.getItemBurnTime(fuel);
    }

    private void save() {
        saveHandler.accept(upgrade);
    }

    /** Returns false when there was nothing to do, so the caller can back off for a while. */
    public boolean tick(World world) {
        updateTimes(world);
        boolean didSomething = true;
        if (isBurning(world) || readyToStartCooking()) {
            ItemStack result = getCookingResult();
            if (result.isEmpty() && isCooking()) {
                setIsCooking(false);
            }
            if (!result.isEmpty()) {
                updateFuel(world, result);
                if (isBurning(world) && canSmelt(result)) {
                    updateCookingProgress(world, result);
                } else if (!isBurning(world)) {
                    didSomething = false;
                }
            }
        }
        if (!isBurning(world) && isCooking()) {
            updateCookingCooldown(world);
        } else {
            didSomething = false;
        }
        return didSomething;
    }

    private void updateTimes(World world) {
        if (paused) {
            unpause(world);
            return;
        }
        remainingBurnTime = isBurning(world) ? getBurnTimeFinish() - world.getTotalWorldTime() : 0;
        remainingCookTime = isCooking() ? getCookTimeFinish() - world.getTotalWorldTime() : 0;
    }

    private void unpause(World world) {
        paused = false;
        if (remainingBurnTime > 0) {
            setBurnTimeFinish(world.getTotalWorldTime() + remainingBurnTime);
        }
        if (remainingCookTime > 0) {
            setCookTimeFinish(world.getTotalWorldTime() + remainingCookTime);
            setIsCooking(true);
        }
    }

    public boolean isBurning(World world) {
        return getBurnTimeFinish() >= world.getTotalWorldTime();
    }

    private ItemStack getCookingResult() {
        if (!cookingResultInitialized) {
            cookingResult = cookingType.getResult(getCookInput());
            cookingResultInitialized = true;
        }
        return cookingResult;
    }

    /** Without fuel the progress runs back down, as in a vanilla furnace. */
    private void updateCookingCooldown(World world) {
        if (getRemainingCookTime(world) + 2 > getCookTimeTotal()) {
            setIsCooking(false);
        } else {
            setCookTimeFinish(world.getTotalWorldTime() + Math.min(getRemainingCookTime(world) + 2, getCookTimeTotal()));
        }
    }

    private void updateCookingProgress(World world, ItemStack result) {
        if (isCooking() && finishedCooking(world)) {
            smelt(result);
            if (canSmelt(result)) {
                setCookTime(world, getCookTime());
            } else {
                setIsCooking(false);
            }
        } else if (!isCooking()) {
            setIsCooking(true);
            setCookTime(world, getCookTime());
        }
    }

    private int getCookTime() {
        return (int) (cookingType.getCookTime() * (1 / cookingSpeedMultiplier));
    }

    private boolean finishedCooking(World world) {
        return getCookTimeFinish() <= world.getTotalWorldTime();
    }

    private boolean readyToStartCooking() {
        return !getFuel().isEmpty() && !getCookInput().isEmpty();
    }

    private void smelt(ItemStack result) {
        if (!canSmelt(result)) {
            return;
        }
        ItemStack input = getCookInput().copy();
        ItemStack output = getCookOutput();
        if (output.isEmpty()) {
            setCookOutput(result.copy());
        } else if (output.isItemEqual(result)) {
            ItemStack grown = output.copy();
            grown.grow(result.getCount());
            setCookOutput(grown);
        }
        // A wet sponge dries out and fills an empty bucket in the fuel slot, as in vanilla.
        if (input.getItem() == Item.getItemFromBlock(Blocks.SPONGE) && input.getMetadata() == 1
                && getFuel().getItem() == Items.BUCKET) {
            setFuel(new ItemStack(Items.WATER_BUCKET));
        }
        input.shrink(1);
        setCookInput(input);
    }

    private boolean canSmelt(ItemStack result) {
        if (getCookInput().isEmpty() || result.isEmpty()) {
            return false;
        }
        ItemStack output = getCookOutput();
        if (output.isEmpty()) {
            return true;
        }
        if (!output.isItemEqual(result)) {
            return false;
        }
        int resultCount = output.getCount() + result.getCount();
        return resultCount <= 64 && resultCount <= output.getMaxStackSize();
    }

    private void updateFuel(World world, ItemStack result) {
        ItemStack fuel = getFuel();
        if (isBurning(world) || !canSmelt(result)) {
            return;
        }
        int burnTime = (int) (getBurnTime(fuel) * cookingType.getBurnTimeModifier());
        if (burnTime <= 0) {
            return;
        }
        setBurnTime(world, (int) (burnTime * fuelEfficiencyMultiplier / cookingSpeedMultiplier));
        if (isBurning(world)) {
            Item fuelItem = fuel.getItem();
            if (fuelItem.hasContainerItem(fuel) && fuel.getCount() == 1) {
                setFuel(fuelItem.getContainerItem(fuel));
            } else {
                ItemStack remaining = fuel.copy();
                remaining.shrink(1);
                setFuel(remaining);
            }
        }
    }

    public void pause() {
        paused = true;
        setCookTimeFinish(0);
        setIsCooking(false);
        setBurnTimeFinish(0);
    }

    private void setBurnTime(World world, int burnTime) {
        setBurnTimeFinish(world.getTotalWorldTime() + burnTime);
        setBurnTimeTotal(burnTime);
    }

    private void setCookTime(World world, int cookTime) {
        setCookTimeFinish(world.getTotalWorldTime() + cookTime);
        setCookTimeTotal(cookTime);
    }

    private int getRemainingCookTime(World world) {
        return (int) (getCookTimeFinish() - world.getTotalWorldTime());
    }

    public ItemStack getCookOutput() {
        return getCookingInventory().getStackInSlot(COOK_OUTPUT_SLOT);
    }

    public ItemStack getCookInput() {
        return getCookingInventory().getStackInSlot(COOK_INPUT_SLOT);
    }

    public void setCookInput(ItemStack input) {
        getCookingInventory().setStackInSlot(COOK_INPUT_SLOT, input);
    }

    private void setCookOutput(ItemStack stack) {
        getCookingInventory().setStackInSlot(COOK_OUTPUT_SLOT, stack);
    }

    public ItemStack getFuel() {
        return getCookingInventory().getStackInSlot(FUEL_SLOT);
    }

    public void setFuel(ItemStack fuel) {
        getCookingInventory().setStackInSlot(FUEL_SLOT, fuel);
    }

    public ItemStackHandler getCookingInventory() {
        if (cookingInventory == null) {
            cookingInventory = new ItemStackHandler(3) {
                @Override
                protected void onContentsChanged(int slot) {
                    super.onContentsChanged(slot);
                    upgrade.setTagInfo(COOKING_INVENTORY_TAG, serializeNBT());
                    save();
                    if (slot == COOK_INPUT_SLOT) {
                        cookingResultInitialized = false;
                    }
                }

                @Override
                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    switch (slot) {
                        case COOK_INPUT_SLOT:
                            return isInput.test(stack);
                        case FUEL_SLOT:
                            return isFuel.test(stack);
                        default:
                            return true;
                    }
                }
            };
            NBTTagCompound tag = upgrade.getTagCompound();
            if (tag != null && tag.hasKey(COOKING_INVENTORY_TAG)) {
                cookingInventory.deserializeNBT(tag.getCompoundTag(COOKING_INVENTORY_TAG));
            }
        }
        return cookingInventory;
    }

    public long getBurnTimeFinish() {
        return NBTHelper.getLong(upgrade, "burnTimeFinish", 0L);
    }

    private void setBurnTimeFinish(long burnTimeFinish) {
        NBTHelper.setLong(upgrade, "burnTimeFinish", burnTimeFinish);
        save();
    }

    public int getBurnTimeTotal() {
        return NBTHelper.getInt(upgrade, "burnTimeTotal", 0);
    }

    private void setBurnTimeTotal(int burnTimeTotal) {
        NBTHelper.setInt(upgrade, "burnTimeTotal", burnTimeTotal);
        save();
    }

    public long getCookTimeFinish() {
        return NBTHelper.getLong(upgrade, "cookTimeFinish", -1L);
    }

    private void setCookTimeFinish(long cookTimeFinish) {
        NBTHelper.setLong(upgrade, "cookTimeFinish", cookTimeFinish);
        save();
    }

    public int getCookTimeTotal() {
        return NBTHelper.getInt(upgrade, "cookTimeTotal", 0);
    }

    private void setCookTimeTotal(int cookTimeTotal) {
        NBTHelper.setInt(upgrade, "cookTimeTotal", cookTimeTotal);
        save();
    }

    public boolean isCooking() {
        return NBTHelper.getBoolean(upgrade, "isCooking", false);
    }

    private void setIsCooking(boolean isCooking) {
        NBTHelper.setBoolean(upgrade, "isCooking", isCooking);
        save();
    }
}
