package com.euphony.better_client.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Chooses which Main inventory slots fill the hovered bundle.
 * Callers pass vanilla weights and the remaining capacity. The plan does not click, read keys, or play sound.
 */
public final class BundleUpPlan {
    private BundleUpPlan() {}

    public static <T> BundleUpResult<T> plan(BundleUpRequest<T> request) {
        if (!request.cursorEmpty() || request.textFieldAcceptingInput()) {
            return BundleUpResult.none();
        }

        List<Eligible<T>> eligible = new ArrayList<>();
        for (BundleUpSlot<T> slot : request.slots()) {
            if (!slot.mainInventory() || slot.hoveredBundle()) {
                continue;
            }
            if (!(slot.contents() instanceof BundleUpSlotContents.Admitted admitted) || admitted.count() <= 0) {
                continue;
            }
            eligible.add(new Eligible<>(slot.identity(), admitted.totalWeight(), admitted.perItemWeight(), admitted.count()));
        }
        eligible.sort(Comparator.comparing(Eligible::totalWeight));

        BundleUpWeight remaining = request.remainingCapacity();
        List<BundleUpInsertion<T>> insertions = new ArrayList<>();
        boolean playFailureSound = false;
        for (Eligible<T> slot : eligible) {
            if (slot.totalWeight().compareTo(remaining) <= 0) {
                insertions.add(new BundleUpInsertion<>(slot.identity(), slot.count(), true));
                remaining = remaining.subtract(slot.totalWeight());
                continue;
            }

            int partial = Math.min(slot.count(), remaining.itemsThatFit(slot.perItemWeight()));
            if (partial > 0) {
                insertions.add(new BundleUpInsertion<>(slot.identity(), partial, false));
            }
            playFailureSound = true;
            break;
        }
        return new BundleUpResult<>(List.copyOf(insertions), playFailureSound);
    }

    public record BundleUpWeight(int numerator, int denominator) implements Comparable<BundleUpWeight> {
        public BundleUpWeight {
            if (denominator == 0) {
                throw new ArithmeticException("denominator is zero");
            }
            if (denominator < 0) {
                numerator = -numerator;
                denominator = -denominator;
            }
            int divisor = gcd(Math.abs(numerator), denominator);
            numerator /= divisor;
            denominator /= divisor;
        }

        public static BundleUpWeight of(int numerator, int denominator) {
            return new BundleUpWeight(numerator, denominator);
        }

        public BundleUpWeight subtract(BundleUpWeight other) {
            long nextNumerator = (long) numerator * other.denominator - (long) other.numerator * denominator;
            long nextDenominator = (long) denominator * other.denominator;
            return new BundleUpWeight(toInt(nextNumerator), toInt(nextDenominator));
        }

        public int itemsThatFit(BundleUpWeight perItem) {
            if (perItem.numerator <= 0 || numerator <= 0) {
                return 0;
            }
            long nextNumerator = (long) numerator * perItem.denominator;
            long nextDenominator = (long) denominator * perItem.numerator;
            return (int) (nextNumerator / nextDenominator);
        }

        @Override
        public int compareTo(BundleUpWeight other) {
            long left = (long) numerator * other.denominator;
            long right = (long) other.numerator * denominator;
            return Long.compare(left, right);
        }

        private static int gcd(int left, int right) {
            while (right != 0) {
                int remainder = left % right;
                left = right;
                right = remainder;
            }
            return left;
        }

        private static int toInt(long value) {
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                throw new ArithmeticException("weight exceeds int range");
            }
            return (int) value;
        }
    }

    public record BundleUpRequest<T>(
            boolean cursorEmpty,
            boolean textFieldAcceptingInput,
            BundleUpWeight remainingCapacity,
            List<BundleUpSlot<T>> slots) {}

    public record BundleUpSlot<T>(T identity, boolean mainInventory, boolean hoveredBundle, BundleUpSlotContents contents) {}

    public sealed interface BundleUpSlotContents
            permits BundleUpSlotContents.Admitted, BundleUpSlotContents.Rejected, BundleUpSlotContents.Empty {
        record Admitted(BundleUpWeight totalWeight, BundleUpWeight perItemWeight, int count) implements BundleUpSlotContents {}

        record Rejected() implements BundleUpSlotContents {}

        record Empty() implements BundleUpSlotContents {}
    }

    public record BundleUpInsertion<T>(T identity, int count, boolean wholeStack) {}

    public record BundleUpResult<T>(List<BundleUpInsertion<T>> insertions, boolean playFailureSound) {
        private static <T> BundleUpResult<T> none() {
            return new BundleUpResult<>(List.of(), false);
        }
    }

    private record Eligible<T>(T identity, BundleUpWeight totalWeight, BundleUpWeight perItemWeight, int count) {}
}
