package com.euphony.better_client.mixin;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.euphony.better_client.BetterClient.config;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
    @ModifyVariable(method = "tryApplyNewDataPacks", at = @At("HEAD"), argsOnly = true)
    private boolean applyNewDataPacks(boolean bl) {
        if(config.enableNoExperimentalWarning) {
            return false;
        }
        return bl;
    }
}
