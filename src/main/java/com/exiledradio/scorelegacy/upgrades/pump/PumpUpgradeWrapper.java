/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.pump;

import com.exiledradio.scorelegacy.config.PumpUpgradeConfig;
import com.exiledradio.scorelegacy.api.ITickableUpgrade;
import com.exiledradio.scorelegacy.inventory.StorageFluidHandler;
import com.exiledradio.scorelegacy.api.IStorageWrapper;
import com.exiledradio.scorelegacy.upgrades.UpgradeWrapperBase;
import com.exiledradio.scorelegacy.util.NBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Carried: fills or empties the fluid container in the player's hand. Placed: does the same for
 * players standing next to it, and the advanced pump also picks up or places fluid blocks
 * around it; failing that it pumps into or out of adjacent machines and tanks.
 */
public class PumpUpgradeWrapper extends UpgradeWrapperBase<PumpUpgradeWrapper, PumpUpgradeItem> implements ITickableUpgrade {
    private static final int DID_NOTHING_COOLDOWN_TIME = 40;
    private static final int HAND_INTERACTION_COOLDOWN_TIME = 3;
    private static final int WORLD_INTERACTION_COOLDOWN_TIME = 20;
    private static final int FLUID_HANDLER_INTERACTION_COOLDOWN_TIME = 20;
    private static final int PLAYER_SEARCH_RANGE = 3;
    private static final int PUMP_IN_WORLD_RANGE = 4;
    private static final int PUMP_IN_WORLD_RANGE_SQR = PUMP_IN_WORLD_RANGE * PUMP_IN_WORLD_RANGE;
    private long cooldownTime = 0;
    private long lastHandActionTime = -1;
    private final FluidFilterLogic fluidFilterLogic;

    public PumpUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        fluidFilterLogic = new FluidFilterLogic(upgradeItem.getConfig().filterSlots, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (cooldownTime > world.getTotalWorldTime()) {
            return;
        }
        StorageFluidHandler backpackFluidHandler = storageWrapper.getFluidHandler();
        cooldownTime = world.getTotalWorldTime() + (backpackFluidHandler == null ? DID_NOTHING_COOLDOWN_TIME : tick(backpackFluidHandler, entity, world, pos));
    }

    private int tick(IFluidHandler backpackFluidHandler, @Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (entity == null) {
            Integer newCooldown = handleInWorldInteractions(backpackFluidHandler, world, pos);
            if (newCooldown != null) {
                return newCooldown;
            }
        } else if (shouldInteractWithHand() && entity instanceof EntityPlayer && handleFluidContainerInHands((EntityPlayer) entity, backpackFluidHandler)) {
            lastHandActionTime = world.getTotalWorldTime();
            return HAND_INTERACTION_COOLDOWN_TIME;
        }
        // Stay quick for a while after a hand action, so filling several buckets in a row is smooth.
        return lastHandActionTime + 10 * HAND_INTERACTION_COOLDOWN_TIME > world.getTotalWorldTime() ? HAND_INTERACTION_COOLDOWN_TIME : DID_NOTHING_COOLDOWN_TIME;
    }

    @Nullable
    private Integer handleInWorldInteractions(IFluidHandler backpackFluidHandler, World world, BlockPos pos) {
        if (shouldInteractWithHand() && handleFluidContainersInHandsOfNearbyPlayers(world, pos, backpackFluidHandler)) {
            lastHandActionTime = world.getTotalWorldTime();
            return HAND_INTERACTION_COOLDOWN_TIME;
        }
        if (shouldInteractWithWorld()) {
            Integer newCooldown = interactWithWorld(world, pos, backpackFluidHandler);
            if (newCooldown != null) {
                return newCooldown;
            }
        }
        return interactWithAttachedFluidHandlers(world, pos, backpackFluidHandler);
    }

