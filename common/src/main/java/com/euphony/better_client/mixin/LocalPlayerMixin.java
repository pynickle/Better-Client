package com.euphony.better_client.mixin;

import com.euphony.better_client.client.events.TradingHudEvent;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.euphony.better_client.BetterClient.config;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(at = @At("HEAD"), method = "closeContainer")
    public void onCloseContainer(CallbackInfo ci) {
        if (!config.enableTradingHud) return;
        TradingHudEvent.setWindowOpen(false);
    }
}
