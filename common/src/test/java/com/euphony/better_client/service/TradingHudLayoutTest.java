package com.euphony.better_client.service;

import com.euphony.better_client.config.option.TradingHudPos;
import com.euphony.better_client.service.TradingHudLayout.Frame;
import com.euphony.better_client.service.TradingHudLayout.PlacedCaption;
import com.euphony.better_client.service.TradingHudLayout.Request;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradingHudLayoutTest {
    @Test
    void topLeftPinsTheFirstCostFivePixelsFromTheScreenEdge() {
        Frame frame = layout(400, 300, TradingHudPos.TOP_LEFT, 0, 0, false, List.of(""));

        assertEquals(5, frame.costX());
        assertEquals(25, frame.secondCostX());
        assertEquals(45, frame.arrowX());
        assertEquals(58, frame.resultX());
        assertEquals(7, frame.rowY());
    }

    @Test
    void topRightPinsTheResultCountFivePixelsFromTheScreenEdge() {
        Frame direct = layout(400, 300, TradingHudPos.TOP_RIGHT, 0, 0, false, List.of(""));
        Frame discounted = layout(400, 300, TradingHudPos.TOP_RIGHT, 0, 0, true, List.of(""));

        assertEquals(378, direct.resultX());
        assertEquals(378, discounted.resultX());
        assertEquals(325, direct.costX());
        assertEquals(310, discounted.costX());
        assertEquals(345, discounted.secondCostX());
        assertEquals(365, discounted.arrowX());
    }

    @Test
    void discountSpaceShiftsTheResultRightWhenTheHudIsOnTheLeft() {
        Frame frame = layout(400, 300, TradingHudPos.TOP_LEFT, 0, 0, true, List.of(""));

        assertEquals(5, frame.costX());
        assertEquals(40, frame.secondCostX());
        assertEquals(60, frame.arrowX());
        assertEquals(73, frame.resultX());
    }

    @Test
    void topLeftPlacesTheCaptionSixPixelsPastTheResult() {
        Frame frame = layout(400, 300, TradingHudPos.TOP_LEFT, 0, 0, false, List.of("Mending", "Sharpness V"));

        assertEquals(new PlacedCaption(80, 10, "Mending"), frame.captions().get(0));
        assertEquals(new PlacedCaption(80, 30, "Sharpness V"), frame.captions().get(1));
    }

    @Test
    void topRightCaptionEndsSixPixelsBeforeTheFirstCost() {
        Frame frame = layout(400, 300, TradingHudPos.TOP_RIGHT, 0, 0, false, List.of("Mending"));

        assertEquals(new PlacedCaption(312, 10, "Mending"), frame.captions().getFirst());
    }

    @Test
    void rightAnchorKeepsTheCaptionStartNextToTheFirstCost() {
        Frame frame = layout(
                100,
                300,
                TradingHudPos.TOP_RIGHT,
                0,
                0,
                false,
                List.of("Sharpness V, Knockback II"));

        assertEquals(new PlacedCaption(5, 10, "...Sharpness V"), frame.captions().getFirst());
    }

    @Test
    void leftAnchorKeepsTheCaptionStartNextToTheResult() {
        Frame frame = layout(
                99,
                300,
                TradingHudPos.TOP_LEFT,
                0,
                0,
                false,
                List.of("Sharpness V, Knockback II"));

        assertEquals(new PlacedCaption(80, 10, "Sharpness V..."), frame.captions().getFirst());
    }

    @Test
    void captionIsOmittedWhenTheEllipsisItselfDoesNotFit() {
        Frame frame = layout(88, 300, TradingHudPos.TOP_RIGHT, 0, 0, false, List.of("Mending"));

        assertEquals("", frame.captions().getFirst().text());
    }

    @Test
    void customOffsetKeepsTheFirstCostTenPixelsRightOfTheOffset() {
        Frame frame = layout(400, 300, TradingHudPos.CUSTOM, 20, 4, false, List.of("Hi"));

        assertEquals(30, frame.costX());
        assertEquals(50, frame.secondCostX());
        assertEquals(70, frame.arrowX());
        assertEquals(83, frame.resultX());
        assertEquals(11, frame.rowY());
        assertEquals(new PlacedCaption(105, 14, "Hi"), frame.captions().getFirst());
    }

    @Test
    void bottomRightPinsTheCountAndSitsFivePixelsAboveTheBottom() {
        Frame frame = layout(400, 300, TradingHudPos.BOTTOM_RIGHT, 0, 0, false, List.of("A", "B", "C"));

        assertEquals(325, frame.costX());
        assertEquals(378, frame.resultX());
        assertEquals(237, frame.rowY());
        assertEquals(new PlacedCaption(318, 240, "A"), frame.captions().get(0));
        assertEquals(260, frame.captions().get(1).y());
        assertEquals(280, frame.captions().get(2).y());
    }

    @Test
    void bottomLeftKeepsTheLeftPin() {
        Frame frame = layout(400, 300, TradingHudPos.BOTTOM_LEFT, 0, 0, false, List.of("A"));

        assertEquals(5, frame.costX());
        assertEquals(58, frame.resultX());
        assertEquals(277, frame.rowY());
        assertEquals(new PlacedCaption(80, 280, "A"), frame.captions().getFirst());
    }

    @Test
    void onlyTheFirstSevenOffersArePlaced() {
        Frame frame = layout(
                400,
                200,
                TradingHudPos.BOTTOM_LEFT,
                0,
                0,
                false,
                List.of("r0", "r1", "r2", "r3", "r4", "r5", "r6", "r7"));

        assertEquals(7, frame.captions().size());
        assertEquals("r6", frame.captions().get(6).text());
        assertEquals(57, frame.rowY());
    }

    @Test
    void anOfferWithoutEnchantmentsLeavesTheIconRowWhereItIs() {
        Frame frame = layout(400, 300, TradingHudPos.TOP_RIGHT, 0, 0, false, List.of("Mending", ""));

        assertEquals(325, frame.costX());
        assertEquals("", frame.captions().get(1).text());
    }

    @Test
    void aWideCharacterTruncatesByMeasuredWidth() {
        ToIntFunction<String> width = text -> {
            int measured = 0;
            for (int i = 0; i < text.length(); i++) {
                measured += text.charAt(i) == 'W' ? 5 : 1;
            }
            return measured;
        };
        Frame frame = TradingHudLayout.layout(new Request(
                97, 300, TradingHudPos.TOP_LEFT, 0, 0, false, List.of("WWWW"), width));

        assertEquals(new PlacedCaption(80, 10, "W..."), frame.captions().getFirst());
    }

    private static Frame layout(
            int screenWidth,
            int screenHeight,
            TradingHudPos position,
            int xOffset,
            int yOffset,
            boolean showDiscountSpace,
            List<String> captions) {
        return TradingHudLayout.layout(new Request(
                screenWidth,
                screenHeight,
                position,
                xOffset,
                yOffset,
                showDiscountSpace,
                captions,
                TradingHudLayoutTest::onePixelPerCharacter));
    }

    private static int onePixelPerCharacter(String text) {
        return text.length();
    }
}
