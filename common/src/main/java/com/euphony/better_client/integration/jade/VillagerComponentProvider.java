package com.euphony.better_client.integration.jade;

import com.euphony.better_client.api.IVillager;
import com.euphony.better_client.utils.data.MerchantInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

import java.util.logging.Logger;

public enum VillagerComponentProvider implements IEntityComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(
            ITooltip tooltip,
            EntityAccessor accessor,
            IPluginConfig config
    ) {
        Villager villager = (Villager) accessor.getEntity();
        MerchantOffers merchantOffers = MerchantInfo.getInstance().getOffers();

        Player player = accessor.getPlayer();

        // tooltip.add(Component.translatable("info.better_client.reputation", reputation));

        // 如果有交易数据，显示可能的声望范围
        if (!merchantOffers.isEmpty()) {
            MobEffectInstance effectInstance = player.getEffect(MobEffects.HERO_OF_THE_VILLAGE);
            int amplifier = effectInstance != null ? effectInstance.getAmplifier() : -1;
            int[] range = calculateAllReputationRange(merchantOffers, amplifier);
            tooltip.add(Component.literal("\n估算声望范围: " + range[0] + " ~ " + range[1]));
        }
    }

    private static int[] calculateAllReputationRange(MerchantOffers merchantOffers, int amplifier) {
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        for(MerchantOffer offer : merchantOffers) {
            int[] range = calculateReputationRange(
                    offer.getBaseCostA().getCount(), // 物品A的基础数量
                    offer.getDemand(),               // 需求值
                    offer.getPriceMultiplier(),      // 价格乘数
                    amplifier,
                    offer.getBaseCostA().getMaxStackSize(), // 物品A的最大堆叠数量
                    offer.getCostA().getCount()                  // 实际交易中物品A的数量
            );
            min = Math.max(min, range[0]);
            max = Math.min(max, range[1]);
        }
        return new int[]{min, max};
    }

    public static int[] calculateReputationRange(
            int baseCount,       // 物品A的基础数量
            int demand,          // 需求值
            float priceMultiplier, // 价格乘数
            int amplifier,       // 英雄药水效果等级（无效果时为-1）
            int maxStackSize,    // 物品A的最大堆叠数量
            int actualCount      // 实际交易中物品A的数量
    ) {
        // 1. 计算需求调整值
        int demandAdjustment = Math.max(0, (int) Math.floor((float) (baseCount * demand) * priceMultiplier));
        int totalWithoutSpecial = baseCount + demandAdjustment;

        // 2. 计算英雄效果调整值 (heroPart)
        int heroPart = 0;
        if (amplifier >= 0) {
            double kVal = 0.3 + 0.0625 * amplifier * baseCount;
            int k = (int) Math.floor(kVal);
            heroPart = Math.max(k, 1);
        }

        // 3. 根据实际数量是否在边界上分情况处理
        if (actualCount == 1) {
            // 情况：实际数量=1（达到下限）
            double minR = totalWithoutSpecial - 1 - heroPart;
            int minRep = (int) Math.ceil(minR / priceMultiplier);
            return new int[]{Math.max(-100, minRep), 100}; // 下限确定，上限取最大值
        } else if (actualCount == maxStackSize) {
            // 情况：实际数量=maxStackSize（达到上限）
            double maxR = totalWithoutSpecial - maxStackSize - heroPart;
            double upperBound = (maxR + 1) / priceMultiplier;
            int maxRep = (int) Math.floor(upperBound - 1e-9);
            return new int[]{-100, Math.min(100, maxRep)}; // 上限确定，下限取最小值
        } else {
            // 情况：实际数量在正常范围内
            int specialPriceDiff = actualCount - totalWithoutSpecial;
            int R = -specialPriceDiff - heroPart; // 声望调整值
            int minRep = (int) Math.ceil((double) R / priceMultiplier);
            int maxRep = (int) Math.floor((double) (R + 1) / priceMultiplier - 1e-9);
            minRep = Math.max(-100, minRep); // 限制在-100~100范围内
            maxRep = Math.min(100, maxRep);
            return new int[]{minRep, maxRep};
        }
    }

    @Override
    public ResourceLocation getUid() {
        return JadeConstants.VILLAGER_REPUTATION;
    }
}
