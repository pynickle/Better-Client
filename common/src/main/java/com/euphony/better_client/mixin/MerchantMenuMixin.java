package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IClientSideMerchant;
import com.euphony.better_client.api.IMerchantMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin implements IMerchantMenu {

    @Shadow private int merchantLevel;
    @Shadow @Final private Merchant trader;

    @Shadow public abstract MerchantOffers getOffers();

    @Unique
    private int better_client$unlockedTradeCount = 0;

    @Unique
    private static final int LEVEL_BITMASK = 0xFF; // 255

    @Unique
    private static final int TRADE_COUNT_SHIFT = 8;

    /**
     * 从传入的整数中读取解锁的交易数量
     */
    @Inject(method = "setMerchantLevel", at = @At("TAIL"))
    private void readUnlockedTradeCountFromLevel(int i, CallbackInfo ci) {
        // 解码交易数量和村民等级
        this.better_client$unlockedTradeCount = i >> TRADE_COUNT_SHIFT;
        this.merchantLevel = i & LEVEL_BITMASK;

        // 如果是客户端商人，则设置客户端解锁的交易
        if (this.trader instanceof IClientSideMerchant clientMerchant) {
            try {
                if (better_client$unlockedTradeCount > 0 && better_client$unlockedTradeCount <= getOffers().size()) {
                    List<MerchantOffer> unlockedTrades = getOffers().subList(0, better_client$unlockedTradeCount);
                    MerchantOffers offers = new MerchantOffers();
                    offers.addAll(unlockedTrades);
                    clientMerchant.better_client$setClientUnlockedTrades(offers);
                } else {
                    // 如果交易数量无效，则清空
                    clientMerchant.better_client$setClientUnlockedTrades(new MerchantOffers());
                }
            } catch (Exception e) {
                // 异常处理
                clientMerchant.better_client$setClientUnlockedTrades(new MerchantOffers());
            }
        }
    }

    /**
     * 检查是否应允许交易
     */
    @Override
    public boolean better_client$shouldAllowTrade(int i) {
        // 如果unlockedTradeCount为0，则允许所有交易（向后兼容）
        if (better_client$unlockedTradeCount == 0) {
            return true;
        }
        // 否则，只允许在解锁范围内的交易
        return i < better_client$unlockedTradeCount;
    }
}

