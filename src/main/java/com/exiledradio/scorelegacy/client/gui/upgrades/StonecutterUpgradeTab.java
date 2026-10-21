/*
 * Modified from Sophisticated Core (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeSettingsTab;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinitions;
import com.exiledradio.scorelegacy.client.gui.controls.ToggleButton;
import com.exiledradio.scorelegacy.client.gui.controls.Widget;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.upgrades.stonecutter.StonecutterUpgradeContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * A stonecutter: the input slot, a scrolling grid of what it can be cut into, and the result
 * slot (the original's StonecutterUpgradeTab and StonecutterRecipeControl). The button picks
 * where shift-clicked results go. The list scrolls with the wheel or a click on the slider's
 * track (1.12 widgets don't get drags).
 */
public class StonecutterUpgradeTab extends UpgradeSettingsTab<StonecutterUpgradeContainer> {
    private final RecipeControl recipeControl;

    public StonecutterUpgradeTab(StonecutterUpgradeContainer container, Position position, StorageScreenBase screen) {
        super(container, position, screen, "stonecutter");
        addHideableChild(new ToggleButton<>(new Position(x + 3, y + 24), ButtonDefinitions.SHIFT_CLICK_TARGET,
                button -> container.setShiftClickIntoStorage(!container.shouldShiftClickIntoStorage()), container::shouldShiftClickIntoStorage));
        recipeControl = addHideableChild(new RecipeControl(screen, container, new Position(x + 3, y + 24)));
    }

    @Override
    protected void moveSlotsToTab() {
        recipeControl.moveSlotsToView();
    }

