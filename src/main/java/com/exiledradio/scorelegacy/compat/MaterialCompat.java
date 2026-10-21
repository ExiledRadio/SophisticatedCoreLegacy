/*
 * Written for Sophisticated Storage Legacy and Backpacks Legacy by ExiledRadio, GPLv3.
 */
package com.exiledradio.scorelegacy.compat;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

/**
 * Materials 1.12.2 itself lacks, for the original's copper and netherite tiers and crimson and
 * warped woods in Storage and Backpacks. Their blocks and items always exist, so worlds keep them
 * if a mod goes away, but they are listed and craftable only when some installed mod adds the
 * material:
 * <ul>
 * <li>copper: anything in the ore dictionary as {@code ingotCopper} (Thermal, Mekanism, IE,
 * Farmer's Future Delight, Deeper Depths, Ice and Fire...);</li>
 * <li>netherite: {@code ingotNetherite} (Future MC, Nether Backport); the netherite mods that
 * register no ore name are added to it here;</li>
 * <li>crimson and warped: Nether Backport's or Farmer's Future Delight's planks, put in the ore
 * dictionary here as {@code plankCrimson} / {@code slabCrimson} and the warped ones.</li>
 * </ul>
 * Checked in Core's postInit, once every mod has registered its items and ore names (Core's
 * dependents' postInit comes after it). Each mod then loads the recipes that need a material from
 * {@code assets/<modid>/compat_recipes/<material>/} with {@link #loadCompatRecipes}.
 */
public final class MaterialCompat {
    public enum Material {
        COPPER("ingotCopper"),
        NETHERITE("ingotNetherite"),
        CRIMSON("plankCrimson"),
        WARPED("plankWarped");

        private final String oreName;
        private boolean available = false;

        Material(String oreName) {
            this.oreName = oreName;
        }

        /** False until postInit, then whether an installed mod adds the material. */
        public boolean isAvailable() {
            return available;
        }

        public String getFolder() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    // Netherite mods that register no ore names (checked in their jars).
    private static final String[] NETHERITE_INGOTS = {"futuremc:netherite_ingot", "nb:netherite_ingot", "netherite:netherite_ingot",
            "nstuff:netherite_ingot", "netheriteupdate:netheriteingot", "netherupdate:netheriteingot"};
    private static final String[] NETHERITE_BLOCKS = {"futuremc:netherite_block", "nb:netherite_block", "netherite:netherite_block",
            "nstuff:netherite_block", "netheriteupdate:netheriteblock", "netherupdate:blockofnetherite"};
    private static final String[] CRIMSON_PLANKS = {"nb:crimson_planks", "farmers_future_delight:crimson_planks", "netherupdate:crimsonplanks"};
    private static final String[] WARPED_PLANKS = {"nb:warped_planks", "farmers_future_delight:warped_planks", "netherupdate:warpedplanks"};
    private static final String[] CRIMSON_SLABS = {"nb:crimson_slab_half", "farmers_future_delight:crimson_slab"};
    private static final String[] WARPED_SLABS = {"nb:warped_slab_half", "farmers_future_delight:warped_slab"};

    private static final Gson GSON = new Gson();

    private MaterialCompat() {}

    /** Core's postInit: fills in the missing ore names and decides what's available. */
    public static void postInit() {
        addDevStandIns();
        addOreNames("ingotNetherite", NETHERITE_INGOTS);
        addOreNames("blockNetherite", NETHERITE_BLOCKS);
        addOreNames("plankCrimson", CRIMSON_PLANKS);
        addOreNames("plankWarped", WARPED_PLANKS);
        addOreNames("slabCrimson", CRIMSON_SLABS);
        addOreNames("slabWarped", WARPED_SLABS);
        for (Material material : Material.values()) {
            material.available = !OreDictionary.getOres(material.oreName, false).isEmpty();
            SophisticatedCoreLegacy.LOGGER.info("{}: {}", material.oreName, material.available ? "found" : "not found");
        }
    }

