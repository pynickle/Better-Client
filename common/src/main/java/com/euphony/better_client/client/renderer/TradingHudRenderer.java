package com.euphony.better_client.client.renderer;

import com.euphony.better_client.utils.data.MerchantInfo;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import static com.euphony.better_client.BetterClient.config;

public class TradingHudRenderer {
    protected static final int imageWidth = 176;
    protected static final int imageHeight = 166;

    private static final ResourceLocation TRADE_ARROW_OUT_OF_STOCK_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/trade_arrow_out_of_stock");
    private static final ResourceLocation TRADE_ARROW_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/trade_arrow");
    private static final ResourceLocation DISCOUNT_STRIKETHRUOGH_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/discount_strikethrough");

    public static void renderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!config.enableTradingHud)
            return;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null)
            return;

        Font font = minecraft.font;

        MerchantInfo.getInstance().getLastEntityId().ifPresent(lastId -> {
            MerchantOffers merchantOffers = MerchantInfo.getInstance().getOffers();
            if (!merchantOffers.isEmpty()) {
                int i = 0;
                int k = 5;
                int l = i + 5 + 5;
                int m = 0;

                for(MerchantOffer merchantOffer : merchantOffers) {
                    if (m < 7) {
                        ItemStack itemStack = merchantOffer.getBaseCostA();
                        ItemStack itemStack2 = merchantOffer.getCostA();
                        ItemStack itemStack3 = merchantOffer.getCostB();
                        ItemStack itemStack4 = merchantOffer.getResult();
                        int n = k + 2;
                        renderAndDecorateCostA(guiGraphics, font, itemStack2, itemStack, l, n);
                        if (!itemStack3.isEmpty()) {
                            guiGraphics.renderFakeItem(itemStack3, i + 5 + 35, n);
                            guiGraphics.renderItemDecorations(font, itemStack3, i + 5 + 35, n);
                        }

                        renderButtonArrows(guiGraphics, merchantOffer, i, n);
                        guiGraphics.renderFakeItem(itemStack4, i + 5 + 68, n);
                        guiGraphics.renderItemDecorations(font, itemStack4, i + 5 + 68, n);
                        k += 20;
                        ++m;
                    } else {
                        ++m;
                    }
                }
            }
        });

        /*
        MerchantOffers merchantOffers = ((MerchantMenu)this.menu).getOffers();


        var modelMatrices = context.getMatrices();

        modelMatrices.pushMatrix();

        modelMatrices.translate(config.offsetX, config.offsetY, new Matrix3x2f(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f));

        modelMatrices.pushMatrix();
        modelMatrices.scale(config.scale, config.scale, new Matrix3x2f(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f));

        MerchantInfo.getInfo().getLastId().ifPresent(lastId -> {
            var offers = MerchantInfo.getInfo().getOffers();
            var i = 0;

            for (TradeOffer offer : offers) {
                var baseX = 0;
                var baseY = 0 + i * 20;

                var firstBuy =
                        offer.getDisplayedFirstBuyItem().copy()
                var secondBuy =
                        offer.getDisplayedSecondBuyItem().copy()
                var sell = offer.getSellItem().copy();

                context.drawItem(firstBuy, baseX, baseY);
                context.drawStackOverlay(textRenderer, firstBuy, baseX, baseY);

                context.drawItem(secondBuy, baseX + 20, baseY);
                context.drawStackOverlay(textRenderer, secondBuy, baseX + 20, baseY);

                context.drawItem(sell, baseX + 53, baseY);
                context.drawStackOverlay(textRenderer, sell, baseX + 53, baseY);

                this.renderArrow(context, offer, baseX + -20, baseY);

                List<String> enchantments = new ArrayList<>();

                var itemEnchantmentsComponent = EnchantmentHelper.getEnchantments(offer.getSellItem());
                if (EnchantmentHelper.hasEnchantments(offer.getSellItem())) {
                    for (var entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
                        var level = entry.getIntValue();
                        enchantments.add(Enchantment.getName(entry.getKey(), level).getString());
                    }
                }

                context.drawTextWithShadow(textRenderer, String.join(", ", enchantments), (baseX + 75), (baseY + 5), Colors.WHITE);
                i += 1;
            }
        });

        modelMatrices.popMatrix();
        modelMatrices.popMatrix();
         */
    }


    private static void renderButtonArrows(GuiGraphics guiGraphics, MerchantOffer merchantOffers, int posX, int posY) {
        if (merchantOffers.isOutOfStock()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TRADE_ARROW_OUT_OF_STOCK_SPRITE, posX + 5 + 35 + 20, posY + 3, 10, 9);
        } else {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TRADE_ARROW_SPRITE, posX + 5 + 35 + 20, posY + 3, 10, 9);
        }

    }

    private static void renderAndDecorateCostA(GuiGraphics guiGraphics, Font font, ItemStack realCost, ItemStack baseCost, int x, int y) {
        guiGraphics.renderFakeItem(realCost, x, y);
        if (baseCost.getCount() == realCost.getCount()) {
            guiGraphics.renderItemDecorations(font, realCost, x, y);
        } else {
            guiGraphics.renderItemDecorations(font, baseCost, x, y, baseCost.getCount() == 1 ? "1" : null);
            guiGraphics.renderItemDecorations(font, realCost, x + 14, y, realCost.getCount() == 1 ? "1" : null);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DISCOUNT_STRIKETHRUOGH_SPRITE, x + 7, y + 12, 9, 2);
        }

    }
}