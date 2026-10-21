/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.client.gui.controls.CompositeWidget;
import com.exiledradio.scorelegacy.client.gui.controls.ProgressBar;
import com.exiledradio.scorelegacy.client.gui.controls.Widget;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.upgrades.cooking.CookingLogic;
import com.exiledradio.scorelegacy.upgrades.cooking.CookingLogicContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.world.World;

import java.util.List;

/** The furnace picture with its three slots, the cook arrow and the fuel flame. */
public class CookingLogicControl extends CompositeWidget<Widget> {
    private static final TextureBlitData FURNACE_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(29, 202), new Dimension(68, 54));
    private static final TextureBlitData COOK_PROGRESS = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(100, 239), new Dimension(22, 16));
    private static final TextureBlitData BURN_PROGRESS = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(99, 225), new Dimension(14, 14));
    private final CookingLogicContainer cookingLogicContainer;

    public CookingLogicControl(Position position, CookingLogicContainer cookingLogicContainer) {
        super(position);
        this.cookingLogicContainer = cookingLogicContainer;
        addChild(new ProgressBar(new Position(x + 19, y + 18), COOK_PROGRESS, this::getCookProgress, ProgressBar.ProgressDirection.LEFT_RIGHT));
        addChild(new ProgressBar(new Position(x + 1, y + 20), BURN_PROGRESS, this::getBurnProgress, ProgressBar.ProgressDirection.BOTTOM_UP));
    }

    /** The flame shrinks as the fuel burns down, so this is the fraction still left. */
    private float getBurnProgress() {
        World world = minecraft.world;
        return world != null && cookingLogicContainer.isBurning(world)
                ? 1 - getProgress(cookingLogicContainer.getBurnTimeFinish(), cookingLogicContainer.getBurnTimeTotal()) : 0;
    }

    private float getCookProgress() {
        return cookingLogicContainer.isCooking() ? getProgress(cookingLogicContainer.getCookTimeFinish(), cookingLogicContainer.getCookTimeTotal()) : 0;
    }

    private float getProgress(long finishTime, int timeTotal) {
        World world = minecraft.world;
        if (world == null || timeTotal <= 0) {
            return 0;
        }
        return 1 - ((float) Math.max(finishTime - world.getTotalWorldTime(), 0) / timeTotal);
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        GuiHelper.blit(x, y, FURNACE_BACKGROUND);
    }

    @Override
    public int getWidth() {
        return 68;
    }

    @Override
    public int getHeight() {
        return 54;
    }

    public void moveSlotsToView(int screenGuiLeft, int screenGuiTop) {
        List<Slot> slots = cookingLogicContainer.getCookingSlots();
        positionSlot(slots.get(CookingLogic.COOK_INPUT_SLOT), screenGuiLeft, screenGuiTop, 1, 1);
        positionSlot(slots.get(CookingLogic.COOK_OUTPUT_SLOT), screenGuiLeft, screenGuiTop, 47, 19);
        positionSlot(slots.get(CookingLogic.FUEL_SLOT), screenGuiLeft, screenGuiTop, 1, 37);
    }

    private void positionSlot(Slot slot, int screenGuiLeft, int screenGuiTop, int xOffset, int yOffset) {
        slot.xPos = x - screenGuiLeft + xOffset;
        slot.yPos = y - screenGuiTop + yOffset;
    }
}
