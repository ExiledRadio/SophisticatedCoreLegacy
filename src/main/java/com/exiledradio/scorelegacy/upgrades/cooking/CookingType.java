/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.cooking;

import com.exiledradio.scorelegacy.compat.BackportRecipes;
import com.exiledradio.scorelegacy.compat.MaterialCompat;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

/**
 * What a cooking upgrade cooks, as the original's recipe types: smelting takes every furnace
 * recipe; smoking and blasting (from a mod's smoker or blast furnace, see {@link MaterialCompat})
 * cook in half the time and burn fuel twice as fast, so a piece of fuel still does as many items.
 *
 * <p>1.12 has no smoking or blasting recipes, so they are read from the furnace's, by the rules
 * VillagerBackport's smoker and blast furnace use: a smoker cooks whatever smelts into food, a
 * blast furnace whatever is an ore or smelts into an ingot or gem. Future MC's own smoker and
 * blast furnace lists are added on top.
 */
public enum CookingType {
    SMELTING("smelting", 200, 1.0F) {
        @Override
        public ItemStack getResult(ItemStack input) {
            return getFurnaceResult(input);
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    },
    SMOKING("smoking", 100, 0.5F) {
        @Override
        public ItemStack getResult(ItemStack input) {
            ItemStack result = getFurnaceResult(input);
            if (result.getItem() instanceof ItemFood) {
                return result;
            }
            return BackportRecipes.getFutureMcSmokerResult(input);
        }

        @Override
        public boolean isAvailable() {
            return MaterialCompat.Material.SMOKER.isAvailable();
        }
    },
    BLASTING("blasting", 100, 0.5F) {
        @Override
        public ItemStack getResult(ItemStack input) {
            ItemStack result = getFurnaceResult(input);
            if (!result.isEmpty() && (hasOrePrefix(input, "ore") || hasOrePrefix(result, "ingot") || hasOrePrefix(result, "gem"))) {
                return result;
            }
            return BackportRecipes.getFutureMcBlastFurnaceResult(input);
        }

        @Override
        public boolean isAvailable() {
            return MaterialCompat.Material.BLAST_FURNACE.isAvailable();
        }
    };

    private final String name;
    private final int cookTime;
    private final float burnTimeModifier;

    CookingType(String name, int cookTime, float burnTimeModifier) {
        this.name = name;
        this.cookTime = cookTime;
        this.burnTimeModifier = burnTimeModifier;
    }

    /** What one input item cooks into, or empty if this type doesn't cook it. */
    public abstract ItemStack getResult(ItemStack input);

    /** Whether an installed mod adds the block this type stands for. */
    public abstract boolean isAvailable();

    /** The upgrade tab's name: smelting, smoking or blasting (auto_ in front for the auto upgrades). */
    public String getName() {
        return name;
    }

    /** Ticks to cook one item at a speed multiplier of 1 (the original recipes' cooking time). */
    public int getCookTime() {
        return cookTime;
    }

    /** How much of a fuel's furnace burn time it gives (the original's burnTimeModifier). */
    public float getBurnTimeModifier() {
        return burnTimeModifier;
    }

    static ItemStack getFurnaceResult(ItemStack input) {
        return input.isEmpty() ? ItemStack.EMPTY : FurnaceRecipes.instance().getSmeltingResult(input);
    }

    private static boolean hasOrePrefix(ItemStack stack, String prefix) {
        if (stack.isEmpty()) {
            return false;
        }
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (OreDictionary.getOreName(id).startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
