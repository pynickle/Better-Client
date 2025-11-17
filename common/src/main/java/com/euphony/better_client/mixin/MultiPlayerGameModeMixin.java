package com.euphony.better_client.mixin;

import com.euphony.better_client.client.events.TradingHudEvent;
import com.euphony.better_client.utils.data.MerchantInfo;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.euphony.better_client.BetterClient.config;

/**
 * 多人游戏模式混入类，用于处理实体交互事件
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    /**
     * 当玩家与实体交互时调用
     * @param player 玩家
     * @param target 目标实体
     * @param hand 交互手
     * @param cir 回调信息
     */
    @Inject(at = @At("HEAD"), method = "interact")
    public void onInteractWithEntity(
            Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!config.enableTradingHud) return;

        if (!(target instanceof Merchant)) {
            return;
        }

        MerchantInfo merchantInfo = MerchantInfo.getInstance();
        merchantInfo.getLastEntityId().ifPresent(lastEntityId -> {
            if (target.getId() == lastEntityId && !merchantInfo.getOffers().isEmpty()) {
                TradingHudEvent.setWindowOpen(true);
            }
        });
    }
}
