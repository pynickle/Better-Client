package com.euphony.better_client.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractRecipeBookScreen.class)
public interface BundlePanelRecipeBookAccessor {
    @Accessor("recipeBookComponent") RecipeBookComponent<?> better_client$recipeBook();
}
