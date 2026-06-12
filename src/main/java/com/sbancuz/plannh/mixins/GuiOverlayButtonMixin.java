package com.sbancuz.plannh.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sbancuz.plannh.api.PlanAPI;
import com.sbancuz.plannh.data.flowchart.Graph;
import com.sbancuz.plannh.data.flowchart.Node;
import com.sbancuz.plannh.gui.FlowchartGuiContainer;

import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.Recipe;
import codechicken.nei.recipe.RecipeHandlerRef;

@Mixin(value = GuiOverlayButton.class, remap = false)
public class GuiOverlayButtonMixin {

    @Inject(method = "drawItemOverlay", at = @At("HEAD"), cancellable = true, remap = false)
    private void plannh$onDrawItemOverlay(CallbackInfo ci) {
        GuiOverlayButton self = (GuiOverlayButton) (Object) this;
        if (self.firstGui instanceof FlowchartGuiContainer || isRecipeInGraph(self.handlerRef)) {
            ci.cancel();
        }
    }

    private static boolean isRecipeInGraph(RecipeHandlerRef ref) {
        Recipe.RecipeId currentId = Recipe.RecipeId.of(ref.handler, ref.recipeIndex);
        Graph graph = PlanAPI.getActiveGraph();
        for (Node node : graph.getNodes()) {
            if (currentId.equals(node.recipeId)) {
                return true;
            }
        }
        return false;
    }
}