    private static class RecipeControl extends Widget {
        private static final TextureBlitData SLIDER = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(29, 131), new Dimension(12, 15));
        private static final TextureBlitData DISABLED_SLIDER = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(41, 131), new Dimension(12, 15));
        private static final TextureBlitData RECIPE_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(110, 148), new Dimension(16, 18));
        private static final TextureBlitData SELECTED_RECIPE_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(110, 166), new Dimension(16, 18));
        private static final TextureBlitData RECIPE_BACKGROUND_HOVERED = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(110, 184), new Dimension(16, 18));
        private static final TextureBlitData LIST_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(29, 146), new Dimension(81, 56));
        private static final int WIDTH = 81;
        private static final int LIST_Y_OFFSET = 22;
        private static final int INPUT_SLOT_HEIGHT = 18;
        private static final int SPACING = 4;
        private static final int VISIBLE = 12;
        private final GuiContainer screen;
        private final StonecutterUpgradeContainer container;
        private int recipeIndexOffset;
        private float sliderProgress;
        private ItemStack lastInput = ItemStack.EMPTY;

        RecipeControl(GuiContainer screen, StonecutterUpgradeContainer container, Position position) {
            super(position);
            this.screen = screen;
            this.container = container;
        }

        void moveSlotsToView() {
            Slot inputSlot = container.getInputSlot();
            inputSlot.xPos = x + getCenteredX(16) - screen.getGuiLeft();
            inputSlot.yPos = y - screen.getGuiTop() + 1;
            Slot outputSlot = container.getOutputSlot();
            outputSlot.xPos = x + getCenteredX(16) - screen.getGuiLeft();
            outputSlot.yPos = inputSlot.yPos + INPUT_SLOT_HEIGHT + SPACING + LIST_BACKGROUND.getHeight() + SPACING + 4;
        }

        private int getCenteredX(int elementWidth) {
            return (WIDTH - elementWidth) / 2;
        }

        @Override
        public int getWidth() {
            return WIDTH;
        }

        @Override
        public int getHeight() {
            return INPUT_SLOT_HEIGHT + SPACING + LIST_BACKGROUND.getHeight() + SPACING + 26;
        }

        @Override
        protected void renderBg(int mouseX, int mouseY) {
            onInventoryUpdate();
            GuiHelper.renderSlotsBackground(x + getCenteredX(18), y, 1, 1, 0);
            GuiHelper.blit(x, y + LIST_Y_OFFSET, LIST_BACKGROUND);
            GuiHelper.blit(x + getCenteredX(26), y + INPUT_SLOT_HEIGHT + SPACING + LIST_BACKGROUND.getHeight() + SPACING, GuiHelper.CRAFTING_RESULT_SLOT);
            int sliderYOffset = (int) (39.0F * sliderProgress) + 1;
            GuiHelper.blit(x + 68, y + LIST_Y_OFFSET + sliderYOffset, canScroll() ? SLIDER : DISABLED_SLIDER);
            List<ItemStack> recipes = container.getRecipeList();
            int selected = container.getSelectedRecipe();
            for (int recipeIndex = recipeIndexOffset; recipeIndex < recipeIndexOffset + VISIBLE && recipeIndex < recipes.size(); recipeIndex++) {
                int recipeX = getRecipeX(recipeIndex);
                int recipeY = getRecipeY(recipeIndex);
                TextureBlitData background = RECIPE_BACKGROUND;
                if (recipeIndex == selected) {
                    background = SELECTED_RECIPE_BACKGROUND;
                } else if (mouseX >= recipeX && mouseY >= recipeY && mouseX < recipeX + 16 && mouseY < recipeY + 18) {
                    background = RECIPE_BACKGROUND_HOVERED;
                }
                GuiHelper.blit(recipeX, recipeY - 1, background);
            }
            for (int recipeIndex = recipeIndexOffset; recipeIndex < recipeIndexOffset + VISIBLE && recipeIndex < recipes.size(); recipeIndex++) {
                GuiHelper.renderItemInGUI(recipes.get(recipeIndex), getRecipeX(recipeIndex), getRecipeY(recipeIndex));
            }
        }

        private int getRecipeX(int recipeIndex) {
            return x + 1 + (recipeIndex - recipeIndexOffset) % 4 * 16;
        }

        private int getRecipeY(int recipeIndex) {
            return y + LIST_Y_OFFSET + (recipeIndex - recipeIndexOffset) / 4 * 18 + 2;
        }

        @Override
        protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
            if (!container.hasItemsInInputSlot()) {
                return;
            }
            List<ItemStack> recipes = container.getRecipeList();
            for (int recipeIndex = recipeIndexOffset; recipeIndex < recipeIndexOffset + VISIBLE && recipeIndex < recipes.size(); recipeIndex++) {
                int recipeX = getRecipeX(recipeIndex);
                int recipeY = getRecipeY(recipeIndex);
                if (mouseX >= recipeX && mouseX < recipeX + 16 && mouseY >= recipeY && mouseY < recipeY + 18) {
                    Minecraft mc = Minecraft.getMinecraft();
                    GuiHelper.setTooltipToRender(recipes.get(recipeIndex).getTooltip(mc.player,
                            mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL));
                }
            }
        }

        private boolean canScroll() {
            return container.hasItemsInInputSlot() && container.getRecipeList().size() > VISIBLE;
        }

        /** A new kind of input starts at the top, scrolled to show its chosen result. */
        private void onInventoryUpdate() {
            ItemStack input = container.getInputSlot().getStack();
            if (ItemStack.areItemsEqual(input, lastInput) && ItemStack.areItemStackTagsEqual(input, lastInput)) {
                return;
            }
            lastInput = input.copy();
            sliderProgress = 0.0F;
            recipeIndexOffset = 0;
            int selected = container.getSelectedRecipe();
            if (selected - recipeIndexOffset >= VISIBLE) {
                scrollRecipesByDelta(-((selected - VISIBLE) / 4 + 1));
            }
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            if (!container.hasItemsInInputSlot()) {
                return false;
            }
            List<ItemStack> recipes = container.getRecipeList();
            for (int recipeIndex = recipeIndexOffset; recipeIndex < recipeIndexOffset + VISIBLE && recipeIndex < recipes.size(); recipeIndex++) {
                int relativeX = mouseX - getRecipeX(recipeIndex);
                int relativeY = mouseY - (getRecipeY(recipeIndex) - 1);
                if (relativeX >= 0 && relativeY >= 0 && relativeX < 16 && relativeY < 18) {
                    container.selectRecipe(recipeIndex);
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
            int sliderLeftX = x + 68;
            int listTopY = y + LIST_Y_OFFSET;
            if (canScroll() && mouseX >= sliderLeftX && mouseX < sliderLeftX + 12 && mouseY >= listTopY && mouseY < listTopY + 54) {
                sliderProgress = Math.max(0.0F, Math.min(1.0F, (mouseY - listTopY - 7.5F) / (54 - 15.0F)));
                recipeIndexOffset = (int) (sliderProgress * getHiddenRows() + 0.5D) * 4;
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
            if (canScroll()) {
                scrollRecipesByDelta(delta);
            }
            return true;
        }

        private void scrollRecipesByDelta(double delta) {
            int hiddenRows = getHiddenRows();
            if (hiddenRows <= 0) {
                return;
            }
            sliderProgress = (float) Math.max(0.0F, Math.min(1.0F, sliderProgress - delta / hiddenRows));
            recipeIndexOffset = (int) (sliderProgress * hiddenRows + 0.5D) * 4;
        }

        private int getHiddenRows() {
            return (container.getRecipeList().size() + 4 - 1) / 4 - 3;
        }
    }
}
