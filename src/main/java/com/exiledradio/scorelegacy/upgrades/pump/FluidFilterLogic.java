/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.pump;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Consumer;

/** Which fluids the advanced pump moves; with no filter set it moves any fluid. */
public class FluidFilterLogic {
    private static final String FLUIDS_TAG = "fluids";
    private final Fluid[] fluidFilters;
    private final ItemStack upgrade;
    private final Consumer<ItemStack> saveHandler;
    private boolean noFilter = true;

    public FluidFilterLogic(int filterSlots, ItemStack upgrade, Consumer<ItemStack> saveHandler) {
        fluidFilters = new Fluid[filterSlots];
        this.upgrade = upgrade;
        this.saveHandler = saveHandler;
        NBTTagCompound tag = upgrade.getTagCompound();
        if (tag != null && tag.hasKey(FLUIDS_TAG)) {
            NBTTagList list = tag.getTagList(FLUIDS_TAG, Constants.NBT.TAG_STRING);
            for (int i = 0; i < list.tagCount() && i < fluidFilters.length; i++) {
                fluidFilters[i] = FluidRegistry.getFluid(list.getStringTagAt(i));
            }
        }
        updateNoFilter();
    }

    private void updateNoFilter() {
        noFilter = Arrays.stream(fluidFilters).allMatch(fluid -> fluid == null);
    }

    public boolean fluidMatches(@Nullable Fluid fluid) {
        if (noFilter) {
            return true;
        }
        for (Fluid fluidFilter : fluidFilters) {
            if (fluidFilter != null && fluidFilter == fluid) {
                return true;
            }
        }
        return false;
    }

    public void setFluid(int index, @Nullable Fluid fluid) {
        if (index < 0 || index >= fluidFilters.length) {
            return;
        }
        fluidFilters[index] = fluid;
        NBTTagList list = new NBTTagList();
        for (Fluid fluidFilter : fluidFilters) {
            list.appendTag(new NBTTagString(fluidFilter == null ? "" : fluidFilter.getName()));
        }
        upgrade.setTagInfo(FLUIDS_TAG, list);
        updateNoFilter();
        saveHandler.accept(upgrade);
    }

    @Nullable
    public Fluid getFluid(int index) {
        return fluidFilters[index];
    }

    public int getNumberOfFluidFilters() {
        return fluidFilters.length;
    }
}
