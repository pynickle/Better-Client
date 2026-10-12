package com.euphony.better_client.service;

import com.euphony.better_client.service.BundleUpPlan.BundleUpInsertion;
import com.euphony.better_client.service.BundleUpPlan.BundleUpRequest;
import com.euphony.better_client.service.BundleUpPlan.BundleUpResult;
import com.euphony.better_client.service.BundleUpPlan.BundleUpSlot;
import com.euphony.better_client.service.BundleUpPlan.BundleUpSlotContents;
import com.euphony.better_client.service.BundleUpPlan.BundleUpWeight;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundleUpPlanTest {
    @Test
    void occupiedCursorDoesNothingAndStaysQuiet() {
        BundleUpResult<Integer> result = plan(false, false, BundleUpWeight.of(0, 1), List.of(main(1, 1, 64, 1, 64, 1)));

        assertTrue(result.insertions().isEmpty());
        assertFalse(result.playFailureSound());
    }

    @Test
    void textFieldAcceptingInputDoesNothingAndStaysQuiet() {
        BundleUpResult<Integer> result = plan(true, true, BundleUpWeight.of(1, 1), List.of(main(1, 1, 64, 1, 64, 1)));

        assertTrue(result.insertions().isEmpty());
        assertFalse(result.playFailureSound());
    }

    @Test
    void skipsTheHoveredBundleSlot() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(1, 1), List.of(
                admitted(1, true, true, 1, 64, 1, 64, 1),
                main(2, 1, 32, 1, 64, 2)));

        assertEquals(List.of(whole(2, 2)), result.insertions());
        assertFalse(result.playFailureSound());
    }

    @Test
    void skipsSlotsOutsideTheMainInventory() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(1, 1), List.of(
                admitted(1, false, false, 1, 64, 1, 64, 1),
                main(2, 1, 16, 1, 16, 1)));

        assertEquals(List.of(whole(2, 1)), result.insertions());
        assertFalse(result.playFailureSound());
    }

    @Test
    void rejectedStacksAreSkippedAndStayQuiet() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(0, 1), List.of(
                new BundleUpSlot<>(1, true, false, new BundleUpSlotContents.Rejected()),
                new BundleUpSlot<>(2, true, false, new BundleUpSlotContents.Rejected())));

        assertTrue(result.insertions().isEmpty());
        assertFalse(result.playFailureSound());
    }

    @Test
    void rejectedStackDoesNotBlockLaterMainInventoryStacks() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(1, 1), List.of(
                new BundleUpSlot<>(1, true, false, new BundleUpSlotContents.Rejected()),
                main(2, 1, 64, 1, 64, 1)));

        assertEquals(List.of(whole(2, 1)), result.insertions());
        assertFalse(result.playFailureSound());
    }

    @Test
    void emptySlotsAreIgnored() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(1, 1), List.of(
                new BundleUpSlot<>(1, true, false, new BundleUpSlotContents.Empty()),
                main(2, 3, 64, 1, 64, 3),
                new BundleUpSlot<>(3, true, false, new BundleUpSlotContents.Empty())));

        assertEquals(List.of(whole(2, 3)), result.insertions());
        assertFalse(result.playFailureSound());
    }

    @Test
    void fillsInAscendingTotalWeight() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(1, 1), List.of(
                main(1, 1, 2, 1, 16, 8),
                main(2, 1, 64, 1, 64, 1),
                main(3, 1, 8, 1, 16, 2)));

        assertEquals(List.of(whole(2, 1), whole(3, 2), whole(1, 8)), result.insertions());
        assertFalse(result.playFailureSound());
    }

    @Test
    void honorsANestedBundleWeightAsGiven() {
        // 1/16 is an empty bundle's vanilla weight. 1/64 is one ordinary item. The plan must not re-rank them.
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(1, 1), List.of(
                main(1, 1, 16, 1, 16, 1),
                main(2, 1, 64, 1, 64, 1)));

        assertEquals(List.of(whole(2, 1), whole(1, 1)), result.insertions());
        assertFalse(result.playFailureSound());
    }

    @Test
    void fullFitRequestsNoFailureSound() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(1, 4), List.of(
                main(1, 1, 8, 1, 16, 2),
                main(2, 1, 8, 1, 64, 8)));

        assertEquals(List.of(whole(1, 2), whole(2, 8)), result.insertions());
        assertFalse(result.playFailureSound());
    }

    @Test
    void partialFitStopsTheWalk() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(5, 64), List.of(
                main(1, 1, 64, 1, 64, 1),
                main(2, 8, 64, 4, 64, 2),
                main(3, 20, 64, 1, 64, 20)));

        assertEquals(List.of(whole(1, 1), partial(2, 1)), result.insertions());
        assertTrue(result.playFailureSound());
    }

    @Test
    void doesNotSpendLeftoverCapacityOnAHeavierStack() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(10, 64), List.of(
                main(3, 30, 64, 1, 64, 30),
                main(2, 12, 64, 3, 64, 4),
                main(1, 2, 64, 1, 64, 2)));

        assertEquals(List.of(whole(1, 2), partial(2, 2)), result.insertions());
        assertTrue(result.playFailureSound());
    }

    @Test
    void fullBundleWithAnEligibleStackRequestsOneSoundAndNoInsertions() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(0, 1), List.of(main(1, 1, 64, 1, 64, 1)));

        assertTrue(result.insertions().isEmpty());
        assertTrue(result.playFailureSound());
    }

    @Test
    void midwayFullRequestsOneFailureSound() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(4, 64), List.of(
                main(1, 1, 64, 1, 64, 1),
                main(2, 8, 64, 4, 64, 2)));

        assertEquals(List.of(whole(1, 1)), result.insertions());
        assertTrue(result.playFailureSound());
    }

    @Test
    void skipsOnlyTheHoveredBundleWhenAnotherBundleIsPresent() {
        BundleUpResult<Integer> result = plan(BundleUpWeight.of(1, 1), List.of(
                admitted(1, true, true, 1, 16, 1, 16, 1),
                main(2, 5, 16, 5, 16, 1),
                main(3, 1, 64, 1, 64, 1)));

        assertEquals(List.of(whole(3, 1), whole(2, 1)), result.insertions());
        assertFalse(result.playFailureSound());
    }

    private static BundleUpResult<Integer> plan(BundleUpWeight remaining, List<BundleUpSlot<Integer>> slots) {
        return plan(true, false, remaining, slots);
    }

    private static BundleUpResult<Integer> plan(
            boolean cursorEmpty, boolean textFieldAcceptingInput, BundleUpWeight remaining, List<BundleUpSlot<Integer>> slots) {
        return BundleUpPlan.plan(new BundleUpRequest<>(cursorEmpty, textFieldAcceptingInput, remaining, slots));
    }

    private static BundleUpSlot<Integer> main(
            int identity, int totalNumerator, int totalDenominator, int perItemNumerator, int perItemDenominator, int count) {
        return admitted(identity, true, false, totalNumerator, totalDenominator, perItemNumerator, perItemDenominator, count);
    }

    private static BundleUpSlot<Integer> admitted(
            int identity,
            boolean mainInventory,
            boolean hoveredBundle,
            int totalNumerator,
            int totalDenominator,
            int perItemNumerator,
            int perItemDenominator,
            int count) {
        return new BundleUpSlot<>(
                identity,
                mainInventory,
                hoveredBundle,
                new BundleUpSlotContents.Admitted(
                        BundleUpWeight.of(totalNumerator, totalDenominator),
                        BundleUpWeight.of(perItemNumerator, perItemDenominator),
                        count));
    }

    private static BundleUpInsertion<Integer> whole(int identity, int count) {
        return new BundleUpInsertion<>(identity, count, true);
    }

    private static BundleUpInsertion<Integer> partial(int identity, int count) {
        return new BundleUpInsertion<>(identity, count, false);
    }
}
