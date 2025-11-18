package com.euphony.better_client.mixin;

import com.euphony.better_client.config.BetterClientConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyReturnValue(method = "getNightVisionScale", at = @At("RETURN"))
    private static float getNightVisionScaleModify(float original, LivingEntity livingEntity, float pNanoTime) {
        if (!BetterClientConfig.HANDLER.instance().enableFadingNightVision) return original;

        int fadingOutTicks = (int) (BetterClientConfig.HANDLER.instance().fadingOutDuration * 20);
        MobEffectInstance mobeffectinstance = livingEntity.getEffect(MobEffects.NIGHT_VISION);
        if (mobeffectinstance != null) {
            return !mobeffectinstance.endsWithin(fadingOutTicks)
                    ? 1.0F
                    : (1f / fadingOutTicks * (mobeffectinstance.getDuration() - pNanoTime));
        } else {
            return 1.0F;
        }
    }
}
