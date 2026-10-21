/*
 * Modified from Sophisticated Storage (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.scorelegacy.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;

/** Core's key bindings, for every storage screen (backpacks and Storage's blocks). */
public final class CoreKeyBindings {
    /** Middle mouse by default (1.12 stores mouse button N as N - 100), like the original's sort keybind. */
    public static final KeyBinding SORT = new KeyBinding("keybind.scorelegacy.sort", KeyConflictContext.GUI, -98, "keybind.scorelegacy.category");

    private CoreKeyBindings() {}

    static void register() {
        ClientRegistry.registerKeyBinding(SORT);
    }
}
