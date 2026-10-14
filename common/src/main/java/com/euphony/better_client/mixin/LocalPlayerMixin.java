package com.euphony.better_client.mixin;

import com.euphony.better_client.client.events.TradingHudEvent;
import com.euphony.better_client.service.QuietClicks;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.euphony.better_client.BetterClient.config;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    // BundleItem calls this override, which uses playLocalSound and bypasses playSeededSound.
    // Cancel before NeoForge's sound event and before either loader allocates a sound channel.
    @Inject(method = "playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", at = @At("HEAD"), cancellable = true)
    private void better_client$silenceSyntheticClicks(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        if (QuietClicks.active()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "closeContainer")
    public void onCloseContainer(CallbackInfo ci) {
        if (!config.enableTradingHud) return;
        TradingHudEvent.setWindowOpen(false);
    }
}
