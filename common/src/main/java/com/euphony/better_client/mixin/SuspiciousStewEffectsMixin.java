package com.euphony.better_client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.euphony.better_client.BetterClient.config;

@Mixin(SuspiciousStewEffects.class)
public class SuspiciousStewEffectsMixin {
    @ModifyExpressionValue(
            method = "addToTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/TooltipFlag;isCreative()Z"
            )
    )
    private boolean modifyIsCreative(boolean original) {
        if (!config.enableSuspiciousStewTooltip) return original;
        return true;
    }
}
