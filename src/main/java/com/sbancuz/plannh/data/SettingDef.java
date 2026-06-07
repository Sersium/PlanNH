package com.sbancuz.plannh.data;

import java.util.List;

public class SettingDef<T> {

    public final String key;
    public final String label;
    public final Class<T> type;
    public final T defaultValue;
    public final int minInt;
    public final int maxInt;
    public final List<String> options;

    private SettingDef(String key, String label, Class<T> type, T defaultValue, int minInt, int maxInt,
        List<String> options) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.defaultValue = defaultValue;
        this.minInt = minInt;
        this.maxInt = maxInt;
        this.options = options;
    }

    public static SettingDef<Integer> intDef(String key, String label, int def, int min, int max) {
        return new SettingDef<>(key, label, Integer.class, def, min, max, null);
    }

    public static SettingDef<Boolean> boolDef(String key, String label, boolean def) {
        return new SettingDef<>(key, label, Boolean.class, def, 0, 0, null);
    }

    public static SettingDef<String> enumDef(String key, String label, String def, List<String> options) {
        return new SettingDef<>(key, label, String.class, def, 0, 0, options);
    }

    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }
}
