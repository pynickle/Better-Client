package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IVillager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.euphony.better_client.BetterClient.config;

@Mixin(Merchant.class)
public interface MerchantMixin {

    @WrapOperation(method = "openTradingScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;sendMerchantOffers(ILnet/minecraft/world/item/trading/MerchantOffers;IIZZ)V"))
    private void sendLockedOffersWithNormalOffersOnScreenOpen(Player instance, int syncId, MerchantOffers merchantOffers, int j, int k, boolean bl, boolean bl2, Operation<Void> original) {
        if(!(((Merchant)this) instanceof Villager villager) || !config.enableVisibleTrade) original.call(instance, syncId, merchantOffers, j, k, bl, bl2);
        else {
            IVillager duck = IVillager.of(villager);
            original.call(instance, syncId, duck.better_client$getCombinedOffers(), duck.better_client$getShiftedLevel(), k, bl, bl2);
        }
    }

}