/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.upgrades.crafting;

import com.exiledradio.scorelegacy.inventory.UpgradeHandler;
import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;
import com.exiledradio.scorelegacy.common.gui.SlotSuppliedHandler;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class CraftingUpgradeContainer extends UpgradeContainerBase<CraftingUpgradeWrapper, CraftingUpgradeContainer> {
    private static final String DATA_SHIFT_CLICK_INTO_BACKPACK = "shiftClickIntoBackpack";
    private final InventoryCraftResult craftResult = new InventoryCraftResult();
    private final CraftingItemHandler craftMatrix;
    private final SlotCrafting craftingResultSlot;

    public CraftingUpgradeContainer(EntityPlayer player, int containerId, CraftingUpgradeWrapper wrapper, UpgradeHandler upgradeHandler,
                                    UpgradeContainerType<CraftingUpgradeWrapper, CraftingUpgradeContainer> type) {
        super(player, containerId, wrapper, upgradeHandler, type);
        Supplier<IItemHandler> inventory = () -> getUpgradeWrapper().getInventory();
        for (int slot = 0; slot < 9; slot++) {
            slots.add(new SlotSuppliedHandler(inventory, slot, -100, -100) {
                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    updateCraftingResult();
                }

                @Nonnull
                @Override
                public ItemStack decrStackSize(int amount) {
                    ItemStack taken = super.decrStackSize(amount);
                    updateCraftingResult();
                    return taken;
                }
            });
        }
        craftMatrix = new CraftingItemHandler(() -> (IItemHandlerModifiable) getUpgradeWrapper().getInventory(), this::onCraftMatrixChanged);
        // Vanilla's result slot already crafts, consumes the grid, returns container items and
        // fires the crafting events; the matrix above routes all of that to the upgrade.
        craftingResultSlot = new SlotCrafting(player, craftMatrix, craftResult, 0, -100, -100);
        slots.add(craftingResultSlot);
        updateCraftingResult();
    }

    private void onCraftMatrixChanged(IInventory inventory) {
        updateCraftingResult();
    }

    /**
     * Only the server matches recipes. 1.12 doesn't send slot changes right after a click
     * (the client is trusted to have predicted them), and the client can't predict a recipe
     * result, so the result slot goes out directly, as vanilla's crafting table does it.
     */
    private void updateCraftingResult() {
        if (player.world.isRemote || craftingResultSlot == null) {
            return;
        }
        ItemStack result = ItemStack.EMPTY;
        IRecipe recipe = CraftingManager.findMatchingRecipe(craftMatrix, player.world);
        if (recipe != null && (recipe.isDynamic() || !player.world.getGameRules().getBoolean("doLimitedCrafting")
                || ((EntityPlayerMP) player).getRecipeBook().isUnlocked(recipe))) {
            craftResult.setRecipeUsed(recipe);
            result = recipe.getCraftingResult(craftMatrix);
        }
        craftResult.setInventorySlotContents(0, result);
        if (player.openContainer instanceof StorageContainerBase && player.openContainer.inventorySlots.contains(craftingResultSlot)) {
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketSetSlot(player.openContainer.windowId, craftingResultSlot.slotNumber, result));
        }
    }

    /** The 3x3 grid, in row order (recipe transfer fills these). */
    public java.util.List<Slot> getCraftingGridSlots() {
        return slots.subList(0, 9);
    }

    public boolean isCraftingResultSlot(Slot slot) {
        return slot == craftingResultSlot;
    }

    @Override
    public boolean mergeIntoBackpackFirst(Slot slot) {
        return slot != craftingResultSlot || shouldShiftClickIntoBackpack();
    }

    @Override
    public void handleMessage(NBTTagCompound data) {
        if (data.hasKey(DATA_SHIFT_CLICK_INTO_BACKPACK)) {
            setShiftClickIntoBackpack(data.getBoolean(DATA_SHIFT_CLICK_INTO_BACKPACK));
        }
    }

    public boolean shouldShiftClickIntoBackpack() {
        return getUpgradeWrapper().shouldShiftClickIntoBackpack();
    }

    public void setShiftClickIntoBackpack(boolean shiftClickIntoBackpack) {
        getUpgradeWrapper().setShiftClickIntoBackpack(shiftClickIntoBackpack);
        sendBooleanToServer(DATA_SHIFT_CLICK_INTO_BACKPACK, shiftClickIntoBackpack);
    }
}
