package com.euphony.better_client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.euphony.better_client.BetterClient.LOGGER;
import static com.euphony.better_client.BetterClient.config;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private boolean better_client$forceWorldIconRefresh = true;

    @Unique
    private long better_client$nextWorldIconRefreshAttemptAt;

    @ModifyReturnValue(method = "getNightVisionScale", at = @At("RETURN"))
    private static float getNightVisionScaleModify(float original, LivingEntity camera, float a) {
        if (!config.enableFadingNightVision) return original;

        int fadingOutTicks = (int) (config.fadingOutDuration * 20);
        MobEffectInstance mobeffectinstance = camera.getEffect(MobEffects.NIGHT_VISION);
        if (mobeffectinstance != null) {
            return !mobeffectinstance.endsWithin(fadingOutTicks)
                    ? 1.0F
                    : (1f / fadingOutTicks * (mobeffectinstance.getDuration() - a));
        } else {
            return 1.0F;
        }
    }

    @Inject(method = "tryTakeScreenshotIfNeeded", at = @At("HEAD"), cancellable = true)
    private void better_client$refreshWorldIconOncePerSession(CallbackInfo ci) {
        if (!config.enableWorldIconUpdate || !this.minecraft.isLocalServer() || !this.better_client$forceWorldIconRefresh) {
            return;
        }

        if (Util.getMillis() < this.better_client$nextWorldIconRefreshAttemptAt) {
            ci.cancel();
            return;
        }

        IntegratedServer server = this.minecraft.getSingleplayerServer();
        if (server == null || server.isStopped()) {
            return;
        }

        server.getWorldScreenshotFile().ifPresent(path -> this.better_client$refreshWorldIconFile(path, ci));
    }

    @Unique
    private void better_client$refreshWorldIconFile(Path path, CallbackInfo ci) {
        if (!Files.isRegularFile(path)) {
            this.better_client$forceWorldIconRefresh = false;
            this.better_client$nextWorldIconRefreshAttemptAt = 0L;
            return;
        }

        try {
            Files.deleteIfExists(path);
            this.better_client$forceWorldIconRefresh = false;
            this.better_client$nextWorldIconRefreshAttemptAt = 0L;
        } catch (IOException exception) {
            LOGGER.warn("Couldn't refresh world icon before auto screenshot", exception);
            this.better_client$nextWorldIconRefreshAttemptAt = Util.getMillis() + 1000L;
            ci.cancel();
        }
    }

    @Inject(method = "resetData", at = @At("TAIL"))
    private void better_client$resetWorldIconRefreshState(CallbackInfo ci) {
        this.better_client$forceWorldIconRefresh = true;
        this.better_client$nextWorldIconRefreshAttemptAt = 0L;
    }
}