    /**
     * A mod's postInit: registers its recipes for the materials that are available, from
     * compat_recipes/<material>/ (copper_and_netherite/ needs both).
     */
    public static void loadCompatRecipes(String modId) {
        int loaded = 0;
        for (Material material : Material.values()) {
            if (material.available) {
                loaded += loadRecipes(modId, material.getFolder());
            }
        }
        if (Material.COPPER.available && Material.NETHERITE.available) {
            loaded += loadRecipes(modId, "copper_and_netherite");
        }
        SophisticatedCoreLegacy.LOGGER.info("{}: {} compat recipes added", modId, loaded);
    }

    /**
     * Dev runs only: mods that add these materials mostly can't load in a deobfuscated environment,
     * so {@code config/sophisticated-dev-materials.txt} in the run folder can give vanilla items
     * stand-in ore names, one {@code oreName=modid:item[@meta]} per line.
     */
    private static void addDevStandIns() {
        if (!Boolean.TRUE.equals(net.minecraft.launchwrapper.Launch.blackboard.get("fml.deobfuscatedEnvironment"))) {
            return;
        }
        java.io.File file = new java.io.File("config/sophisticated-dev-materials.txt");
        if (!file.isFile()) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file.toPath())) {
                String[] parts = line.trim().split("=");
                if (parts.length != 2 || line.startsWith("#")) {
                    continue;
                }
                String[] itemAndMeta = parts[1].split("@");
                Item item = Item.REGISTRY.getObject(new ResourceLocation(itemAndMeta[0]));
                if (item != null) {
                    OreDictionary.registerOre(parts[0], new ItemStack(item, 1, itemAndMeta.length > 1 ? Integer.parseInt(itemAndMeta[1]) : 0));
                    SophisticatedCoreLegacy.LOGGER.info("Dev stand-in: {} = {}", parts[0], parts[1]);
                }
            }
        } catch (IOException | RuntimeException e) {
            SophisticatedCoreLegacy.LOGGER.error("Couldn't read {}", file, e);
        }
    }

    /** Puts the items that exist under an ore name, unless one of them already has it. */
    private static void addOreNames(String oreName, String[] registryNames) {
        for (String name : registryNames) {
            Item item = Item.REGISTRY.getObject(new ResourceLocation(name));
            if (item == null) {
                Block block = Block.REGISTRY.containsKey(new ResourceLocation(name)) ? Block.REGISTRY.getObject(new ResourceLocation(name)) : null;
                item = block == null ? null : Item.getItemFromBlock(block);
            }
            if (item == null || item == net.minecraft.init.Items.AIR) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            boolean known = false;
            for (int id : OreDictionary.getOreIDs(stack)) {
                if (OreDictionary.getOreName(id).equals(oreName)) {
                    known = true;
                }
            }
            if (!known) {
                OreDictionary.registerOre(oreName, stack);
            }
        }
    }

    private static int loadRecipes(String modId, String folder) {
        ModContainer mod = Loader.instance().getIndexedModList().get(modId);
        JsonContext context = new JsonContext(modId);
        int[] count = {0};
        CraftingHelper.findFiles(mod, "assets/" + modId + "/compat_recipes/" + folder, null, (root, file) -> {
            if (!"json".equals(FilenameUtils.getExtension(file.toString()))) {
                return true;
            }
            String name = FilenameUtils.removeExtension(root.relativize(file).toString()).replaceAll("\\\\", "/");
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                JsonObject json = JsonUtils.fromJson(GSON, reader, JsonObject.class);
                IRecipe recipe = CraftingHelper.getRecipe(json, context);
                recipe.setRegistryName(new ResourceLocation(modId, folder + "/" + name));
                ForgeRegistries.RECIPES.register(recipe);
                count[0]++;
            } catch (IOException | RuntimeException e) {
                SophisticatedCoreLegacy.LOGGER.error("Couldn't load compat recipe {}", file, e);
            }
            return true;
        }, true, true);
        return count[0];
    }
}
