/*
 * Written for Sophisticated Storage Legacy and Backpacks Legacy by ExiledRadio, GPLv3.
 */
package com.exiledradio.scorelegacy.compat;

import com.exiledradio.scorelegacy.SophisticatedCoreLegacy;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The smoker, blast furnace and stonecutter recipes of the 1.14 backports, for the original's
 * smoking, blasting and stonecutter upgrades. Every lookup is by reflection and does nothing
 * when the mod is missing; a mod whose API changed is logged once and then left out.
 * <ul>
 * <li>Future MC: its SmokerRecipes, BlastFurnaceRecipes and StonecutterRecipes lists (which
 * CraftTweaker can change);</li>
 * <li>VillagerBackport: its stonecutter table, derived from the crafting recipes of every
 * installed mod. Its smoker and blast furnace go by rules the cooking types share (see
 * {@link com.exiledradio.scorelegacy.upgrades.cooking.CookingType}).</li>
 * </ul>
 */
public final class BackportRecipes {
    private static final String FUTURE_MC = "futuremc";
    private static final String VILLAGER_BACKPORT = "villagerbackport";

    private static final FutureMcList FUTURE_MC_SMOKER = new FutureMcList("thedarkcolour.futuremc.recipe.furnace.SmokerRecipes");
    private static final FutureMcList FUTURE_MC_BLAST_FURNACE = new FutureMcList("thedarkcolour.futuremc.recipe.furnace.BlastFurnaceRecipes");
    private static final FutureMcList FUTURE_MC_STONECUTTER = new FutureMcList("thedarkcolour.futuremc.recipe.stonecutter.StonecutterRecipes");
    private static boolean villagerBackportFailed = false;
    @Nullable
    private static Object villagerBackportStonecutter;
    @Nullable
    private static Method villagerBackportResultsFor;

    private BackportRecipes() {}

    /** Future MC's smoker result for the input, or empty. */
    public static ItemStack getFutureMcSmokerResult(ItemStack input) {
        return FUTURE_MC_SMOKER.getResult(input);
    }

    /** Future MC's blast furnace result for the input, or empty. */
    public static ItemStack getFutureMcBlastFurnaceResult(ItemStack input) {
        return FUTURE_MC_BLAST_FURNACE.getResult(input);
    }

    /** Whether an installed mod has stonecutter recipes the upgrade can use. */
    public static boolean hasStonecutterRecipes() {
        return Loader.isModLoaded(FUTURE_MC) || Loader.isModLoaded(VILLAGER_BACKPORT);
    }

    /**
     * Everything the input can be cut into, VillagerBackport's results first, without
     * repeats. Each result is a fresh stack made from one input item.
     */
    public static List<ItemStack> getStonecutterResults(ItemStack input) {
        if (input.isEmpty()) {
            return Collections.emptyList();
        }
        List<ItemStack> results = new ArrayList<>();
        for (ItemStack result : getVillagerBackportStonecutterResults(input)) {
            addIfNew(results, result);
        }
        for (ItemStack result : FUTURE_MC_STONECUTTER.getResults(input)) {
            addIfNew(results, result);
        }
        return results;
    }

    private static void addIfNew(List<ItemStack> results, ItemStack result) {
        if (result.isEmpty()) {
            return;
        }
        for (ItemStack known : results) {
            if (ItemStack.areItemStacksEqual(known, result)) {
                return;
            }
        }
        results.add(result.copy());
    }

    @SuppressWarnings("unchecked")
    private static List<ItemStack> getVillagerBackportStonecutterResults(ItemStack input) {
        if (villagerBackportFailed || !Loader.isModLoaded(VILLAGER_BACKPORT)) {
            return Collections.emptyList();
        }
        try {
            if (villagerBackportResultsFor == null) {
                Class<?> table = Class.forName("com.exiledradio.villagerbackport.block.StonecutterRecipes");
                Class<?> kind = Class.forName("com.exiledradio.villagerbackport.block.CutterKind");
                Object stone = kind.getField("STONE").get(null);
                villagerBackportStonecutter = table.getMethod("of", kind).invoke(null, stone);
                villagerBackportResultsFor = table.getMethod("resultsFor", ItemStack.class);
            }
            Object results = villagerBackportResultsFor.invoke(villagerBackportStonecutter, input);
            return results instanceof List ? (List<ItemStack>) results : Collections.emptyList();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            villagerBackportFailed = true;
            SophisticatedCoreLegacy.LOGGER.error("Couldn't read VillagerBackport's stonecutter recipes; the stonecutter upgrade leaves them out", e);
            return Collections.emptyList();
        }
    }

    /** One of Future MC's recipe lists (a Kotlin object with an INSTANCE and SimpleRecipe entries). */
    private static final class FutureMcList {
        private final String className;
        private boolean failed = false;
        @Nullable
        private Object instance;
        @Nullable
        private Method getRecipes;
        @Nullable
        private Method getOutput;

        FutureMcList(String className) {
            this.className = className;
        }

        ItemStack getResult(ItemStack input) {
            List<ItemStack> results = getResults(input);
            return results.isEmpty() ? ItemStack.EMPTY : results.get(0);
        }

        List<ItemStack> getResults(ItemStack input) {
            if (input.isEmpty() || failed || !Loader.isModLoaded(FUTURE_MC)) {
                return Collections.emptyList();
            }
            try {
                if (getRecipes == null) {
                    Class<?> recipes = Class.forName(className);
                    Field instanceField = recipes.getField("INSTANCE");
                    instance = instanceField.get(null);
                    getRecipes = recipes.getMethod("getRecipes", ItemStack.class);
                    getOutput = Class.forName("thedarkcolour.futuremc.recipe.SimpleRecipe").getMethod("getOutput");
                }
                Object recipes = getRecipes.invoke(instance, input);
                if (!(recipes instanceof List)) {
                    return Collections.emptyList();
                }
                List<ItemStack> results = new ArrayList<>();
                for (Object recipe : (List<?>) recipes) {
                    Object output = getOutput.invoke(recipe);
                    if (output instanceof ItemStack && !((ItemStack) output).isEmpty()) {
                        results.add(((ItemStack) output).copy());
                    }
                }
                return results;
            } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                failed = true;
                SophisticatedCoreLegacy.LOGGER.error("Couldn't read Future MC's {}; the upgrades leave those recipes out", className, e);
                return Collections.emptyList();
            }
        }
    }
}
