/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.magnet;

import com.exiledradio.scorelegacy.api.IPickupResponseUpgrade;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.ContentsFilterLogic;
import com.exiledradio.scorelegacy.upgrades.IContentsFilteredUpgrade;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.IItemHandlerSimpleInserter;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import com.exiledradio.scorelegacy.inventory.StorageFluidHandler;
import com.exiledradio.scorelegacy.util.XpHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/** Pulls matching items lying nearby into the backpack. */
public class MagnetUpgradeWrapper extends UpgradeWrapperBase<MagnetUpgradeWrapper, MagnetUpgradeItem>
        implements IContentsFilteredUpgrade, ITickableUpgrade, IPickupResponseUpgrade {
    // Entity data keys other mods (Demagnetize, item conveyors) use to keep magnets off their items.
    private static final String PREVENT_REMOTE_MOVEMENT = "PreventRemoteMovement";
    private static final String ALLOW_MACHINE_MOVEMENT = "AllowMachineRemoteMovement";
    private static final int COOLDOWN_TICKS = 10;
    private static final int FULL_COOLDOWN_TICKS = 40;

    private final ContentsFilterLogic filterLogic;

    public MagnetUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new ContentsFilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount(), storageWrapper::getInventoryHandler);
    }

    @Override
    public ContentsFilterLogic getFilterLogic() {
        return filterLogic;
    }

    @Override
    public ItemStack pickup(World world, ItemStack stack, boolean simulate) {
        if (!shouldPickupItems() || !filterLogic.matchesFilter(stack)) {
            return stack;
        }
        return storageWrapper.getInventoryForUpgradeProcessing().insertItem(stack, simulate);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (isInCooldown(world)) {
            return;
        }
        int cooldown = shouldPickupItems() ? pickupItems(entity, world, pos) : FULL_COOLDOWN_TICKS;
        StorageFluidHandler fluidHandler = storageWrapper.getFluidHandler();
        if (shouldPickupXp() && fluidHandler != null && fluidHandler.fillExperience(XpHelper.experienceToLiquid(1), false, true) > 0) {
            cooldown = Math.min(cooldown, pickupXpOrbs(fluidHandler, entity, world, pos));
        }
        setCooldown(world, cooldown);
    }

    /** Experience orbs go into a tank as liquid experience; whatever doesn't fit drops back as an orb. */
    private int pickupXpOrbs(StorageFluidHandler fluidHandler, @Nullable EntityLivingBase entity, World world, BlockPos pos) {
        List<EntityXPOrb> orbs = world.getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB(pos).grow(upgradeItem.getRadius()));
        int cooldown = COOLDOWN_TICKS;
        for (EntityXPOrb orb : orbs) {
            if (orb.isDead || canNotPickup(orb, entity)) {
                continue;
            }
            int amountToTransfer = XpHelper.experienceToLiquid(orb.getXpValue());
            // Whole experience points only, so the conversion never creates or loses experience.
            int points = (int) XpHelper.liquidToExperience(fluidHandler.fillExperience(amountToTransfer, false, true));
            if (points <= 0) {
                cooldown = FULL_COOLDOWN_TICKS;
                break;
            }
            fluidHandler.fillExperience(XpHelper.experienceToLiquid(points), true, true);
            int leftOver = orb.getXpValue() - points;
            orb.setDead();
            if (leftOver > 0) {
                world.spawnEntity(new EntityXPOrb(world, orb.posX, orb.posY, orb.posZ, leftOver));
            }
            if (entity instanceof EntityPlayer) {
                world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F,
                        0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.8F));
            }
        }
        return cooldown;
    }

    private int pickupItems(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        List<EntityItem> itemEntities = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos).grow(upgradeItem.getRadius()));
        if (itemEntities.isEmpty()) {
            return COOLDOWN_TICKS;
        }
        int cooldown = COOLDOWN_TICKS;
        for (EntityItem itemEntity : itemEntities) {
            if (itemEntity.isDead || !filterLogic.matchesFilter(itemEntity.getItem()) || canNotPickup(itemEntity, entity)) {
                continue;
            }
            if (tryToInsertItem(itemEntity)) {
                if (entity instanceof EntityPlayer) {
                    world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                            (world.rand.nextFloat() - world.rand.nextFloat()) * 1.4F + 2.0F);
                }
            } else {
                cooldown = FULL_COOLDOWN_TICKS;
            }
        }
        return cooldown;
    }

    private static boolean canNotPickup(Entity entity, @Nullable EntityLivingBase carrier) {
        NBTTagCompound data = entity.getEntityData();
        return carrier != null ? data.hasKey(PREVENT_REMOTE_MOVEMENT) : data.hasKey(PREVENT_REMOTE_MOVEMENT) && !data.hasKey(ALLOW_MACHINE_MOVEMENT);
    }

    private boolean tryToInsertItem(EntityItem itemEntity) {
        ItemStack stack = itemEntity.getItem();
        IItemHandlerSimpleInserter inventory = storageWrapper.getInventoryForUpgradeProcessing();
        ItemStack remaining = inventory.insertItem(stack, true);
        if (remaining.getCount() == stack.getCount()) {
            return false;
        }
        remaining = inventory.insertItem(stack.copy(), false);
        if (remaining.isEmpty()) {
            itemEntity.setDead();
        } else {
            itemEntity.setItem(remaining);
        }
        return true;
    }

    public void setPickupItems(boolean pickupItems) {
        NBTHelper.setBoolean(upgrade, "pickupItems", pickupItems);
        save();
    }

    public boolean shouldPickupItems() {
        return NBTHelper.getBoolean(upgrade, "pickupItems", true);
    }

    public void setPickupXp(boolean pickupXp) {
        NBTHelper.setBoolean(upgrade, "pickupXp", pickupXp);
        save();
    }

    public boolean shouldPickupXp() {
        return NBTHelper.getBoolean(upgrade, "pickupXp", true);
    }
}
