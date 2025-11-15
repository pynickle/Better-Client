package com.euphony.better_client.mixin;

import static com.euphony.better_client.BetterClient.config;

import com.euphony.better_client.client.events.TradingHudEvent;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 本地玩家混入类，用于处理容器关闭事件
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    /**
     * 当玩家关闭容器时调用
     * @param ci 回调信息
     */
    @Inject(at = @At("HEAD"), method = "closeContainer")
    public void onCloseContainer(CallbackInfo ci) {
        if (!config.enableTradingHud) return;
        TradingHudEvent.setWindowOpen(false);
    }
}
