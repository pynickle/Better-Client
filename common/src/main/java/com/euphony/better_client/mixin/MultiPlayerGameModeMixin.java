package com.euphony.better_client.mixin;

import com.euphony.better_client.client.events.TradingHudEvent;
import com.euphony.better_client.utils.data.MerchantInfo;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.euphony.better_client.BetterClient.config;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Inject(at = @At("HEAD"), method = "interact")
    public void onInteractWithEntity(
            Player player,
            Entity entity,
            EntityHitResult hitResult,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (!config.enableTradingHud) return;

        if (!(entity instanceof Merchant)) {
            return;
        }

        MerchantInfo merchantInfo = MerchantInfo.getInstance();
        merchantInfo.getLastEntityId().ifPresent(lastEntityId -> {
            if (entity.getId() == lastEntityId && !merchantInfo.getOffers().isEmpty()) {
                TradingHudEvent.setWindowOpen(true);
            }
        });
    }
}
