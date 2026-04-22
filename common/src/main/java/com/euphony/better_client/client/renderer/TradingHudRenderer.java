package com.euphony.better_client.client.renderer;

import com.euphony.better_client.config.BetterClientConfig;
import com.euphony.better_client.config.option.TradingHudPos;
import com.euphony.better_client.utils.MerchantInfo;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.List;

/**
 * Trading HUD renderer — draws up to 7 merchant offers on-screen while a tradable
 * villager / wandering trader is under the crosshair.
 */
public final class TradingHudRenderer {

    private static final int MAX_VISIBLE_OFFERS = 7;
    private static final int HUD_WIDTH = 90;
    private static final int OFFER_HEIGHT = 20;
    private static final int EDGE_MARGIN = 5;
    private static final int REAL_COST_EXTRA_SPACE = 15;

    private static final ResourceLocation TRADE_ARROW_OUT_OF_STOCK_SPRITE =
            ResourceLocation.withDefaultNamespace("container/villager/trade_arrow_out_of_stock");
    private static final ResourceLocation TRADE_ARROW_SPRITE =
            ResourceLocation.withDefaultNamespace("container/villager/trade_arrow");
    private static final ResourceLocation DISCOUNT_STRIKETHROUGH_SPRITE =
            ResourceLocation.withDefaultNamespace("container/villager/discount_strikethrough");

    private TradingHudRenderer() {}

    public static void renderHud(GuiGraphics graphics, DeltaTracker deltaTracker) {
        BetterClientConfig config = BetterClientConfig.HANDLER.instance();
        if (!config.enableTradingHud) return;

        MerchantInfo merchantInfo = MerchantInfo.INSTANCE;
        if (merchantInfo.getLastEntityId().isEmpty()) return;

        MerchantOffers offers = merchantInfo.getOffers();
        if (offers.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        Font font = minecraft.font;
        List<String> enchantmentTexts = merchantInfo.getOfferEnchantmentTexts();

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int visibleOffers = Math.min(offers.size(), MAX_VISIBLE_OFFERS);
        int hudHeight = visibleOffers * OFFER_HEIGHT;

        int i;
        int k;
        TradingHudPos pos = config.tradingHudPos;
        switch (pos) {
            case TOP_LEFT -> {
                i = 0;
                k = EDGE_MARGIN;
            }
            case TOP_RIGHT -> {
                i = screenWidth - HUD_WIDTH;
                k = EDGE_MARGIN;
            }
            case BOTTOM_LEFT -> {
                i = 0;
                k = screenHeight - hudHeight - EDGE_MARGIN;
            }
            case BOTTOM_RIGHT -> {
                i = screenWidth - HUD_WIDTH;
                k = screenHeight - hudHeight - EDGE_MARGIN;
            }
            default -> {
                i = config.tradingHudXOffset;
                k = EDGE_MARGIN + config.tradingHudYOffset;
            }
        }

        int costAStartX = i + EDGE_MARGIN + EDGE_MARGIN;
        int extraSpace = config.renderRealCostDirectly ? 0 : REAL_COST_EXTRA_SPACE;

        int rendered = 0;
        for (MerchantOffer offer : offers) {
            if (rendered >= MAX_VISIBLE_OFFERS) {
                break;
            }

            ItemStack baseCostA = offer.getBaseCostA();
            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();
            ItemStack result = offer.getResult();

            int rowY = k + 2;

            renderAndDecorateCostA(graphics, font, costA, baseCostA, costAStartX, rowY, config.renderRealCostDirectly);

            if (!costB.isEmpty()) {
                int costBX = i + EDGE_MARGIN + 25 + extraSpace;
                graphics.renderFakeItem(costB, costBX, rowY);
                graphics.renderItemDecorations(font, costB, costBX, rowY);
            }

            renderTradeArrow(graphics, offer, i, rowY);

            int resultX = i + EDGE_MARGIN + 58 + extraSpace;
            graphics.renderFakeItem(result, resultX, rowY);
            graphics.renderItemDecorations(font, result, resultX, rowY);

            if (rendered < enchantmentTexts.size()) {
                String enchantmentText = enchantmentTexts.get(rendered);
                if (!enchantmentText.isEmpty()) {
                    graphics.drawString(font, enchantmentText, i + 85 + extraSpace, rowY + 3, CommonColors.WHITE);
                }
            }

            k += OFFER_HEIGHT;
            rendered++;
        }
    }

    private static void renderTradeArrow(GuiGraphics graphics, MerchantOffer offer, int posX, int posY) {
        int extraSpace = BetterClientConfig.HANDLER.instance().renderRealCostDirectly ? 0 : REAL_COST_EXTRA_SPACE;
        ResourceLocation sprite = offer.isOutOfStock() ? TRADE_ARROW_OUT_OF_STOCK_SPRITE : TRADE_ARROW_SPRITE;
        graphics.blitSprite(sprite, posX + EDGE_MARGIN + 25 + 20 + extraSpace, posY + 3, 10, 9);
    }

    private static void renderAndDecorateCostA(
            GuiGraphics graphics,
            Font font,
            ItemStack realCost,
            ItemStack baseCost,
            int x,
            int y,
            boolean renderRealCostDirectly) {
        graphics.renderFakeItem(realCost, x, y);
        if (baseCost.getCount() == realCost.getCount() || renderRealCostDirectly) {
            graphics.renderItemDecorations(font, realCost, x, y);
        } else {
            graphics.renderItemDecorations(font, baseCost, x, y, baseCost.getCount() == 1 ? "1" : null);
            graphics.renderItemDecorations(font, realCost, x + 14, y, realCost.getCount() == 1 ? "1" : null);
            graphics.blitSprite(DISCOUNT_STRIKETHROUGH_SPRITE, x + 7, y + 12, 9, 2);
        }
    }
}
