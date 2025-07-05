package com.euphony.better_client.api;

import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Unique;

public interface IClientSideMerchant {
    @Unique
    MerchantOffers better_client$getClientUnlockedTrades();

    @Unique
    void better_client$setClientUnlockedTrades(MerchantOffers offers);

}
