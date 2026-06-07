package com.sbancuz.plannh.data.extractors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sbancuz.plannh.Compat;
import com.sbancuz.plannh.api.RecipePropertyAPI;
import com.sbancuz.plannh.data.FlowchartNode;
import com.sbancuz.plannh.data.MachineProfile;
import com.sbancuz.plannh.data.MachineProfileRegistry;
import com.sbancuz.plannh.data.RecipeHandlerAccess;
import com.sbancuz.plannh.data.RecipeProperty;
import com.sbancuz.plannh.data.RecipePropertyExtractor;
import com.sbancuz.plannh.data.SettingDef;

import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.OverclockCalculator;
import gregtech.common.items.ItemFluidDisplay;
import gregtech.nei.GTNEIDefaultHandler;
import gregtech.nei.GTNEIDefaultHandler.CachedDefaultRecipe;
import it.unimi.dsi.fastutil.objects.ObjectFloatImmutablePair;

public class GTExtractor implements RecipePropertyExtractor {

    public static final RecipeProperty<Integer> SPECIAL_VALUE = RecipeProperty
        .intProperty("specialValue", "Special Value", 0);

    @Override
    public String getModId() {
        return Compat.GREGTECH.modid;
    }

    @Override
    public void register() {
        RecipePropertyAPI.registerExtractor(this);
        RecipePropertyAPI.registerProperty(SPECIAL_VALUE);

        MachineProfileRegistry.register(gtBasicProfile());
        MachineProfileRegistry.register(gtEbfProfile());
        MachineProfileRegistry.register(gtLaserProfile());
        MachineProfileRegistry.register(gtFusionProfile());
    }

    @Override
    public String getProfileId(IRecipeHandler handler, int recipeIndex) {
        if (!(handler instanceof GTNEIDefaultHandler gth)) return null;
        List<TemplateRecipeHandler.CachedRecipe> recipes = RecipeHandlerAccess.getArecipes(gth);
        if (recipeIndex < 0 || recipeIndex >= recipes.size()) return null;
        CachedDefaultRecipe cached = (CachedDefaultRecipe) recipes.get(recipeIndex);
        GTRecipe r = cached.mRecipe;
        if (r == null) return null;
        return r.mSpecialValue > 0 ? "gregtech:ebf" : "gregtech:basic";
    }

    @Override
    public boolean canHandle(String recipeOwner) {
        return recipeOwner != null && recipeOwner.startsWith("gt.recipe");
    }

    @Override
    public Map<RecipeProperty<?>, Object> extract(FlowchartNode node, IRecipeHandler handler, int recipeIndex) {
        Map<RecipeProperty<?>, Object> props = new HashMap<>();
        if (!(handler instanceof GTNEIDefaultHandler gth)) return props;

        List<TemplateRecipeHandler.CachedRecipe> recipes = RecipeHandlerAccess.getArecipes(gth);
        if (recipeIndex < 0 || recipeIndex >= recipes.size()) return props;

        CachedDefaultRecipe cached = (CachedDefaultRecipe) recipes.get(recipeIndex);
        GTRecipe r = cached.mRecipe;
        if (r == null) return props;

        int duration = r.mDuration;
        int eut = r.mEUt;

        props.put(RecipePropertyAPI.DURATION_TICKS, duration);
        props.put(RecipePropertyAPI.EU_PER_TICK, (long) eut);
        props.put(RecipePropertyAPI.TOTAL_EU, (long) eut * duration);

        if (r.mSpecialValue != 0) {
            props.put(SPECIAL_VALUE, r.mSpecialValue);
        }

        if (r.mInputChances != null) {
            for (int i = 0; i < r.mInputs.length; i++) {
                node.inputs.set(
                    i,
                    new ObjectFloatImmutablePair<>(
                        node.inputs.get(i)
                            .left(),
                        r.mInputChances[i] / 10000.0f));
            }
        }
        if (r.mOutputChances != null) {
            for (int i = 0; i < r.mOutputs.length; i++) {
                node.outputs.set(
                    i,
                    new ObjectFloatImmutablePair<>(
                        node.outputs.get(i)
                            .left(),
                        r.mOutputChances[i] / 10000.0f));
            }
        }

        for (int i = 0; i < r.mFluidInputs.length; i++) {
            node.fluidInputs.add(
                i,
                new ObjectFloatImmutablePair<>(
                    r.mFluidInputs[i],
                    r.mFluidInputChances != null ? r.mFluidInputChances[i] / 10000.0f : 1.f));
        }
        for (int i = 0; i < r.mFluidOutputs.length; i++) {
            node.fluidOutputs.add(
                i,
                new ObjectFloatImmutablePair<>(
                    r.mFluidOutputs[i],
                    r.mFluidOutputChances != null ? r.mFluidOutputChances[i] / 10000.0f : 1.f));
        }

        node.inputs.removeIf(
            p -> p.left()
                .getItem() instanceof ItemFluidDisplay);
        node.outputs.removeIf(
            p -> p.left()
                .getItem() instanceof ItemFluidDisplay);

        return props;
    }

