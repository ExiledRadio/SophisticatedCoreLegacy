package com.exiledradio.scorelegacy.client.gui.settings;

import com.exiledradio.scorelegacy.client.gui.utils.Position;
import com.exiledradio.scorelegacy.settings.SettingsCategoryContainer;
import com.exiledradio.scorelegacy.settings.itemdisplay.ItemDisplaySettingsCategory;
import com.exiledradio.scorelegacy.settings.itemdisplay.ItemDisplaySettingsContainer;
import com.exiledradio.scorelegacy.settings.main.MainSettingsCategory;
import com.exiledradio.scorelegacy.settings.main.MainSettingsContainer;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsContainer;
import com.exiledradio.scorelegacy.settings.nosort.NoSortSettingsCategory;
import com.exiledradio.scorelegacy.settings.nosort.NoSortSettingsContainer;
import net.minecraft.client.gui.inventory.GuiContainer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** The tab for each settings category, by category name. Client only. */
public final class SettingsTabRegistry {
    private static final Map<String, IFactory<?>> FACTORIES = new HashMap<>();

    static {
        register(MainSettingsCategory.NAME, (IFactory<MainSettingsContainer>) MainSettingsTab::new);
        register(NoSortSettingsCategory.NAME, (IFactory<NoSortSettingsContainer>) SlotSelectionTabs.NoSortTab::new);
        register(MemorySettingsCategory.NAME, (IFactory<MemorySettingsContainer>) SlotSelectionTabs.MemoryTab::new);
        register(ItemDisplaySettingsCategory.NAME, (IFactory<ItemDisplaySettingsContainer>) SlotSelectionTabs.ItemDisplayTab::new);
    }

    private SettingsTabRegistry() {}

    public static <C extends SettingsCategoryContainer<?>> void register(String categoryName, IFactory<C> factory) {
        FACTORIES.put(categoryName, factory);
    }

    @Nullable
    public static SettingsTab<?> create(String categoryName, SettingsCategoryContainer<?> container, Position position, GuiContainer screen) {
        //noinspection unchecked - registered under the container's own category name
        IFactory<SettingsCategoryContainer<?>> factory = (IFactory<SettingsCategoryContainer<?>>) FACTORIES.get(categoryName);
        return factory == null ? null : factory.create(container, position, screen);
    }

    public interface IFactory<C extends SettingsCategoryContainer<?>> {
        SettingsTab<C> create(C container, Position position, GuiContainer screen);
    }
}
