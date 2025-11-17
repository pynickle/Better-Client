package com.euphony.better_client.utils.data;

import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 商人信息管理类，使用单例模式
 */
public class MerchantInfo {
    private static final MerchantInfo INSTANCE = new MerchantInfo();

    @Nullable
    private Integer lastEntityId;

    @NotNull
    private MerchantOffers offers = new MerchantOffers();

    private MerchantInfo() {}

    public static MerchantInfo getInstance() {
        return INSTANCE;
    }

    @NotNull
    public MerchantOffers getOffers() {
        return this.offers;
    }

    public void setOffers(@NotNull MerchantOffers offers) {
        this.offers = offers;
    }

    public Optional<Integer> getLastEntityId() {
        return Optional.ofNullable(this.lastEntityId);
    }

    public void setLastEntityId(@Nullable Integer entityId) {
        this.lastEntityId = entityId;
    }

    /**
     * 检查是否是同一个实体
     * @param entityId 实体 ID
     * @return 如果是同一个实体则返回 true
     */
    public boolean isSameEntity(int entityId) {
        return getLastEntityId().map(id -> id == entityId).orElse(false);
    }

    /**
     * 重置商人信息
     */
    public void reset() {
        this.lastEntityId = null;
        this.offers = new MerchantOffers();
    }
}
