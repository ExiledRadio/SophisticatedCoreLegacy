/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.compat.jei;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.settings.SettingsScreenBase;
import com.exiledradio.scorelegacy.common.gui.IFilterSlot;
import com.exiledradio.scorelegacy.common.gui.SettingsContainerBase;
import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.network.CorePacketHandler;
import com.exiledradio.scorelegacy.network.SetGhostSlotMessage;
import com.exiledradio.scorelegacy.network.SetMemorySlotMessage;
import com.exiledradio.scorelegacy.upgrades.crafting.CraftingUpgradeContainer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JEI/HEI support for every storage screen: the upgrade column, tabs and sort buttons are kept
 * clear of JEI's overlay; items can be dragged from JEI into filter slots; and the "+" button of
 * a crafting recipe fills an open Crafting Upgrade tab from the storage and player inventory;
 * and in the settings screen's memory tab, a JEI item dropped on an empty slot is memorized there.
 */
@JEIPlugin
public class CoreJeiPlugin implements IModPlugin {

    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<StorageScreenBase>() {
            @Nonnull
            @Override
            public Class<StorageScreenBase> getGuiContainerClass() {
                return StorageScreenBase.class;
            }

            @Override
            public List<Rectangle> getGuiExtraAreas(@Nonnull StorageScreenBase gui) {
                return gui.getExtraAreas();
            }
        }, new IAdvancedGuiHandler<SettingsScreenBase>() {
            @Nonnull
            @Override
            public Class<SettingsScreenBase> getGuiContainerClass() {
                return SettingsScreenBase.class;
            }

            @Override
            public List<Rectangle> getGuiExtraAreas(@Nonnull SettingsScreenBase gui) {
                return gui.getExtraAreas();
            }
        });

        registry.addGhostIngredientHandler(StorageScreenBase.class, new FilterSlotGhostHandler());
        registry.addGhostIngredientHandler(SettingsScreenBase.class, new MemorySlotGhostHandler());

        for (Class<? extends StorageContainerBase> containerClass : StorageContainerClasses.get()) {
            registerCraftingTransfer(registry, containerClass);
        }
    }

    private static <C extends StorageContainerBase> void registerCraftingTransfer(IModRegistry registry, Class<C> containerClass) {
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(new IRecipeTransferInfo<C>() {
            @Nonnull
            @Override
            public Class<C> getContainerClass() {
                return containerClass;
            }

            @Nonnull
            @Override
            public String getRecipeCategoryUid() {
                return VanillaRecipeCategoryUid.CRAFTING;
            }

            /** Only while a Crafting Upgrade tab is open: its grid slots are off screen otherwise. */
            @Override
            public boolean canHandle(@Nonnull C container) {
                return container.getOpenUpgradeContainer() instanceof CraftingUpgradeContainer;
            }

            @Nonnull
            @Override
            public List<Slot> getRecipeSlots(@Nonnull C container) {
                UpgradeContainerBase<?, ?> open = container.getOpenUpgradeContainer();
                return open instanceof CraftingUpgradeContainer ? ((CraftingUpgradeContainer) open).getCraftingGridSlots() : Collections.emptyList();
            }

            @Nonnull
            @Override
            public List<Slot> getInventorySlots(@Nonnull C container) {
                return container.getStorageAndPlayerSlots();
            }
        });
    }

    /** Dropping a JEI item on a visible filter slot sets that filter. */
    private static class FilterSlotGhostHandler implements IGhostIngredientHandler<StorageScreenBase> {
        @Nonnull
        @Override
        public <I> List<Target<I>> getTargets(@Nonnull StorageScreenBase gui, @Nonnull I ingredient, boolean doStart) {
            List<Target<I>> targets = new ArrayList<>();
            if (!(ingredient instanceof ItemStack)) {
                return targets;
            }
            ItemStack stack = (ItemStack) ingredient;
            for (Slot slot : gui.getContainer().inventorySlots) {
                if (!(slot instanceof IFilterSlot) || slot.xPos < 0 || !slot.isItemValid(stack)) {
                    continue;
                }
                Rectangle area = new Rectangle(gui.getGuiLeft() + slot.xPos, gui.getGuiTop() + slot.yPos, 16, 16);
                int slotNumber = slot.slotNumber;
                targets.add(new Target<I>() {
                    @Nonnull
                    @Override
                    public Rectangle getArea() {
                        return area;
                    }

                    @Override
                    public void accept(@Nonnull I dropped) {
                        ItemStack ghost = ((ItemStack) dropped).copy();
                        ghost.setCount(1);
                        CorePacketHandler.CHANNEL.sendToServer(new SetGhostSlotMessage(slotNumber, ghost));
                    }
                });
            }
            return targets;
        }

        @Override
        public void onComplete() {
            //noop
        }
    }

    /** With the memory tab open, dropping a JEI item on an empty storage slot memorizes it there. */
    private static class MemorySlotGhostHandler implements IGhostIngredientHandler<SettingsScreenBase> {
        @Nonnull
        @Override
        public <I> List<Target<I>> getTargets(@Nonnull SettingsScreenBase gui, @Nonnull I ingredient, boolean doStart) {
            List<Target<I>> targets = new ArrayList<>();
            if (!(ingredient instanceof ItemStack) || !gui.isMemoryTabOpen()) {
                return targets;
            }
            SettingsContainerBase container = (SettingsContainerBase) gui.inventorySlots;
            for (int i = 0; i < container.getNumberOfSlots() && i < container.inventorySlots.size(); i++) {
                Slot slot = container.inventorySlots.get(i);
                if (slot.getHasStack()) {
                    continue;
                }
                Rectangle area = new Rectangle(gui.getGuiLeft() + slot.xPos, gui.getGuiTop() + slot.yPos, 16, 16);
                int slotIndex = slot.getSlotIndex();
                targets.add(new Target<I>() {
                    @Nonnull
                    @Override
                    public Rectangle getArea() {
                        return area;
                    }

                    @Override
                    public void accept(@Nonnull I dropped) {
                        ItemStack memorized = ((ItemStack) dropped).copy();
                        memorized.setCount(1);
                        CorePacketHandler.CHANNEL.sendToServer(new SetMemorySlotMessage(slotIndex, memorized));
                    }
                });
            }
            return targets;
        }

        @Override
        public void onComplete() {
            //noop
        }
    }
}
