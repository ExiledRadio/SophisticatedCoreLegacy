/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeInventoryPartBase;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.upgrades.battery.BatteryUpgradeContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.Collections;

/** The battery drawn over two inventory columns: charge segments from yellow (bottom) to red (top). */
public class BatteryInventoryPart extends UpgradeInventoryPartBase<BatteryUpgradeContainer> {
    private static final TextureBlitData OVERLAY = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(47, 56), new Dimension(16, 18));
    private static final TextureBlitData CHARGE_SEGMENT = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(47, 74), new Dimension(16, 6));
    private static final TextureBlitData CONNECTION_TOP = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(47, 48), new Dimension(16, 4));
    private static final TextureBlitData CONNECTION_BOTTOM = new TextureBlitData(GuiHelper.GUI_CONTROLS, new UV(47, 52), new Dimension(16, 4));
    private static final int TOP_BAR_COLOR = 0xff1a1a;
    private static final int BOTTOM_BAR_COLOR = 0xffff40;
    private final Position pos;
    private final int height;
    private final StorageScreenBase screen;

    public BatteryInventoryPart(int upgradeSlot, BatteryUpgradeContainer container, Position pos, int height, StorageScreenBase screen) {
        super(upgradeSlot, container);
        this.pos = pos;
        this.height = height;
        this.screen = screen;
    }

    private int getBatteryLeft() {
        return screen.getGuiLeft() + pos.getX() + 9;
    }

    private int getTop() {
        return screen.getGuiTop() + pos.getY();
    }

    @Override
    public void render(int mouseX, int mouseY) {
        GuiHelper.renderBarBackground(getBatteryLeft(), getTop(), height);
        for (int yOffset = 0; yOffset + 18 <= height; yOffset += 18) {
            GuiHelper.blit(getBatteryLeft() + 1, getTop() + yOffset, OVERLAY);
        }
        renderCharge();
        GuiHelper.blit(getBatteryLeft() + 1, getTop(), CONNECTION_TOP);
        GuiHelper.blit(getBatteryLeft() + 1, getTop() + height - 4, CONNECTION_BOTTOM);
        if (mouseX >= getBatteryLeft() + 1 && mouseX < getBatteryLeft() + 17 && mouseY >= getTop() + 1 && mouseY < getTop() + height - 1) {
            GuiHelper.setTooltipToRender(Collections.singletonList(I18n.format(TranslationHelper.translUpgradeKey("battery.contents_tooltip"),
                    String.format("%,d", container.getEnergyStored()), String.format("%,d", container.getMaxEnergyStored()))));
        }
    }

    private void renderCharge() {
        int maxEnergyStored = container.getMaxEnergyStored();
        if (maxEnergyStored <= 0) {
            return;
        }
        int segmentHeight = CHARGE_SEGMENT.getHeight();
        int numberOfSegments = height / segmentHeight;
        int displayLevel = (int) (numberOfSegments * ((float) container.getEnergyStored() / maxEnergyStored));
        for (int i = 0; i < displayLevel; i++) {
            float percentage = numberOfSegments <= 1 ? 1 : (float) i / (numberOfSegments - 1);
            float red = mix(BOTTOM_BAR_COLOR >> 16 & 255, TOP_BAR_COLOR >> 16 & 255, percentage);
            float green = mix(BOTTOM_BAR_COLOR >> 8 & 255, TOP_BAR_COLOR >> 8 & 255, percentage);
            float blue = mix(BOTTOM_BAR_COLOR & 255, TOP_BAR_COLOR & 255, percentage);
            GuiHelper.blit(getBatteryLeft() + 1, getTop() + height - (i + 1) * segmentHeight, CHARGE_SEGMENT, red, green, blue);
        }
        GlStateManager.color(1, 1, 1, 1);
    }

    private static float mix(int from, int to, float percentage) {
        return (from * (1 - percentage) + to * percentage) / 255f;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }
}
