/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.controller;

import com.exiledradio.scorelegacy.api.IStorageWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A storage a controller can use (the original's IControllableStorage). The original also
 * registers listeners on the storage's inventory and memory so the controller keeps an index;
 * this port's controller indexes lazily instead, so joining and leaving is all there is.
 */
public interface IControllableStorage extends IControllerBoundable {

    IStorageWrapper getStorageWrapper();

    /**
     * Whether the controller uses this storage's contents. False for a part of a bigger storage
     * that keeps none itself (one half of a double chest): it still joins the network, so storages
     * beyond it connect, but its contents are reached through the other part.
     */
    default boolean hasStorageData() {
        return true;
    }

    default boolean canBeConnected() {
        return getControllerPos() == null;
    }

    @Override
    default boolean canConnectStorages() {
        return true;
    }

    /** Joins the network of a neighbouring controller or of a neighbour that belongs to one. */
    default void tryToAddToController() {
        World world = getStorageBlockWorld();
        if (world.isRemote) {
            return;
        }
        BlockPos pos = getStorageBlockPos();
        for (EnumFacing direction : EnumFacing.values()) {
            BlockPos offsetPos = pos.offset(direction);
            if (!world.isBlockLoaded(offsetPos)) {
                continue;
            }
            TileEntity te = world.getTileEntity(offsetPos);
            if (te instanceof IControllerBoundable) {
                IControllerBoundable boundable = (IControllerBoundable) te;
                if (boundable.canConnectStorages() && boundable.getControllerPos() != null) {
                    addToController(world, pos, boundable.getControllerPos());
                }
            } else if (te instanceof ControllerTileEntityBase) {
                addToController(world, pos, offsetPos);
            }
            if (getControllerPos() != null) {
                break;
            }
        }
    }

    static void addToController(World world, BlockPos pos, BlockPos controllerPos) {
        ControllerTileEntityBase controller = IControllerBoundable.getLoadedController(world, controllerPos);
        if (controller != null) {
            controller.addStorage(pos);
        }
    }

    default void removeFromController() {
        World world = getStorageBlockWorld();
        if (!world.isRemote && getControllerPos() != null) {
            runOnController(controller -> controller.removeStorage(getStorageBlockPos()));
            removeControllerPos();
        }
    }

    default void registerController(ControllerTileEntityBase controller) {
        setControllerPos(controller.getPos());
    }

    default void unregisterController() {
        removeControllerPos();
    }

    /**
     * When the storage loads: if its controller is loaded and no longer lists it (the network
     * changed while it was unloaded), it forgets that controller and looks for one next to it.
     */
    default void registerWithControllerOnLoad() {
        World world = getStorageBlockWorld();
        BlockPos controllerPos = getControllerPos();
        if (world.isRemote || controllerPos == null) {
            return;
        }
        ControllerTileEntityBase controller = IControllerBoundable.getLoadedController(world, controllerPos);
        if (controller != null && !controller.isStorageConnected(getStorageBlockPos())) {
            removeControllerPos();
            tryToAddToController();
        }
    }
}
