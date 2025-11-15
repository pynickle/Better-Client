package com.euphony.better_client.client.renderer;

import static com.euphony.better_client.BetterClient.config;

import com.euphony.better_client.utils.data.MerchantInfo;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class TradingHudRenderer {
    private static final ResourceLocation TRADE_ARROW_OUT_OF_STOCK_SPRITE =
            ResourceLocation.withDefaultNamespace("container/villager/trade_arrow_out_of_stock");
    private static final ResourceLocation TRADE_ARROW_SPRITE =
            ResourceLocation.withDefaultNamespace("container/villager/trade_arrow");
    private static final ResourceLocation DISCOUNT_STRIKETHRUOGH_SPRITE =
            ResourceLocation.withDefaultNamespace("container/villager/discount_strikethrough");

    public static void renderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!config.enableTradingHud) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        Font font = minecraft.font;

        MerchantInfo.getInstance().getLastEntityId().ifPresent(lastId -> {
            MerchantOffers merchantOffers = MerchantInfo.getInstance().getOffers();
            if (!merchantOffers.isEmpty()) {
                int i = config.tradingHudXOffset;
                int k = 5 + config.tradingHudYOffset;
                int l = i + 5 + 5;
                int m = 0;

                int extraSpace = config.renderRealCostDirectly ? 0 : 10;

                for (MerchantOffer merchantOffer : merchantOffers) {
                    if (m < 7) {
                        ItemStack itemStack = merchantOffer.getBaseCostA();
                        ItemStack itemStack2 = merchantOffer.getCostA();
                        ItemStack itemStack3 = merchantOffer.getCostB();
                        ItemStack itemStack4 = merchantOffer.getResult();
                        int n = k + 2;
                        renderAndDecorateCostA(guiGraphics, font, itemStack2, itemStack, l, n);
                        if (!itemStack3.isEmpty()) {
                            guiGraphics.renderFakeItem(itemStack3, i + 5 + 25 + extraSpace, n);
                            guiGraphics.renderItemDecorations(font, itemStack3, i + 5 + 25 + extraSpace, n);
                        }

                        renderButtonArrows(guiGraphics, merchantOffer, i, n);
                        guiGraphics.renderFakeItem(itemStack4, i + 5 + 58 + extraSpace, n);
                        guiGraphics.renderItemDecorations(font, itemStack4, i + 5 + 58 + extraSpace, n);
                        k += 20;

                        List<String> enchantments = new ArrayList<>();

                        var itemEnchantmentsComponent = EnchantmentHelper.getEnchantmentsForCrafting(itemStack4);
                        if (EnchantmentHelper.hasAnyEnchantments(itemStack4)) {
                            for (var entry : itemEnchantmentsComponent.entrySet()) {
                                var level = entry.getIntValue();
                                enchantments.add(Enchantment.getFullname(entry.getKey(), level)
                                        .getString());
                            }
                        }

                        guiGraphics.drawString(
                                font, String.join(", ", enchantments), (i + 85), (n + 3), CommonColors.WHITE);
                    }
                    ++m;
                }
            }
        });
    }

    private static void renderButtonArrows(GuiGraphics guiGraphics, MerchantOffer merchantOffers, int posX, int posY) {
        if (merchantOffers.isOutOfStock()) {
            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED, TRADE_ARROW_OUT_OF_STOCK_SPRITE, posX + 5 + 25 + 20, posY + 3, 10, 9);
        } else {
            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED, TRADE_ARROW_SPRITE, posX + 5 + 25 + 20, posY + 3, 10, 9);
        }
    }

    private static void renderAndDecorateCostA(
            GuiGraphics guiGraphics, Font font, ItemStack realCost, ItemStack baseCost, int x, int y) {
        guiGraphics.renderFakeItem(realCost, x, y);
        if (baseCost.getCount() == realCost.getCount() || config.renderRealCostDirectly) {
            guiGraphics.renderItemDecorations(font, realCost, x, y);
        } else {
            guiGraphics.renderItemDecorations(font, baseCost, x, y, baseCost.getCount() == 1 ? "1" : null);
            guiGraphics.renderItemDecorations(font, realCost, x + 14, y, realCost.getCount() == 1 ? "1" : null);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DISCOUNT_STRIKETHRUOGH_SPRITE, x + 7, y + 12, 9, 2);
        }
    }
}
