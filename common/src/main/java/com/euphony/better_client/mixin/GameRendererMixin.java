package com.euphony.better_client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

import static com.euphony.better_client.BetterClient.config;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Shadow protected abstract void takeAutoScreenshot(Path path);

    @Shadow private boolean hasWorldScreenshot;

    @ModifyReturnValue(method = "getNightVisionScale", at = @At("RETURN"))
    private static float getNightVisionScaleModify(float original, LivingEntity livingEntity, float pNanoTime) {
        if(!config.enableFadingNightVision) return original;

        int fadingOutTicks = (int) (config.fadingOutDuration * 20);
        MobEffectInstance mobeffectinstance = livingEntity.getEffect(MobEffects.NIGHT_VISION);
        if (mobeffectinstance != null) {
            return !mobeffectinstance.endsWithin(fadingOutTicks) ? 1.0F : (1f / fadingOutTicks * (mobeffectinstance.getDuration() - pNanoTime));
        } else {
            return 1.0F;
        }
    }

    @Inject(
            method = "tryTakeScreenshotIfNeeded",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onTryTakeScreenshotIfNeeded(CallbackInfo ci) {
        if(!config.enableWorldIconUpdate) return;

        // Only run on integrated server
        if (this.minecraft.isLocalServer()) {
            IntegratedServer server = this.minecraft.getSingleplayerServer();
            if (server != null && !server.isStopped()) {
                server.getWorldScreenshotFile().ifPresent(path -> {
                    // Force take new screenshot
                    this.takeAutoScreenshot(path);
                    // Mark as having a screenshot to prevent further attempts
                    this.hasWorldScreenshot = true;
                });
            }
            // Cancel original method to prevent duplicate processing
            ci.cancel();
        }
    }
}
