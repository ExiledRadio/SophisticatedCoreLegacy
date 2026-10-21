/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import com.exiledradio.scorelegacy.client.gui.controls.CompositeWidget;
import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;
import com.exiledradio.scorelegacy.common.gui.UpgradeContainerBase;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The column of upgrade tabs. Only one is open at a time; the open one is drawn last, on top. */
public class UpgradeSettingsTabControl extends CompositeWidget<Tab> {
    private static final int VERTICAL_SPACE = 1;
    @Nullable
    private SettingsTabBase openTab = null;

    public UpgradeSettingsTabControl(Position position, StorageScreenBase screen) {
        super(position);
        StorageContainerBase container = screen.getContainer();
        addChild(new StorageSettingsTab(new Position(x, getNextTopY()), container));
        for (Map.Entry<Integer, UpgradeContainerBase<?, ?>> entry : container.getUpgradeContainers().entrySet()) {
            UpgradeSettingsTab<?> tab = UpgradeGuiManager.getTab(entry.getValue(), new Position(x, getNextTopY()), screen);
            if (tab == null) {
                continue;
            }
            int upgradeSlot = entry.getKey();
            addSettingsTab(() -> container.setOpenTabId(upgradeSlot), () -> container.setOpenTabId(-1), tab);
            tab.onAfterInit();
        }
    }

    private void addSettingsTab(Runnable onTabOpenContainerAction, Runnable onTabCloseContainerAction, SettingsTabBase tab) {
        addChild(tab);
        tab.setHandlers(() -> {
                    if (openTab != null && openTab != tab) {
                        openTab.close();
                    }
                    openTab = tab;
                    onTabOpenContainerAction.run();
                },
                () -> {
                    if (openTab == tab) {
                        openTab = null;
                        onTabCloseContainerAction.run();
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
            // Closed tabs partly under the open one have already drawn their icons. Item renders
            // write depth in front of flat GUI quads, so without this an icon shows through the
            // open tab's background (the next tab's icon over a tank's input slot).
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
            openTab.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        // The open tab is on top, so it gets the click first.
        if (openTab != null && openTab.isMouseOver(mouseX, mouseY)) {
            return openTab.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
        return openTab != null && openTab.isMouseOver(mouseX, mouseY) && openTab.mouseScrolled(mouseX, mouseY, delta);
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
}
