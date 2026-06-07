package com.sbancuz.plannh.data.extractors;

import java.util.HashMap;
import java.util.Map;

import codechicken.nei.recipe.IRecipeHandler;
import com.sbancuz.plannh.data.RecipeProperty;
import com.sbancuz.plannh.api.RecipePropertyAPI;
import com.sbancuz.plannh.data.RecipePropertyExtractor;

public class VanillaExtractor implements RecipePropertyExtractor {

    @Override
    public String getModId() {
        return "vanilla";
    }

    @Override
    public boolean canHandle(String recipeOwner) {
        return true;
    }

    @Override
    public Map<RecipeProperty<?>, Object> extract(IRecipeHandler handler, int recipeIndex) {
        Map<RecipeProperty<?>, Object> props = new HashMap<>();
        int h = handler.getRecipeHeight(recipeIndex);
        props.put(RecipePropertyAPI.DURATION_TICKS, h > 0 ? h * 20 : 200);
        return props;
    }
}
