package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IOptions;
import com.euphony.better_client.config.BetterClientConfig;
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

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Final private SoundManager soundManager;

    @Shadow @Final public Options options;

    @Inject(method = "pauseGame",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
                    ordinal = 0))
    private void onPauseGame(boolean bl, CallbackInfo ci) {
        if(BetterClientConfig.HANDLER.instance().enableMusicPause && ((IOptions)this.options).enc_vanilla$pauseMusic().get()) {
            if(BetterClientConfig.HANDLER.instance().pauseUiSound) {
                this.soundManager.pauseAllExcept();
            } else {
                this.soundManager.pauseAllExcept(SoundSource.UI);
            }
        }
    }
}
