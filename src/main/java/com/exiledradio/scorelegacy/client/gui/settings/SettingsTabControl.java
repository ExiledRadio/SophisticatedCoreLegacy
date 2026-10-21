/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui.settings;

import com.exiledradio.scorelegacy.client.gui.SettingsTabBase;
import com.exiledradio.scorelegacy.client.gui.Tab;
import com.exiledradio.scorelegacy.client.gui.controls.Button;
import com.exiledradio.scorelegacy.client.gui.controls.ButtonDefinition;
import com.exiledradio.scorelegacy.client.gui.controls.CompositeWidget;
import com.exiledradio.scorelegacy.client.gui.utils.Dimension;
import com.exiledradio.scorelegacy.client.gui.utils.GuiHelper;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.client.gui.utils.TextureBlitData;
import com.exiledradio.scorelegacy.client.gui.utils.TranslationHelper;
import com.exiledradio.scorelegacy.client.gui.utils.UV;
import com.exiledradio.scorelegacy.common.gui.SettingsContainerBase;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The settings screen's tab column: "back to storage", then one tab per settings category. */
public class SettingsTabControl extends CompositeWidget<Tab> {
    private static final int VERTICAL_SPACE = 1;
    private final List<SettingsTab<?>> settingsTabs = new ArrayList<>();
    @Nullable
    private SettingsTabBase openTab = null;

    public SettingsTabControl(Position position, GuiContainer screen, SettingsContainerBase container) {
        super(position);
        addChild(new BackTab(new Position(x, getNextTopY()), container));
        container.forEachCategoryContainer((name, categoryContainer) -> {
            if (container.isSettingsCategoryDisabled(name)) {
                return;
            }
            SettingsTab<?> tab = SettingsTabRegistry.create(name, categoryContainer, new Position(x, getNextTopY()), screen);
            if (tab != null) {
                addSettingsTab(tab);
            }
        });
    }

    private void addSettingsTab(SettingsTab<?> tab) {
        addChild(tab);
        settingsTabs.add(tab);
        tab.setHandlers(() -> {
                    if (openTab != null && openTab != tab) {
                        openTab.close();
                    }
                    openTab = tab;
                },
                () -> {
                    if (openTab == tab) {
                        openTab = null;
                    }
                },
                () -> openTab == null || openTab == tab || isNotCovered(openTab, tab, true),
                () -> openTab == null || isNotCovered(openTab, tab, false));
    }

    private static boolean isNotCovered(Tab open, Tab tab, boolean checkFullyCovered) {
        if (checkFullyCovered) {
            return open.getBottomY() < tab.getBottomY() || open.getTopY() > tab.getTopY();
        }
        return open.getBottomY() < tab.getTopY() || open.getTopY() > tab.getTopY();
    }

    private int getNextTopY() {
        return y + children.size() * (Tab.DEFAULT_HEIGHT + VERTICAL_SPACE);
    }

    @Nullable
    public SettingsTabBase getOpenTab() {
        return openTab;
    }

    /** Slot clicks go to the open tab only. */
    public void handleSlotClick(int slotIndex, int mouseButton) {
        if (openTab instanceof SettingsTab) {
            ((SettingsTab<?>) openTab).handleSlotClick(slotIndex, mouseButton);
        }
    }

    /** Every tab's colour for the slot (shown even while the tab is closed). */
    public List<Integer> getSlotOverlayColors(int slotIndex) {
        List<Integer> colors = new ArrayList<>();
        for (SettingsTab<?> tab : settingsTabs) {
            Integer color = tab.getSlotOverlayColor(slotIndex);
            if (color != null) {
                colors.add(color);
            }
        }
        return colors;
    }

    public int getItemRotation(int slotIndex) {
        for (SettingsTab<?> tab : settingsTabs) {
            int rotation = tab.getItemRotation(slotIndex);
            if (rotation != 0) {
                return rotation;
            }
        }
        return 0;
    }

    @Override
    protected void renderBg(int mouseX, int mouseY) {
        //noop
    }

    @Override
    protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
        for (Tab tab : children) {
            if (tab != openTab) {
                tab.render(mouseX, mouseY, partialTicks);
            }
        }
        if (openTab != null) {
            // Icons of tabs drawn so far must not show through the open tab (see UpgradeSettingsTabControl).
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
            openTab.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (openTab != null && openTab.isMouseOver(mouseX, mouseY)) {
            return openTab.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        for (Tab tab : children) {
            if (tab.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getWidth() {
        int maxWidth = 0;
        for (Tab tab : children) {
            maxWidth = Math.max(maxWidth, tab.getWidth());
        }
        return maxWidth;
    }

    @Override
    public int getHeight() {
        int maxY = y;
        for (Tab tab : children) {
            maxY = Math.max(maxY, tab.getBottomY());
        }
        return maxY - y;
    }

    public List<Rectangle> getTabRectangles() {
        List<Rectangle> ret = new ArrayList<>();
        children.forEach(child -> ret.add(child.getRectangle()));
        return ret;
    }

    /** Back to the storage's own screen. */
    private static class BackTab extends Tab {
        private final SettingsContainerBase container;

        BackTab(Position position, SettingsContainerBase container) {
            super(position, TranslationHelper.tooltip(container.getStorageWrapper().getBackToStorageTooltipKey()),
                    onTabIconClicked -> new Button(new Position(position.getX() + 1, position.getY() + 4),
                            new ButtonDefinition(Dimension.SQUARE_16, null, null, new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(64, 80), Dimension.SQUARE_16),
                                    Collections::emptyList), onTabIconClicked));
            this.container = container;
        }

        @Override
        protected void onTabIconClicked(int button) {
            container.backToStorage();
        }
    }
}