    // ── shared setting list builders ──

    private static List<SettingDef<?>> gtBaseSettings() {
        List<SettingDef<?>> list = new ArrayList<>();
        list.add(
            SettingDef.enumDef(
                "voltage",
                "Tier",
                "OFF",
                List.of(
                    "OFF",
                    "ULV",
                    "LV",
                    "MV",
                    "HV",
                    "EV",
                    "IV",
                    "LuV",
                    "ZPM",
                    "UV",
                    "UHV",
                    "UEV",
                    "UIV",
                    "UMV",
                    "UXV",
                    "MAX")));
        list.add(SettingDef.intDef("amp", "Amp", 1, 1, 64));
        list.add(SettingDef.intDef("speed", "Speed", 100, 10, 10000));
        list.add(SettingDef.intDef("parallels", "Par", 1, 1, 4096));
        list.add(SettingDef.intDef("machines", "Mach", 1, 1, 4096));
        list.add(SettingDef.boolDef("perfectOC", "Perfect OC", false));
        return list;
    }

    private static List<SettingDef<?>> gtAdvancedSettings() {
        List<SettingDef<?>> list = new ArrayList<>();
        list.add(SettingDef.boolDef("laserOC", "Laser OC", false));
        list.add(SettingDef.intDef("eutDiscount", "EU Disc.%", 0, 0, 100));
        list.add(SettingDef.intDef("eutIncreasePerOC", "EU%/OC", 400, 100, 1000));
        list.add(SettingDef.intDef("durationDecreasePerOC", "Spd%/OC", 200, 100, 1000));
        list.add(SettingDef.intDef("maxOverclocks", "Max OC", 0, 0, 64));
        list.add(SettingDef.intDef("maxRegularOc", "Max Reg OC", 0, 0, 64));
        list.add(SettingDef.intDef("maxTierSkips", "Max Skips", 0, 0, 10));
        list.add(SettingDef.boolDef("unlimitedSkips", "Unl. Skips", false));
        list.add(SettingDef.boolDef("noOverclock", "No OC", false));
        return list;
    }

    // ── GT helpers ──

