package com.euphony.better_client.client.renderer;

import com.euphony.better_client.service.TradingHudLayout;
import com.euphony.better_client.service.TradingHudLayout.Frame;
import com.euphony.better_client.service.TradingHudLayout.PlacedCaption;
import com.euphony.better_client.utils.data.MerchantInfo;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;

import static com.euphony.better_client.BetterClient.config;

public class TradingHudRenderer {
    private static final Identifier TRADE_ARROW_OUT_OF_STOCK_SPRITE =
            Identifier.withDefaultNamespace("container/villager/trade_arrow_out_of_stock");
    private static final Identifier TRADE_ARROW_SPRITE =
            Identifier.withDefaultNamespace("container/villager/trade_arrow");
    private static final Identifier DISCOUNT_STRIKETHRUOGH_SPRITE =
            Identifier.withDefaultNamespace("container/villager/discount_strikethrough");

    public static void renderHud(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!config.enableTradingHud) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        if (!config.enableTradingHudWhileSneaking && player.isShiftKeyDown()) return;

        Font font = minecraft.font;

        MerchantInfo merchantInfo = MerchantInfo.getInstance();
        merchantInfo.getLastEntityId().ifPresent(lastId -> {
            MerchantOffers merchantOffers = merchantInfo.getOffers();
            List<String> enchantmentTexts = merchantInfo.getOfferEnchantmentTexts();
            if (!merchantOffers.isEmpty()) {
                int screenWidth = minecraft.getWindow().getGuiScaledWidth();
                int screenHeight = minecraft.getWindow().getGuiScaledHeight();
                List<String> captions = new ArrayList<>(merchantOffers.size());
                for (int index = 0; index < merchantOffers.size(); index++) {
                    captions.add(index < enchantmentTexts.size() ? enchantmentTexts.get(index) : "");
                }
                Frame frame = TradingHudLayout.layout(new TradingHudLayout.Request(
                        screenWidth,
                        screenHeight,
                        config.tradingHudPos,
                        config.tradingHudXOffset,
                        config.tradingHudYOffset,
                        !config.renderRealCostDirectly,
                        captions,
                        font::width));

                for (int row = 0; row < frame.captions().size(); row++) {
                    MerchantOffer merchantOffer = merchantOffers.get(row);
                    ItemStack baseCost = merchantOffer.getBaseCostA();
                    ItemStack realCost = merchantOffer.getCostA();
                    ItemStack costB = merchantOffer.getCostB();
                    ItemStack result = merchantOffer.getResult();
                    int y = frame.rowY() + row * TradingHudLayout.ROW_STRIDE;
                    extractAndDecorateCostA(guiGraphics, font, realCost, baseCost, frame.costX(), y);
                    if (!costB.isEmpty()) {
                        guiGraphics.fakeItem(costB, frame.secondCostX(), y);
                        guiGraphics.itemDecorations(font, costB, frame.secondCostX(), y);
                    }

                    extractButtonArrows(guiGraphics, merchantOffer, frame.arrowX(), y);
                    guiGraphics.fakeItem(result, frame.resultX(), y);
                    guiGraphics.itemDecorations(font, result, frame.resultX(), y);

                    PlacedCaption caption = frame.captions().get(row);
                    if (!caption.text().isEmpty()) {
                        guiGraphics.text(font, caption.text(), caption.x(), caption.y(), CommonColors.WHITE);
                    }
                }
            }
        });
    }

    private static void extractButtonArrows(
            GuiGraphicsExtractor guiGraphics, MerchantOffer offer, int arrowX, int itemY) {
        Identifier sprite = offer.isOutOfStock() ? TRADE_ARROW_OUT_OF_STOCK_SPRITE : TRADE_ARROW_SPRITE;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, arrowX, itemY + 3, 10, 9);
    }

    private static void extractAndDecorateCostA(
            GuiGraphicsExtractor guiGraphics, Font font, ItemStack realCost, ItemStack baseCost, int x, int y) {
        guiGraphics.fakeItem(realCost, x, y);
        if (baseCost.getCount() == realCost.getCount() || config.renderRealCostDirectly) {
            guiGraphics.itemDecorations(font, realCost, x, y);
        } else {
            guiGraphics.itemDecorations(font, baseCost, x, y, baseCost.getCount() == 1 ? "1" : null);
            guiGraphics.itemDecorations(font, realCost, x + 14, y, realCost.getCount() == 1 ? "1" : null);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DISCOUNT_STRIKETHRUOGH_SPRITE, x + 7, y + 12, 9, 2);
        }
    }
}
