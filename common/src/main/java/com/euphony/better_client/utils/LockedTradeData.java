package com.euphony.better_client.utils;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LockedTradeData {

    private static final Logger visibleTradersLogger = LoggerFactory.getLogger("Visible Traders");
    private static final int MAX_VILLAGER_LEVEL = 5;

    private List<MerchantOffers> lockedOffers;

    public LockedTradeData(Villager villager) {
        this.lockedOffers = generateTrades(villager);
    }

    private LockedTradeData(List<MerchantOffers> offers) {
        this.lockedOffers = offers != null ? new ArrayList<>(offers) : new ArrayList<>();
    }

    public static @Nullable LockedTradeData constructOrNull(ValueInput valueInput) {
        Optional<List<MerchantOffers>> offers = valueInput.read("LockedOffers", MerchantOffers.CODEC.listOf());
        return offers.map(LockedTradeData::new).orElse(null);
    }

    /**
     * 优化的交易生成逻辑 - 提高性能并增强错误处理
     */
    private static ArrayList<MerchantOffers> generateTrades(Villager villager) {
        if (villager == null) {
            visibleTradersLogger.warn("Attempting to generate trades for null villager");
            return new ArrayList<>();
        }

        MerchantOffers currentOffers = villager.getOffers();
        VillagerData originalData = villager.getVillagerData();
        ArrayList<MerchantOffers> lockedOffers = new ArrayList<>();

        // 记录原始状态以便恢复
        int currentLevel = originalData.level();

        try {
            // 为每个更高级别生成交易
            for (int targetLevel = currentLevel + 1; targetLevel <= MAX_VILLAGER_LEVEL; targetLevel++) {
                // 临时提升村民等级
                villager.setVillagerData(originalData.withLevel(targetLevel));

                int preUpdateSize = currentOffers.size();
                villager.updateTrades();
                int postUpdateSize = currentOffers.size();

                // 提取新增的交易
                int newTradesCount = postUpdateSize - preUpdateSize;
                if (newTradesCount > 0) {
                    MerchantOffers newOffers = new MerchantOffers();
                    // 从末尾提取新交易（避免索引问题）
                    for (int i = 0; i < newTradesCount; i++) {
                        MerchantOffer offer = currentOffers.removeLast();
                        if (isValidOffer(offer)) {
                            newOffers.add(0, offer); // 保持顺序
                        }
                    }

                    if (!newOffers.isEmpty()) {
                        lockedOffers.add(newOffers);
                    }
                } else if (newTradesCount < 0) {
                    // 异常情况：交易数量减少
                    visibleTradersLogger.warn("Trade count decreased unexpectedly for villager at level {}", targetLevel);
                }
            }
        } catch (Exception e) {
            visibleTradersLogger.error("Error generating trades for villager", e);
            // 清理可能的部分状态
            lockedOffers.clear();
        } finally {
            // 确保恢复原始状态
            villager.setVillagerData(originalData);
        }

        return lockedOffers;
    }

    /**
     * 验证交易是否有效
     */
    private static boolean isValidOffer(MerchantOffer offer) {
        return offer != null &&
               !offer.getResult().isEmpty() &&
               !offer.getCostA().isEmpty();
    }

    public void write(ValueOutput valueOutput) {
        if (this.lockedOffers != null && !this.lockedOffers.isEmpty()) {
            valueOutput.store("LockedOffers", MerchantOffers.CODEC.listOf(), lockedOffers);
        }
    }

    public boolean hasNoOffers() {
        return this.lockedOffers == null || this.lockedOffers.isEmpty();
    }

    public MerchantOffers popTradeSet() {
        if (hasNoOffers()) {
            return null;
        }
        return this.lockedOffers.removeFirst();
    }

    /**
     * 构建锁定的交易列表，增强错误处理
     */
    public MerchantOffers buildLockedOffers() {
        MerchantOffers combinedOffers = new MerchantOffers();

        if (hasNoOffers()) {
            return combinedOffers;
        }

        boolean hasInvalidOffer = false;

        for (MerchantOffers levelOffers : this.lockedOffers) {
            if (levelOffers == null) continue;

            for (MerchantOffer offer : levelOffers) {
                if (!isValidOffer(offer)) {
                    hasInvalidOffer = true;
                    visibleTradersLogger.warn("Detected invalid trade offer: {}", offer);
                    continue;
                }
                combinedOffers.add(offer);
            }
        }

        // 如果发现无效交易，重置锁定交易数据
        if (hasInvalidOffer) {
            visibleTradersLogger.warn("Found invalid offers, clearing locked trade data");
            this.lockedOffers = new ArrayList<>();
            return new MerchantOffers();
        }

        return combinedOffers;
    }

    /**
     * 优化的tick方法，改进同步逻辑
     */
    public void tick(Villager villager, Runnable popCallback) {
        if (this.lockedOffers == null) {
            this.lockedOffers = new ArrayList<>();
            return;
        }

        int villagerLevel = villager.getVillagerData().level();
        int requiredSets = MAX_VILLAGER_LEVEL - villagerLevel;
        int currentSets = this.lockedOffers.size();

        // 如果锁定的交易集合过多，移除多余的
        while (currentSets > requiredSets && !this.lockedOffers.isEmpty()) {
            popCallback.run();
            currentSets--;
        }

        // 如果锁定的交易集合不足，重新生成
        if (currentSets < requiredSets) {
            visibleTradersLogger.debug("Rebuilding locked offers: expected {}, got {}", requiredSets, currentSets);
            this.lockedOffers = generateTrades(villager);
        }
    }

    /**
     * 获取锁定交易的总数量
     */
    public int getTotalLockedTradesCount() {
        if (hasNoOffers()) {
            return 0;
        }
        return this.lockedOffers.stream()
                .mapToInt(offers -> offers.size())
                .sum();
    }
}
