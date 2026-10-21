package com.exiledradio.scorelegacy.settings;

import com.exiledradio.scorelegacy.common.gui.SettingsContainerBase;
import com.exiledradio.scorelegacy.settings.itemdisplay.ItemDisplaySettingsCategory;
import com.exiledradio.scorelegacy.settings.itemdisplay.ItemDisplaySettingsContainer;
import com.exiledradio.scorelegacy.settings.main.MainSettingsCategory;
import com.exiledradio.scorelegacy.settings.main.MainSettingsContainer;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsCategory;
import com.exiledradio.scorelegacy.settings.memory.MemorySettingsContainer;
import com.exiledradio.scorelegacy.settings.nosort.NoSortSettingsCategory;
import com.exiledradio.scorelegacy.settings.nosort.NoSortSettingsContainer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** The settings-screen container for each settings category, by category name. Mods can add their own. */
public final class SettingsContainerRegistry {
    private static final Map<String, IFactory<?>> FACTORIES = new HashMap<>();

    static {
        register(MainSettingsCategory.NAME, MainSettingsContainer::new);
        register(NoSortSettingsCategory.NAME, NoSortSettingsContainer::new);
        register(MemorySettingsCategory.NAME, MemorySettingsContainer::new);
        register(ItemDisplaySettingsCategory.NAME, ItemDisplaySettingsContainer::new);
    }

    private SettingsContainerRegistry() {}

    public static <C extends ISettingsCategory> void register(String categoryName, IFactory<C> factory) {
        FACTORIES.put(categoryName, factory);
    }

    @Nullable
    public static SettingsCategoryContainer<?> create(SettingsContainerBase settingsContainer, String categoryName, ISettingsCategory category) {
        //noinspection unchecked - registered under the category's own name
        IFactory<ISettingsCategory> factory = (IFactory<ISettingsCategory>) FACTORIES.get(categoryName);
        return factory == null ? null : factory.create(settingsContainer, categoryName, category);
    }

    public interface IFactory<C extends ISettingsCategory> {
        SettingsCategoryContainer<C> create(SettingsContainerBase settingsContainer, String categoryName, C category);
    }
}
