package com.euphony.better_client.mixin;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.euphony.better_client.BetterClient.config;

@Mixin(SuspiciousStewEffects.class)
public class SuspiciousStewEffectsMixin {
    @Redirect(method = "addToTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/TooltipFlag;isCreative()Z"))
    private boolean isCreative(TooltipFlag instance) {
        if (!config.enableSuspiciousStewTooltip) return instance.isCreative();

        return true;
    }
}
