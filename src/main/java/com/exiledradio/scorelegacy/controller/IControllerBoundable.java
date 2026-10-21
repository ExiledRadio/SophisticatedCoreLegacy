/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.controller;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Anything that can belong to a storage controller's network: a storage or a storage link (the original's IControllerBoundable). */
public interface IControllerBoundable {
    String CONTROLLER_POS_TAG = "controllerPos";

    void setControllerPos(BlockPos controllerPos);

    @Nullable
    BlockPos getControllerPos();

    void removeControllerPos();

    BlockPos getStorageBlockPos();

    World getStorageBlockWorld();

    /** Whether the network continues through this block to its neighbours. */
    boolean canConnectStorages();

    default void runOnController(Consumer<ControllerTileEntityBase> toRun) {
        ControllerTileEntityBase controller = getLoadedController(getStorageBlockWorld(), getControllerPos());
        if (controller != null) {
            toRun.accept(controller);
        }
    }

    default void saveControllerPos(NBTTagCompound tag) {
        BlockPos controllerPos = getControllerPos();
        if (controllerPos != null) {
            tag.setLong(CONTROLLER_POS_TAG, controllerPos.toLong());
        }
    }

    default void loadControllerPos(NBTTagCompound tag) {
        if (tag.hasKey(CONTROLLER_POS_TAG)) {
            setControllerPos(BlockPos.fromLong(tag.getLong(CONTROLLER_POS_TAG)));
        }
    }

    /** The controller at pos if its chunk is loaded, without loading it. */
    @Nullable
    static ControllerTileEntityBase getLoadedController(World world, @Nullable BlockPos pos) {
        if (pos == null || !world.isBlockLoaded(pos)) {
            return null;
        }
        TileEntity te = world.getTileEntity(pos);
        return te instanceof ControllerTileEntityBase ? (ControllerTileEntityBase) te : null;
    }
}
