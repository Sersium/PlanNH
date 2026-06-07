package com.sbancuz.plannh.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sbancuz.plannh.api.RecipePropertyAPI;
import net.minecraft.item.ItemStack;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

public class FlowchartNode {

    public final UUID id;
    public int x;
    public int y;

    public final List<ItemStack> inputs;
    public final List<ItemStack> outputs;

    public String machineName;
    public int durationTicks;
    public String recipeOwner;
    public int handlerRecipeIndex;

    public final ExtractedProperties properties = new ExtractedProperties();

    public FlowchartNode(IRecipeHandler handler, int recipeIndex, int x, int y) {
        this.id = UUID.randomUUID();
        this.x = x;
        this.y = y;

        this.machineName = handler.getRecipeName()
            .trim();
        String ident = handler.getOverlayIdentifier();
        if (ident != null && !ident.isEmpty()) {
            this.recipeOwner = ident;
            this.handlerRecipeIndex = recipeIndex;
        }

        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();

        List<PositionedStack> ins = handler.getIngredientStacks(recipeIndex);
        for (PositionedStack ps : ins) {
            if (ps != null && ps.item != null && ps.item.stackSize > 0) {
                this.inputs.add(ps.item.copy());
            }
        }

        PositionedStack result = handler.getResultStack(recipeIndex);
        if (result != null && result.item != null) {
            this.outputs.add(result.item.copy());
        }
        List<PositionedStack> others = handler.getOtherStacks(recipeIndex);
        for (PositionedStack ps : others) {
            if (ps != null && ps.item != null) this.outputs.add(ps.item.copy());
        }

        for (RecipePropertyExtractor ex : RecipePropertyAPI.getExtractors()) {
            if (ex.canHandle(this.recipeOwner)) {
                this.properties.putAll(ex.extract(handler, recipeIndex));
            }
        }

        this.durationTicks = this.properties.get(RecipePropertyAPI.DURATION_TICKS);
    }

    /**
     * To be used only for serialization/deserialization
     */
    public FlowchartNode(UUID id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
    }
}
