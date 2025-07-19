package com.euphony.better_client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.euphony.better_client.BetterClient.config;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Final private SoundManager soundManager;

    @Shadow @Final public Options options;

    @Inject(method = "pauseGame",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
                    ordinal = 0))
    private void onPauseGame(boolean bl, CallbackInfo ci) {
        if(config.enableMusicPause) {
            if(config.pauseUiSound) {
                this.soundManager.pauseAllExcept();
            } else {
                this.soundManager.pauseAllExcept(SoundSource.UI);
            }
        }
    }

    @ModifyExpressionValue(
            method = "shouldEntityAppearGlowing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;isCurrentlyGlowing()Z"
            )
    )
    private boolean seenByEyeOfTheForestThenGlow(boolean original, Entity entity) {
        return config.enableGlowingEnderEye ? entity.getType() == EntityType.EYE_OF_ENDER || original : original;
    }
}
