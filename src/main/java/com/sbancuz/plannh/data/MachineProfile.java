package com.sbancuz.plannh.data;

import java.util.List;
import java.util.Map;

public record MachineProfile(String id, String displayName, List<SettingDef<?>> settings,
    EffectComputer effectComputer) {

    @FunctionalInterface
    public interface EffectComputer {

        EffectResult compute(Map<String, Object> settings, RecipeContext ctx);
    }

    public record EffectResult(int durationTicks, long consumptionEUt, int throughputFactor) {}

    public record RecipeContext(long recipeEUt, int recipeDuration) {}

    public static int getInt(Map<String, Object> s, String key, int def) {
        Object v = s.get(key);
        return v instanceof Number n ? n.intValue() : def;
    }

    public static boolean getBool(Map<String, Object> s, String key, boolean def) {
        Object v = s.get(key);
        return v instanceof Boolean b ? b : def;
    }

    public static String getString(Map<String, Object> s, String key, String def) {
        Object v = s.get(key);
        return v instanceof String str ? str : def;
    }

    public static long tierNameToVoltage(String name) {
        if (name == null || name.equals("OFF")) return 0;
        String[] names = { "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "UHV", "UEV", "UIV", "UMV", "UXV",
            "MAX" };
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) return 8L * (long) Math.pow(4, i);
        }
        return 0;
    }

    public static String voltageToTierName(long voltage) {
        if (voltage <= 0) return "";
        int tier = (int) Math.round(Math.log(voltage / 8.0) / Math.log(4));
        String[] names = { "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "UHV", "UEV", "UIV", "UMV", "UXV",
            "MAX" };
        return tier >= 0 && tier < names.length ? names[tier] : "T" + tier;
    }
}
