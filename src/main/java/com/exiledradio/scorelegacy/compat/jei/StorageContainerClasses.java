package com.exiledradio.scorelegacy.compat.jei;

import com.exiledradio.scorelegacy.common.gui.StorageContainerBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The concrete storage container classes, for JEI's recipe transfer (which looks containers up
 * by exact class). Storage mods add theirs in preInit. Names no JEI type, so it is safe to call
 * without JEI installed.
 */
public final class StorageContainerClasses {
    private static final List<Class<? extends StorageContainerBase>> CLASSES = new ArrayList<>();

    private StorageContainerClasses() {}

    public static void register(Class<? extends StorageContainerBase> containerClass) {
        CLASSES.add(containerClass);
    }

    public static List<Class<? extends StorageContainerBase>> get() {
        return Collections.unmodifiableList(CLASSES);
    }
}
