package com.sbancuz.plannh;

import com.sbancuz.plannh.data.PropertyProvider;
import com.sbancuz.plannh.data.provider.AE2Provider;
import com.sbancuz.plannh.data.provider.BotaniaProvider;
import com.sbancuz.plannh.data.provider.EnderIOProvider;
import com.sbancuz.plannh.data.provider.ForestryProvider;
import com.sbancuz.plannh.data.provider.GTProvider;
import com.sbancuz.plannh.data.provider.ThaumcraftProvider;

import cpw.mods.fml.common.Loader;

public enum Compat {

    GREGTECH("gregtech", new GTProvider()),
    ENDERIO("EnderIO", new EnderIOProvider()),
    THAUMCRAFT("Thaumcraft", new ThaumcraftProvider()),
    BOTANIA("Botania", new BotaniaProvider()),
    FORESTRY("Forestry", new ForestryProvider()),
    AE2("appliedenergistics2", new AE2Provider())
    //
    ;

    public final String modid;
    public final boolean isLoaded;
    public final PropertyProvider extractor;

    Compat(String modid, PropertyProvider extractor) {
        this.modid = modid;
        this.isLoaded = Loader.isModLoaded(modid);
        this.extractor = extractor;
    }

    public static void init() {
        for (var mod : values()) {
            if (mod.isLoaded) {
                mod.extractor.register();
            }
        }
    }
}
