/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades;

import com.exiledradio.scorelegacy.api.IUpgradeItem;
import com.exiledradio.scorelegacy.api.IUpgradeWrapper;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class UpgradeItemBase<T extends IUpgradeWrapper> extends Item implements IUpgradeItem<T> {
    protected UpgradeItemBase(String name, CreativeTabs tab) {
        // Core adds no items of its own: setRegistryName(String) prefixes the mod that is
        // registering this item (Backpacks, Storage), so each mod gets its own upgrade items.
        setRegistryName(name);
        //noinspection ConstantConditions - set on the line above
        setTranslationKey(getRegistryName().getNamespace() + "." + name);
        setMaxStackSize(1);
        setCreativeTab(tab);
    }

    // Item.addInformation is client-only; FML strips this override on a dedicated server.
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        //noinspection ConstantConditions - registered items always have a registry name
        String key = "item." + getRegistryName().getNamespace() + "." + getRegistryName().getPath() + ".tooltip";
        for (String line : TranslationHelper.translateLines(key)) {
            tooltip.add(TextFormatting.DARK_GRAY + line);
        }
    }
}
