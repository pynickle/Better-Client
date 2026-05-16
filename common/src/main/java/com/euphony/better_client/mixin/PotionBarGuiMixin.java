package com.euphony.better_client.mixin;

import com.euphony.better_client.client.renderer.PotionBarRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class PotionBarGuiMixin {
    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void better_client$hideVanillaEffectHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (PotionBarRenderer.shouldHideVanillaEffectHud()) {
            ci.cancel();
        }
    }
}
