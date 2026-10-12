package com.euphony.better_client.service;

import com.euphony.better_client.config.option.TradingHudPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Places one trading HUD icon row and its enchantment captions.
 * Callers supply the screen, the trading HUD anchor, and a text measurer. The layout does not draw.
 */
public final class TradingHudLayout {
    public static final int ROW_STRIDE = 20;

    private static final int SCREEN_MARGIN = 5;
    private static final int CAPTION_GAP = 6;
    private static final int DISCOUNT_SPACE = 15;
    /** Right edge of an item count. {@code GuiGraphicsExtractor#itemCount} draws that edge at {@code x + 17}. */
    private static final int COUNT_RIGHT = 17;
    private static final int RESULT_WIDTH = 16;
    private static final int SECOND_COST_DX = 20;
    private static final int ARROW_DX = 40;
    private static final int RESULT_DX = 53;
    private static final int ITEM_Y = 2;
    private static final int CAPTION_DY = 3;
    private static final int MAX_ROWS = 7;
    private static final int CUSTOM_COST_DX = 10;
    private static final String ELLIPSIS = "...";

    private TradingHudLayout() {}

    public static Frame layout(Request request) {
        int rows = Math.min(request.captions().size(), MAX_ROWS);
        int extra = request.showDiscountSpace() ? DISCOUNT_SPACE : 0;
        int costX = costX(request, extra);
        int resultX = costX + RESULT_DX + extra;
        int rowY = rowOrigin(request, rows) + ITEM_Y;
        boolean captionBeforeCosts = request.position() == TradingHudPos.TOP_RIGHT
                || request.position() == TradingHudPos.BOTTOM_RIGHT;
        int captionRight = costX - CAPTION_GAP;
        int captionLeft = resultX + RESULT_WIDTH + CAPTION_GAP;

        List<PlacedCaption> placed = new ArrayList<>(rows);
        for (int row = 0; row < rows; row++) {
            String raw = request.captions().get(row);
            if (raw == null) {
                raw = "";
            }
            int maxWidth = captionBeforeCosts
                    ? captionRight - SCREEN_MARGIN
                    : request.screenWidth() - SCREEN_MARGIN - captionLeft;
            String fitted = fit(raw, maxWidth, request.textWidth(), captionBeforeCosts);
            int x = captionBeforeCosts ? captionRight - request.textWidth().applyAsInt(fitted) : captionLeft;
            placed.add(new PlacedCaption(x, rowY + CAPTION_DY + row * ROW_STRIDE, fitted));
        }
        return new Frame(
                costX,
                costX + SECOND_COST_DX + extra,
                costX + ARROW_DX + extra,
                resultX,
                rowY,
                List.copyOf(placed));
    }

    private static int costX(Request request, int extra) {
        return switch (request.position()) {
            case TOP_LEFT, BOTTOM_LEFT -> SCREEN_MARGIN;
            case TOP_RIGHT, BOTTOM_RIGHT ->
                    request.screenWidth() - SCREEN_MARGIN - COUNT_RIGHT - RESULT_DX - extra;
            case CUSTOM -> request.xOffset() + CUSTOM_COST_DX;
        };
    }

    private static int rowOrigin(Request request, int rows) {
        return switch (request.position()) {
            case BOTTOM_LEFT, BOTTOM_RIGHT -> request.screenHeight() - rows * ROW_STRIDE - SCREEN_MARGIN;
            case CUSTOM -> SCREEN_MARGIN + request.yOffset();
            case TOP_LEFT, TOP_RIGHT -> SCREEN_MARGIN;
        };
    }

    private static String fit(String text, int maxWidth, ToIntFunction<String> textWidth, boolean ellipsisOnTheLeft) {
        if (text.isEmpty() || maxWidth < 0) {
            return "";
        }
        if (textWidth.applyAsInt(text) <= maxWidth) {
            return text;
        }
        if (textWidth.applyAsInt(ELLIPSIS) > maxWidth) {
            return "";
        }
        int accepted = 0;
        for (int end = 1; end <= text.length(); end++) {
            String candidate = ellipsisOnTheLeft
                    ? ELLIPSIS + text.substring(0, end)
                    : text.substring(0, end) + ELLIPSIS;
            if (textWidth.applyAsInt(candidate) > maxWidth) {
                break;
            }
            accepted = end;
        }
        if (accepted == 0) {
            return ELLIPSIS;
        }
        return ellipsisOnTheLeft ? ELLIPSIS + text.substring(0, accepted) : text.substring(0, accepted) + ELLIPSIS;
    }

    public record Request(
            int screenWidth,
            int screenHeight,
            TradingHudPos position,
            int xOffset,
            int yOffset,
            boolean showDiscountSpace,
            List<String> captions,
            ToIntFunction<String> textWidth) {}

    public record Frame(int costX, int secondCostX, int arrowX, int resultX, int rowY, List<PlacedCaption> captions) {}

    public record PlacedCaption(int x, int y, String text) {}
}
