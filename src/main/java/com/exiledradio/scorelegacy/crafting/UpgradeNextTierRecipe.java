/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.crafting;

import com.exiledradio.scorelegacy.api.IUpgradeItem;
import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

/** Turns an upgrade into its advanced version, keeping its filter and settings. */
public class UpgradeNextTierRecipe extends ShapedOreRecipe {

    public UpgradeNextTierRecipe(ResourceLocation group, ItemStack result, CraftingHelper.ShapedPrimer primer) {
        super(group, result, primer);
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        ItemStack result = super.getCraftingResult(inv);
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack slotStack = inv.getStackInSlot(slot);
            if (slotStack.getItem() instanceof IUpgradeItem) {
                NBTTagCompound tag = slotStack.getTagCompound();
                if (tag != null) {
                    result.setTagCompound(tag.copy());
                }
                break;
            }
        }
        return result;
    }

    /** Registered in recipes/_factories.json as "sbackpackslegacy:upgrade_next_tier". */
    public static class Factory implements IRecipeFactory {
        @Override
        public UpgradeNextTierRecipe parse(JsonContext context, JsonObject json) {
            ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);
            CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
            primer.width = recipe.getRecipeWidth();
            primer.height = recipe.getRecipeHeight();
            primer.mirrored = true;
            primer.input = recipe.getIngredients();
            String group = recipe.getGroup();
            return new UpgradeNextTierRecipe(group.isEmpty() ? null : new ResourceLocation(group), recipe.getRecipeOutput(), primer);
        }
    }
}
