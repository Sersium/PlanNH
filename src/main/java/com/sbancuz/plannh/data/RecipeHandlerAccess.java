package com.sbancuz.plannh.data;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import codechicken.nei.recipe.TemplateRecipeHandler;

public class RecipeHandlerAccess {

    private static final Field ARECIPES;

    static {
        Field f = null;
        try {
            f = TemplateRecipeHandler.class.getDeclaredField("arecipes");
            f.setAccessible(true);
        } catch (Exception ignored) {}
        ARECIPES = f;
    }

    @SuppressWarnings("unchecked")
    public static List<TemplateRecipeHandler.CachedRecipe> getArecipes(TemplateRecipeHandler handler) {
        if (ARECIPES != null) {
            try {
                return (List<TemplateRecipeHandler.CachedRecipe>) ARECIPES.get(handler);
            } catch (Exception ignored) {}
        }
        return Collections.emptyList();
    }
}
