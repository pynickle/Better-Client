package com.euphony.better_client.api;

import com.euphony.better_client.utils.LockedTradeData;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.Optional;

public interface IVillager {

    static IVillager of(Villager villager) {
        return (IVillager) villager;
    }

    void better_client$setLockedTradeData(LockedTradeData data);

    Optional<LockedTradeData> better_client$getLockedTradeData();

    void visibleTrades$regenerateTrades();

    MerchantOffers better_client$getCombinedOffers();

    int better_client$getShiftedLevel();
}
