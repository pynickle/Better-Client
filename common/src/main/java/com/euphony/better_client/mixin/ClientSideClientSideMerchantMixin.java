package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IClientSideMerchant;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientSideMerchant.class)
public class ClientSideClientSideMerchantMixin implements IClientSideMerchant {
    @Unique
    private MerchantOffers better_client$clientUnlockedTrades = null;


    @Unique
    @Override
    public MerchantOffers better_client$getClientUnlockedTrades() {
        return better_client$clientUnlockedTrades;
    }

    @Unique
    @Override
    public void better_client$setClientUnlockedTrades(MerchantOffers offers) {
        this.better_client$clientUnlockedTrades = offers;
    }
}
