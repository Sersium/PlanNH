package com.sbancuz.plannh.data.extractors;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sbancuz.plannh.Compat;
import com.sbancuz.plannh.api.RecipePropertyAPI;
import com.sbancuz.plannh.data.FlowchartNode;
import com.sbancuz.plannh.data.MachineProfile;
import com.sbancuz.plannh.data.RecipeHandlerAccess;
import com.sbancuz.plannh.data.RecipeProperty;
import com.sbancuz.plannh.data.RecipePropertyExtractor;
import com.sbancuz.plannh.data.SettingDef;

import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import crazypants.enderio.nei.AlloySmelterRecipeHandler.AlloySmelterRecipe;
import crazypants.enderio.nei.SagMillRecipeHandler.MillRecipe;
import crazypants.enderio.nei.SliceAndSpliceRecipeHandler.SliceAndSpliceRecipe;
import crazypants.enderio.nei.SoulBinderRecipeHandler.SoulBinderRecipeNEI;
import crazypants.enderio.nei.VatRecipeHandler.InnerVatRecipe;
import it.unimi.dsi.fastutil.objects.ObjectFloatImmutablePair;

public class EnderIOExtractor implements RecipePropertyExtractor {

    public static final RecipeProperty<Integer> RF_TOTAL = RecipeProperty.intProperty("rfTotal", "RF Total", 0);
    public static final RecipeProperty<Integer> EXPERIENCE = RecipeProperty.intProperty("experience", "Experience", 0);

    private static Field MILL_OUTPUT_CHANCE;

    @Override
    public String getModId() {
        return Compat.ENDERIO.modid;
    }

    @Override
    public void register() {
        RecipePropertyAPI.registerExtractor(this);
        RecipePropertyAPI.registerProperty(RF_TOTAL);
        RecipePropertyAPI.registerProperty(EXPERIENCE);

        Field f = null;
        try {
            f = MillRecipe.class.getDeclaredField("outputChance");
            f.setAccessible(true);
        } catch (Exception ignored) {}
        MILL_OUTPUT_CHANCE = f;
    }

    @Override
    public String getProfileId(IRecipeHandler handler, int recipeIndex) {
        return "enderio";
    }

    @Override
    public boolean canHandle(String recipeOwner) {
        return recipeOwner != null && (recipeOwner.startsWith("EnderIO") || recipeOwner.equals("EIOEnchanter"));
    }

    @Override
    public Map<RecipeProperty<?>, Object> extract(FlowchartNode node, IRecipeHandler handler, int recipeIndex) {
        Map<RecipeProperty<?>, Object> props = new HashMap<>();
        if (!(handler instanceof TemplateRecipeHandler trh)) return props;

        List<TemplateRecipeHandler.CachedRecipe> recipes = RecipeHandlerAccess.getArecipes(trh);
        if (recipeIndex < 0 || recipeIndex >= recipes.size()) return props;

        TemplateRecipeHandler.CachedRecipe cached = recipes.get(recipeIndex);

        if (cached instanceof AlloySmelterRecipe r) {
            props.put(RF_TOTAL, r.getEnergy());
        } else if (cached instanceof MillRecipe r) {
            props.put(RF_TOTAL, r.getEnergy());
            if (MILL_OUTPUT_CHANCE != null) {
                try {
                    float[] chances = (float[]) MILL_OUTPUT_CHANCE.get(r);
                    for (int i = 0; i < chances.length; i++) {
                        node.outputs.set(
                            i,
                            new ObjectFloatImmutablePair<>(
                                node.outputs.get(i)
                                    .left(),
                                chances[i]));
                    }
                } catch (Exception ignored) {}
            }
        } else if (cached instanceof SliceAndSpliceRecipe r) {
            props.put(RF_TOTAL, r.getEnergy());
        } else if (cached instanceof SoulBinderRecipeNEI r) {
            props.put(RF_TOTAL, r.getEnergy());
            if (r.getExperience() > 0) props.put(EXPERIENCE, r.getExperience());
        } else if (cached instanceof InnerVatRecipe r) {
            props.put(RF_TOTAL, r.getEnergy());
        }

        return props;
    }

    private static MachineProfile enderIOProfile() {
        return new MachineProfile(
            "enderio",
            "EnderIO",
            List.of(
                SettingDef.intDef("speed", "Speed", 100, 10, 10000),
                SettingDef.intDef("parallels", "Par", 1, 1, 4096),
                SettingDef.intDef("machines", "Mach", 1, 1, 4096)),
            EnderIOExtractor::enderIOEffect);
    }

    private static MachineProfile.EffectResult enderIOEffect(Map<String, Object> s, MachineProfile.RecipeContext ctx) {
        int speed = MachineProfile.getInt(s, "speed", 100);
        int parallels = MachineProfile.getInt(s, "parallels", 1);
        int machines = MachineProfile.getInt(s, "machines", 1);
        int duration = Math.max(1, Math.round(ctx.recipeDuration() * 100.0f / speed));
        return new MachineProfile.EffectResult(duration, ctx.recipeEUt(), parallels * machines);
    }
}
