package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IVillager;
import com.euphony.better_client.utils.LockedTradeData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder, IVillager {

    @Shadow public abstract @NotNull VillagerData getVillagerData();

    @Unique
    private final Mutable<LockedTradeData> better_client$lockedTradeData = new MutableObject<>();

    @Unique
    private int better_client$lastKnownLevel = -1;

    @Unique
    private boolean better_client$isDirty = false;

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * 安全地执行带有LockedTradeData的操作
     */
    @Unique
    private void better_client$ifPresent(Consumer<LockedTradeData> consumer) {
        LockedTradeData data = better_client$lockedTradeData.getValue();
        if (data != null) {
            try {
                consumer.accept(data);
            } catch (Exception e) {
                // 如果出现异常，清理损坏的数据
                better_client$lockedTradeData.setValue(null);
                better_client$isDirty = true;
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveLockedTradeData(ValueOutput valueOutput, CallbackInfo ci) {
        better_client$ifPresent(data -> data.write(valueOutput));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readLockedTradeData(ValueInput valueInput, CallbackInfo ci) {
        try {
            LockedTradeData data = LockedTradeData.constructOrNull(valueInput);
            better_client$lockedTradeData.setValue(data);
            better_client$isDirty = (data == null);
        } catch (Exception e) {
            // 读取失败时清理数据
            better_client$lockedTradeData.setValue(null);
            better_client$isDirty = true;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void removeLockedTradeDataIfNoOffers(CallbackInfo ci) {
        // 如果没有交易，清理锁定数据
        if (this.offers == null || this.offers.isEmpty()) {
            if (better_client$lockedTradeData.getValue() != null) {
                better_client$lockedTradeData.setValue(null);
                better_client$isDirty = false;
            }
            return;
        }

        // 检查村民等级是否变化
        int currentLevel = getVillagerData().level();
        if (better_client$lastKnownLevel != currentLevel) {
            better_client$lastKnownLevel = currentLevel;
            better_client$isDirty = true;
        }

        // 如果数据脏了，重新生成
        if (better_client$isDirty) {
            better_client$regenerateLockedTradeData();
            better_client$isDirty = false;
        }

        // 更新锁定交易数据
        better_client$ifPresent(data -> data.tick((Villager) (Object) this, this::appendLockedOffer));
    }

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void preventAdditionalTradesOnRankIncrease(CallbackInfo ci) {
        // 如果没有交易或交易为空，清理锁定数据
        if (this.offers == null || this.offers.isEmpty()) {
            better_client$lockedTradeData.setValue(null);
            better_client$isDirty = false;
            return;
        }

        // 尝试添加锁定的交易
        if (appendLockedOffer()) {
            ci.cancel();
        }
    }

    /**
     * 优化的锁定交易添加逻辑
     */
    @Unique
    private boolean appendLockedOffer() {
        if (this.offers == null) {
            return false;
        }

        AtomicBoolean result = new AtomicBoolean(false);
        better_client$ifPresent(data -> {
            MerchantOffers dismissedTrades = data.popTradeSet();
            if (dismissedTrades != null && !dismissedTrades.isEmpty()) {
                // 验证交易的有效性
                MerchantOffers validTrades = new MerchantOffers();
                for (var offer : dismissedTrades) {
                    if (offer != null && !offer.getResult().isEmpty()) {
                        validTrades.add(offer);
                    }
                }

                if (!validTrades.isEmpty()) {
                    this.offers.addAll(validTrades);
                    result.set(true);
                }
            }
        });

        return result.get();
    }

    /**
     * 重新生成锁定交易数据
     */
    @Unique
    private void better_client$regenerateLockedTradeData() {
        try {
            better_client$lockedTradeData.setValue(new LockedTradeData((Villager) (Object) this));
        } catch (Exception e) {
            // 生成失败时清理
            better_client$lockedTradeData.setValue(null);
        }
    }

    @Override
    public void better_client$setLockedTradeData(LockedTradeData data) {
        this.better_client$lockedTradeData.setValue(data);
        this.better_client$isDirty = false;
    }

    @Override
    public Optional<LockedTradeData> better_client$getLockedTradeData() {
        return Optional.ofNullable(better_client$lockedTradeData.getValue());
    }

    @Override
    public void visibleTrades$regenerateTrades() {
        better_client$regenerateLockedTradeData();
    }

    /**
     * 优化的等级计算，包含交易数量信息
     */
    @Override
    public int better_client$getShiftedLevel() {
        int level = getVillagerData().level();
        if (this.offers == null) {
            return level;
        }

        // 将交易数量编码到高位
        int tradeCount = this.offers.size();
        return level | (tradeCount << 8);
    }

    /**
     * 获取合并的交易列表（当前+锁定）
     */
    @Override
    public MerchantOffers better_client$getCombinedOffers() {
        MerchantOffers combinedOffers = new MerchantOffers();

        // 添加当前交易
        if (this.offers != null) {
            combinedOffers.addAll(this.offers);
        }

        // 确保锁定数据存在
        if (better_client$lockedTradeData.getValue() == null) {
            better_client$regenerateLockedTradeData();
        }

        // 添加锁定的交易
        better_client$ifPresent(data -> {
            MerchantOffers lockedOffers = data.buildLockedOffers();
            if (lockedOffers != null) {
                combinedOffers.addAll(lockedOffers);
            }
        });

        return combinedOffers;
    }

    /**
     * 检查是否有锁定的交易数据
     */
    @Unique
    public boolean better_client$hasLockedTrades() {
        return better_client$lockedTradeData.getValue() != null &&
               !better_client$lockedTradeData.getValue().hasNoOffers();
    }

    /**
     * 获取锁定交易的总数量
     */
    @Unique
    public int better_client$getLockedTradesCount() {
        return better_client$getLockedTradeData()
                .map(LockedTradeData::getTotalLockedTradesCount)
                .orElse(0);
    }
}
