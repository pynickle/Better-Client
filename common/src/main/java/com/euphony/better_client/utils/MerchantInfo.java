package com.euphony.better_client.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Merchant info singleton. Tracks the last merchant entity interacted with,
 * its offers, and pre-built enchantment description texts for each offer.
 */
public class MerchantInfo {
    public static final MerchantInfo INSTANCE = new MerchantInfo();

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
     * Check whether the given entity id refers to the same merchant we last tracked.
     */
    public boolean isSameEntity(int entityId) {
        return getLastEntityId().map(id -> id == entityId).orElse(false);
    }

    /**
     * Reset all tracked merchant data.
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
            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(result);
            if (enchantments.isEmpty()) {
                enchantmentTexts.add("");
                continue;
            }

            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
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
