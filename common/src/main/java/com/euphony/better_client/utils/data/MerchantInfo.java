package com.euphony.better_client.utils.data;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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

    @NotNull
    private List<String> offerEnchantmentTexts = List.of();

    private MerchantInfo() {}

    public static MerchantInfo getInstance() {
        return INSTANCE;
    }

    @NotNull
    public MerchantOffers getOffers() {
        return this.offers;
    }

    @NotNull
    public List<String> getOfferEnchantmentTexts() {
        return this.offerEnchantmentTexts;
    }

    public void setOffers(@NotNull MerchantOffers offers) {
        this.offers = offers;
        this.offerEnchantmentTexts = buildOfferEnchantmentTexts(offers);
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
        this.offerEnchantmentTexts = List.of();
    }

    private static List<String> buildOfferEnchantmentTexts(MerchantOffers offers) {
        if (offers.isEmpty()) {
            return List.of();
        }

        List<String> enchantmentTexts = new ArrayList<>(offers.size());
        for (var offer : offers) {
            ItemStack result = offer.getResult();
            var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(result);
            if (enchantments.isEmpty()) {
                enchantmentTexts.add("");
                continue;
            }

            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (var entry : enchantments.entrySet()) {
                if (!first) {
                    builder.append(", ");
                }

                builder.append(Enchantment.getFullname(entry.getKey(), entry.getIntValue()).getString());
                first = false;
            }
            enchantmentTexts.add(builder.toString());
        }

        return List.copyOf(enchantmentTexts);
    }
}
