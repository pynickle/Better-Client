package com.euphony.better_client.mixin;

import com.euphony.better_client.config.BetterClientConfig;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
    @ModifyVariable(method = "tryApplyNewDataPacks", at = @At("HEAD"), argsOnly = true)
    private boolean applyNewDataPacks(boolean bl) {
        if(BetterClientConfig.HANDLER.instance().enableNoExperimentalWarning) {
            return false;
        }
        return bl;
    }
}