    @Nullable
    private Integer interactWithAttachedFluidHandlers(World world, BlockPos pos, IFluidHandler backpackFluidHandler) {
        for (EnumFacing dir : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(dir);
            if (!world.isBlockLoaded(neighbor)) {
                continue;
            }
            TileEntity tile = world.getTileEntity(neighbor);
            if (tile == null || !tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite())) {
                continue;
            }
            IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
            if (fluidHandler != null && (isInput() ? fillFromFluidHandler(fluidHandler, backpackFluidHandler, getMaxInOut())
                    : fillFluidHandler(fluidHandler, backpackFluidHandler, getMaxInOut()))) {
                return FLUID_HANDLER_INTERACTION_COOLDOWN_TIME;
            }
        }
        return null;
    }

    private int getMaxInOut() {
        return Math.max(Fluid.BUCKET_VOLUME, upgradeItem.getConfig().maxInputOutput * storageWrapper.getNumberOfSlotRows() * getAdjustedStackMultiplier(storageWrapper, upgradeItem.getConfig()));
    }

    public static int getAdjustedStackMultiplier(IStorageWrapper storageWrapper, PumpUpgradeConfig config) {
        return 1 + (int) (config.stackMultiplierRatio * (storageWrapper.getInventoryHandler().getStackSizeMultiplier() - 1));
    }

    @Nullable
    private Integer interactWithWorld(World world, BlockPos pos, IFluidHandler backpackFluidHandler) {
        if (isInput()) {
            return fillFromBlockInRange(world, pos, backpackFluidHandler);
        }
        for (EnumFacing dir : EnumFacing.values()) {
            if (dir != EnumFacing.UP && placeFluidInWorld(world, backpackFluidHandler, pos.offset(dir))) {
                return WORLD_INTERACTION_COOLDOWN_TIME;
            }
        }
        return null;
    }

    private boolean placeFluidInWorld(World world, IFluidHandler backpackFluidHandler, BlockPos offsetPos) {
        if (!world.isBlockLoaded(offsetPos) || !isValidForFluidPlacement(world, offsetPos)) {
            return false;
        }
        for (IFluidTankProperties tank : backpackFluidHandler.getTankProperties()) {
            FluidStack tankFluid = tank.getContents();
            if (tankFluid != null && tankFluid.amount >= Fluid.BUCKET_VOLUME && fluidFilterLogic.fluidMatches(tankFluid.getFluid())
                    && tankFluid.getFluid().canBePlacedInWorld()
                    && FluidUtil.tryPlaceFluid(null, world, offsetPos, backpackFluidHandler, new FluidStack(tankFluid, Fluid.BUCKET_VOLUME))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidForFluidPlacement(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isAir(state, world, pos)) {
            return true;
        }
        if (block instanceof BlockLiquid) {
            return state.getValue(BlockLiquid.LEVEL) != 0;
        }
        return block instanceof IFluidBlock && !((IFluidBlock) block).canDrain(world, pos);
    }

    /** The nearest source block within a small radius (breadth first), so the pump drains the closest first. */
    @Nullable
    private Integer fillFromBlockInRange(World world, BlockPos basePos, IFluidHandler backpackFluidHandler) {
        LinkedList<BlockPos> nextPositions = new LinkedList<>();
        Set<BlockPos> searchedPositions = new HashSet<>();
        nextPositions.add(basePos);
        searchedPositions.add(basePos);
        while (!nextPositions.isEmpty()) {
            BlockPos pos = nextPositions.poll();
            if (fillFromBlock(world, pos, backpackFluidHandler)) {
                return (int) (Math.max(1, Math.sqrt(basePos.distanceSq(pos))) * WORLD_INTERACTION_COOLDOWN_TIME);
            }
            for (EnumFacing dir : EnumFacing.values()) {
                BlockPos offsetPos = pos.offset(dir);
                if (searchedPositions.add(offsetPos) && basePos.distanceSq(offsetPos) < PUMP_IN_WORLD_RANGE_SQR) {
                    nextPositions.add(offsetPos);
                }
            }
        }
        return null;
    }

    private boolean fillFromBlock(World world, BlockPos pos, IFluidHandler backpackFluidHandler) {
        if (!world.isBlockLoaded(pos)) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        IFluidHandler targetFluidHandler;
        if (block instanceof IFluidBlock) {
            targetFluidHandler = new FluidBlockWrapper((IFluidBlock) block, world, pos);
        } else if (block instanceof BlockLiquid && state.getValue(BlockLiquid.LEVEL) == 0) {
            targetFluidHandler = new BlockLiquidWrapper((BlockLiquid) block, world, pos);
        } else {
            return false;
        }
        return fillFromFluidHandler(targetFluidHandler, backpackFluidHandler, Fluid.BUCKET_VOLUME);
    }

    private boolean handleFluidContainersInHandsOfNearbyPlayers(World world, BlockPos pos, IFluidHandler backpackFluidHandler) {
        AxisAlignedBB searchBox = new AxisAlignedBB(pos).grow(PLAYER_SEARCH_RANGE);
        for (EntityPlayer player : world.playerEntities) {
            if (searchBox.contains(player.getPositionVector()) && handleFluidContainerInHands(player, backpackFluidHandler)) {
                return true;
            }
        }
        return false;
    }

    private boolean handleFluidContainerInHands(EntityPlayer player, IFluidHandler backpackFluidHandler) {
        return handleFluidContainerInHand(backpackFluidHandler, player, EnumHand.MAIN_HAND) || handleFluidContainerInHand(backpackFluidHandler, player, EnumHand.OFF_HAND);
    }

    private boolean handleFluidContainerInHand(IFluidHandler backpackFluidHandler, EntityPlayer player, EnumHand hand) {
        ItemStack itemInHand = player.getHeldItem(hand);
        if (itemInHand.getCount() != 1 || itemInHand == storageWrapper.getWrappedStorageStack()
                || !itemInHand.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return false;
        }
        IFluidHandlerItem itemFluidHandler = itemInHand.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (itemFluidHandler == null) {
            return false;
        }
        boolean transferred = isInput() ? fillFromFluidHandler(itemFluidHandler, backpackFluidHandler, Fluid.BUCKET_VOLUME)
                : fillFluidHandler(itemFluidHandler, backpackFluidHandler, Fluid.BUCKET_VOLUME);
        if (transferred) {
            player.setHeldItem(hand, itemFluidHandler.getContainer());
        }
        return transferred;
    }

    private boolean fillFluidHandler(IFluidHandler fluidHandler, IFluidHandler backpackFluidHandler, int maxFill) {
        for (IFluidTankProperties tank : backpackFluidHandler.getTankProperties()) {
            FluidStack tankFluid = tank.getContents();
            if (tankFluid != null && fluidFilterLogic.fluidMatches(tankFluid.getFluid())
                    && FluidUtil.tryFluidTransfer(fluidHandler, backpackFluidHandler, new FluidStack(tankFluid, maxFill), true) != null) {
                return true;
            }
        }
        return false;
    }

    private boolean fillFromFluidHandler(IFluidHandler fluidHandler, IFluidHandler backpackFluidHandler, int maxDrain) {
        FluidStack containedFluid = fluidHandler.drain(maxDrain, false);
        return containedFluid != null && containedFluid.amount > 0 && fluidFilterLogic.fluidMatches(containedFluid.getFluid())
                && FluidUtil.tryFluidTransfer(backpackFluidHandler, fluidHandler, containedFluid, true) != null;
    }

    public void setIsInput(boolean input) {
        NBTHelper.setBoolean(upgrade, "input", input);
        save();
    }

    public boolean isInput() {
        return NBTHelper.getBoolean(upgrade, "input", true);
    }

    public FluidFilterLogic getFluidFilterLogic() {
        return fluidFilterLogic;
    }

    public void setInteractWithHand(boolean interactWithHand) {
        NBTHelper.setBoolean(upgrade, "interactWithHand", interactWithHand);
        save();
    }

    public boolean shouldInteractWithHand() {
        return NBTHelper.getBoolean(upgrade, "interactWithHand", upgradeItem.getInteractWithHandDefault());
    }

    public void setInteractWithWorld(boolean interactWithWorld) {
        NBTHelper.setBoolean(upgrade, "interactWithWorld", interactWithWorld);
        save();
    }

    public boolean shouldInteractWithWorld() {
        return NBTHelper.getBoolean(upgrade, "interactWithWorld", upgradeItem.getInteractWithWorldDefault());
    }
}