    private static OverclockCalculator buildGtCalc(Map<String, Object> s, MachineProfile.RecipeContext ctx) {
        long voltage = MachineProfile.tierNameToVoltage(MachineProfile.getString(s, "voltage", "OFF"));
        long amp = MachineProfile.getInt(s, "amp", 1);
        int speed = MachineProfile.getInt(s, "speed", 100);
        int parallels = MachineProfile.getInt(s, "parallels", 1);

        OverclockCalculator calc = new OverclockCalculator().setRecipeEUt(ctx.recipeEUt())
            .setEUt(voltage)
            .setDuration(ctx.recipeDuration())
            .setAmperage(amp)
            .setDurationModifier(100.0 / speed)
            .setParallel(parallels)
            .setAmperageOC(true);

        if (MachineProfile.getBool(s, "perfectOC", false)) calc.enablePerfectOC();
        if (MachineProfile.getBool(s, "laserOC", false)) calc.setLaserOC(true);
        if (MachineProfile.getBool(s, "noOverclock", false)) calc.setNoOverclock(true);

        int eutDisc = MachineProfile.getInt(s, "eutDiscount", 0);
        if (eutDisc > 0) calc.setEUtDiscount(eutDisc / 100.0);

        int ocMult = MachineProfile.getInt(s, "eutIncreasePerOC", 400);
        if (ocMult != 400) calc.setEUtIncreasePerOC(ocMult / 100.0);

        int durMult = MachineProfile.getInt(s, "durationDecreasePerOC", 200);
        if (durMult != 200) calc.setDurationDecreasePerOC(durMult / 100.0);

        int maxOc = MachineProfile.getInt(s, "maxOverclocks", 0);
        if (maxOc > 0) calc.setMaxOverclocks(maxOc);

        int maxReg = MachineProfile.getInt(s, "maxRegularOc", 0);
        if (maxReg > 0) calc.setMaxRegularOverclocks(maxReg);

        int skips = MachineProfile.getInt(s, "maxTierSkips", 0);
        if (skips > 0) calc.setMaxTierSkips(skips);

        if (MachineProfile.getBool(s, "unlimitedSkips", false)) calc.setUnlimitedTierSkips();

        return calc;
    }

    private static MachineProfile.EffectResult gtFallback(Map<String, Object> s, MachineProfile.RecipeContext ctx, int parallels, int machines) {
        return new MachineProfile.EffectResult(ctx.recipeDuration(), ctx.recipeEUt(), parallels * machines);
    }

    // ── gregtech:basic ──

    private static MachineProfile gtBasicProfile() {
        List<SettingDef<?>> settings = new ArrayList<>();
        settings.addAll(gtBaseSettings());
        settings.addAll(gtAdvancedSettings());
        return new MachineProfile("gregtech:basic", "GT Basic", settings, GTExtractor::gtBasicEffect);
    }

    private static MachineProfile.EffectResult gtBasicEffect(Map<String, Object> s, MachineProfile.RecipeContext ctx) {
        int parallels = MachineProfile.getInt(s, "parallels", 1);
        int machines = MachineProfile.getInt(s, "machines", 1);

        if (ctx.recipeEUt() <= 0 || ctx.recipeDuration() <= 0
            || MachineProfile.getString(s, "voltage", "OFF")
                .equals("OFF")) {
            return gtFallback(s, ctx, parallels, machines);
        }

        OverclockCalculator calc = buildGtCalc(s, ctx);
        calc.calculate();
        return new MachineProfile.EffectResult(calc.getDuration(), calc.getConsumption(), parallels * machines);
    }

    // ── gregtech:ebf ──

    private static MachineProfile gtEbfProfile() {
        List<SettingDef<?>> settings = new ArrayList<>();
        settings.addAll(gtBaseSettings());
        settings.add(SettingDef.intDef("machineHeat", "M. Heat", 0, 0, 100000));
        settings.add(SettingDef.intDef("recipeHeat", "R. Heat", 0, 0, 100000));
        settings.add(SettingDef.boolDef("heatOC", "Heat OC", true));
        settings.add(SettingDef.boolDef("heatDiscount", "Heat Disc.", false));
        settings.add(SettingDef.intDef("heatDiscountMult", "HD Mult.%", 100, 0, 200));
        settings.addAll(gtAdvancedSettings());
        return new MachineProfile("gregtech:ebf", "GT EBF", settings, GTExtractor::gtEbfEffect);
    }

