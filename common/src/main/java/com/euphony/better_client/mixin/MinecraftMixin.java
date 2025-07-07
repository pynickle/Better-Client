package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
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
        if(config.enableMusicPause && ((IOptions)this.options).better_client$pauseMusic().get()) {
            if(config.pauseUiSound) {
                this.soundManager.pauseAllExcept();
            } else {
                this.soundManager.pauseAllExcept(SoundSource.UI);
            }
        }
    }
}
