/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.upgrades;

import com.exiledradio.scorelegacy.client.gui.StorageScreenBase;
import com.exiledradio.scorelegacy.client.gui.UpgradeSettingsTab;
import com.exiledradio.scorelegacy.client.gui.controls.Button;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.upgrades.jukebox.JukeboxUpgradeContainer;
import net.minecraft.inventory.Slot;

/** The disc slot with stop and play buttons under it. */
public class JukeboxUpgradeTab extends UpgradeSettingsTab<JukeboxUpgradeContainer> {
    private static final TextureBlitData PLAY_FOREGROUND = new TextureBlitData(GuiHelper.ICONS, new Position(1, 1), Dimension.SQUARE_256, new UV(16, 64), Dimension.SQUARE_16);
    private static final ButtonDefinition PLAY = new ButtonDefinition(Dimension.SQUARE_18, GuiHelper.DEFAULT_BUTTON_BACKGROUND, GuiHelper.DEFAULT_BUTTON_HOVERED_BACKGROUND,
            PLAY_FOREGROUND, TranslationHelper.tooltip(TranslationHelper.translUpgradeButton("play")));
    private static final TextureBlitData STOP_FOREGROUND = new TextureBlitData(GuiHelper.ICONS, new Position(1, 1), Dimension.SQUARE_256, new UV(0, 64), Dimension.SQUARE_16);
    private static final ButtonDefinition STOP = new ButtonDefinition(Dimension.SQUARE_18, GuiHelper.DEFAULT_BUTTON_BACKGROUND, GuiHelper.DEFAULT_BUTTON_HOVERED_BACKGROUND,
            STOP_FOREGROUND, TranslationHelper.tooltip(TranslationHelper.translUpgradeButton("stop")));

    public JukeboxUpgradeTab(JukeboxUpgradeContainer container, Position position, StorageScreenBase screen) {
        super(container, position, screen, "jukebox");
        addHideableChild(new Button(new Position(x + 3, y + 44), STOP, button -> {
            if (button == 0) {
                container.stop();
            }
        }));
        addHideableChild(new Button(new Position(x + 21, y + 44), PLAY, button -> {
            if (button == 0) {
                container.play();
            }
        }));
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        super.renderBg(mouseX, mouseY);
        if (isOpen()) {
            GuiHelper.renderSlotsBackground(x + 3, y + 24, 1, 1, 0);
        }
    }

    @Override
    protected void moveSlotsToTab() {
        Slot discSlot = getContainer().getSlots().get(0);
        discSlot.xPos = x - screen.getGuiLeft() + 4;
        discSlot.yPos = y - screen.getGuiTop() + 25;
    }
}
