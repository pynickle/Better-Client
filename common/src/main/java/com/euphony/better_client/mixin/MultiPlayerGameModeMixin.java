package com.euphony.better_client.mixin;

import com.euphony.better_client.client.events.TradingHudEvent;
import com.euphony.better_client.config.BetterClientConfig;
import com.euphony.better_client.utils.MerchantInfo;
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

/**
 * 多人游戏模式混入类，用于处理实体交互事件
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    /**
     * 当玩家与实体交互时调用 — 1.21.1 签名: interact(Player, Entity, InteractionHand)
     * @param player 玩家
     * @param entity 目标实体
     * @param hand 交互手
     * @param cir 回调信息
     */
    @Inject(at = @At("HEAD"), method = "interact")
    public void better_client$onInteractWithEntity(
            Player player,
            Entity entity,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (!BetterClientConfig.HANDLER.instance().enableTradingHud) return;

        if (!(entity instanceof Merchant)) {
            return;
        }

        MerchantInfo merchantInfo = MerchantInfo.INSTANCE;
        if (!merchantInfo.isSameEntity(entity.getId())) {
            return;
        }
        if (merchantInfo.getOffers().isEmpty()) {
            return;
        }

        TradingHudEvent.setWindowOpen(true);
    }
}
