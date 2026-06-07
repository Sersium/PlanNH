package com.sbancuz.plannh.data;

import java.util.Map;

import codechicken.nei.recipe.IRecipeHandler;

public interface RecipePropertyExtractor {

    String getModId();

    boolean canHandle(String recipeOwner);

    Map<RecipeProperty<?>, Object> extract(IRecipeHandler handler, int recipeIndex);
}
