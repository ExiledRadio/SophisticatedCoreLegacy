/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.feeding;

import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.FilterLogic;
import com.exiledradio.scorelegacy.upgrades.IFilteredUpgrade;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.InventoryHelper;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class FeedingUpgradeWrapper extends UpgradeWrapperBase<FeedingUpgradeWrapper, FeedingUpgradeItem> implements ITickableUpgrade, IFilteredUpgrade {
    private static final int COOLDOWN = 100;
    private static final int STILL_HUNGRY_COOLDOWN = 10;
    private static final int FEEDING_RANGE = 3;
    private final FilterLogic filterLogic;

    public FeedingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount(), stack -> stack.getItem() instanceof ItemFood);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (isInCooldown(world) || (entity != null && !(entity instanceof EntityPlayer))) {
            return;
        }
        if (entity == null) {
            for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos).grow(FEEDING_RANGE))) {
                feedPlayerAndGetHungry(player, world);
            }
        } else if (feedPlayerAndGetHungry((EntityPlayer) entity, world)) {
            setCooldown(world, STILL_HUNGRY_COOLDOWN);
            return;
        }
        setCooldown(world, COOLDOWN);
    }

    private boolean feedPlayerAndGetHungry(EntityPlayer player, World world) {
        int hungerLevel = 20 - player.getFoodStats().getFoodLevel();
        if (hungerLevel == 0) {
            return false;
        }
        return tryFeedingFoodFromBackpack(world, hungerLevel, player) && player.getFoodStats().getFoodLevel() < 20;
    }

    private boolean tryFeedingFoodFromBackpack(World world, int hungerLevel, EntityPlayer player) {
        boolean isHurt = player.getHealth() < player.getMaxHealth() - 0.1F;
        IItemHandlerModifiable inventory = storageWrapper.getInventoryForUpgradeProcessing();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!(stack.getItem() instanceof ItemFood) || !filterLogic.matchesFilter(stack)) {
                continue;
            }
            if (!isHungryEnoughForFood(hungerLevel, stack) && !(shouldFeedImmediatelyWhenHurt() && isHurt)) {
                continue;
            }
            if (eat(world, player, inventory, slot, stack)) {
                return true;
            }
        }
        return false;
    }

    /** Eats one from the slot exactly as if the player ate it from their hand (events included). */
    private boolean eat(World world, EntityPlayer player, IItemHandlerModifiable inventory, int slot, ItemStack stack) {
        int selected = player.inventory.currentItem;
        ItemStack mainHandItem = player.inventory.mainInventory.get(selected);
        player.inventory.mainInventory.set(selected, stack);
        ActionResult<ItemStack> canEat = stack.getItem().onItemRightClick(world, player, EnumHand.MAIN_HAND);
        player.resetActiveHand();
        player.inventory.mainInventory.set(selected, mainHandItem);
        if (canEat.getType() != EnumActionResult.SUCCESS) {
            return false;
        }
        ItemStack before = stack.copy();
        ItemStack containerItem = stack.getItem().onItemUseFinish(stack, world, player);
        containerItem = ForgeEventFactory.onItemUseFinish(player, before, 0, containerItem);
        inventory.setStackInSlot(slot, stack);
        if (containerItem != stack && !containerItem.isEmpty()) {
            InventoryHelper.insertOrDropItem(player, containerItem, inventory, new PlayerMainInvWrapper(player.inventory));
        }
        return true;
    }

    private boolean isHungryEnoughForFood(int hungerLevel, ItemStack stack) {
        HungerLevel feedAtHungerLevel = getFeedAtHungerLevel();
        if (feedAtHungerLevel == HungerLevel.ANY) {
            return true;
        }
        int nutrition = ((ItemFood) stack.getItem()).getHealAmount(stack);
        return (feedAtHungerLevel == HungerLevel.HALF ? nutrition / 2 : nutrition) <= hungerLevel;
    }

    @Override
    public FilterLogic getFilterLogic() {
        return filterLogic;
    }

    public HungerLevel getFeedAtHungerLevel() {
        return NBTHelper.getEnum(upgrade, "feedAtHungerLevel", HungerLevel::fromName, HungerLevel.HALF);
    }

    public void setFeedAtHungerLevel(HungerLevel hungerLevel) {
        NBTHelper.setEnum(upgrade, "feedAtHungerLevel", hungerLevel);
        save();
    }

    public boolean shouldFeedImmediatelyWhenHurt() {
        return NBTHelper.getBoolean(upgrade, "feedImmediatelyWhenHurt", true);
    }

    public void setFeedImmediatelyWhenHurt(boolean feedImmediatelyWhenHurt) {
        NBTHelper.setBoolean(upgrade, "feedImmediatelyWhenHurt", feedImmediatelyWhenHurt);
        save();
    }
}