    private static MachineProfile.EffectResult gtEbfEffect(Map<String, Object> s, MachineProfile.RecipeContext ctx) {
        int parallels = MachineProfile.getInt(s, "parallels", 1);
        int machines = MachineProfile.getInt(s, "machines", 1);

        if (ctx.recipeEUt() <= 0 || ctx.recipeDuration() <= 0
            || MachineProfile.getString(s, "voltage", "OFF")
                .equals("OFF")) {
            return gtFallback(s, ctx, parallels, machines);
        }

        OverclockCalculator calc = buildGtCalc(s, ctx);

        int machineHeat = MachineProfile.getInt(s, "machineHeat", 0);
        boolean heatOC = MachineProfile.getBool(s, "heatOC", true);

        if (heatOC && machineHeat > 0) {
            int recipeHeat = MachineProfile.getInt(s, "recipeHeat", 0);
            calc.setHeatOC(true)
                .setRecipeHeat(recipeHeat > 0 ? recipeHeat : machineHeat)
                .setMachineHeat(machineHeat);
            if (MachineProfile.getBool(s, "heatDiscount", false)) calc.setHeatDiscount(true);
            int hdMult = MachineProfile.getInt(s, "heatDiscountMult", 100);
            if (hdMult != 100) calc.setHeatDiscountMultiplier(hdMult / 100.0);
        }

        calc.calculate();
        return new MachineProfile.EffectResult(calc.getDuration(), calc.getConsumption(), parallels * machines);
    }

    // ── gregtech:laser ──

    private static MachineProfile gtLaserProfile() {
        List<SettingDef<?>> settings = new ArrayList<>();
        settings.addAll(gtBaseSettings());
        settings.addAll(gtAdvancedSettings());
        return new MachineProfile("gregtech:laser", "GT Laser", settings, GTExtractor::gtLaserEffect);
    }

    private static MachineProfile.EffectResult gtLaserEffect(Map<String, Object> s, MachineProfile.RecipeContext ctx) {
        int parallels = MachineProfile.getInt(s, "parallels", 1);
        int machines = MachineProfile.getInt(s, "machines", 1);

        if (ctx.recipeEUt() <= 0 || ctx.recipeDuration() <= 0
            || MachineProfile.getString(s, "voltage", "OFF")
                .equals("OFF")) {
            return gtFallback(s, ctx, parallels, machines);
        }

        OverclockCalculator calc = buildGtCalc(s, ctx);
        calc.setLaserOC(true);
        calc.calculate();
        return new MachineProfile.EffectResult(calc.getDuration(), calc.getConsumption(), parallels * machines);
    }

    // ── gregtech:fusion ──

    private static MachineProfile gtFusionProfile() {
        List<SettingDef<?>> settings = new ArrayList<>();
        settings.addAll(gtBaseSettings());
        settings.add(SettingDef.boolDef("perfectOC", "Perfect OC", true));
        settings.addAll(gtAdvancedSettings());
        return new MachineProfile("gregtech:fusion", "GT Fusion", settings, GTExtractor::gtFusionEffect);
    }

    private static MachineProfile.EffectResult gtFusionEffect(Map<String, Object> s, MachineProfile.RecipeContext ctx) {
        int parallels = MachineProfile.getInt(s, "parallels", 1);
        int machines = MachineProfile.getInt(s, "machines", 1);

        if (ctx.recipeEUt() <= 0 || ctx.recipeDuration() <= 0
            || MachineProfile.getString(s, "voltage", "OFF")
                .equals("OFF")) {
            return gtFallback(s, ctx, parallels, machines);
        }

        OverclockCalculator calc = buildGtCalc(s, ctx);
        // Fusion always uses perfect-OC-like scaling
        calc.enablePerfectOC();
        calc.calculate();
        return new MachineProfile.EffectResult(calc.getDuration(), calc.getConsumption(), parallels * machines);
    }

}
