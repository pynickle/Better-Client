package com.euphony.better_client.client.events;

import com.euphony.better_client.keymapping.BCKeyMappings;
import com.euphony.better_client.service.BundleUpPlan;
import com.euphony.better_client.service.BundleUpPlan.BundleUpInsertion;
import com.euphony.better_client.service.BundleUpPlan.BundleUpRequest;
import com.euphony.better_client.service.BundleUpPlan.BundleUpResult;
import com.euphony.better_client.service.BundleUpPlan.BundleUpSlot;
import com.euphony.better_client.service.BundleUpPlan.BundleUpSlotContents;
import com.euphony.better_client.service.BundleUpPlan.BundleUpWeight;
import com.mojang.serialization.DataResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Bees;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;

import java.util.ArrayList;
import java.util.List;

import static com.euphony.better_client.BetterClient.config;

public class BundleUpEvent {
    private static final int MAIN_INVENTORY_FIRST = 9;
    private static final int MAIN_INVENTORY_LAST = 35;
    private static final int PRIMARY_BUTTON = 0;
    private static final Fraction BUNDLE_IN_BUNDLE_WEIGHT = Fraction.getFraction(1, 16);

    public static void bundleUp(Minecraft minecraft, Screen screen, KeyEvent keyEvent) {
        if (!BCKeyMappings.bundleUp().matches(keyEvent) || !config.enableBundleUp) {
            return;
        }
        if (minecraft.player == null || !(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        Slot hoveredSlot = containerScreen.hoveredSlot;
        if (hoveredSlot == null) {
            return;
        }
        ItemStack bundle = hoveredSlot.getItem();
        if (!bundle.is(ItemTags.BUNDLES) || bundle.get(DataComponents.BUNDLE_CONTENTS) == null) {
            return;
        }

        List<BundleUpSlot<Slot>> slots = new ArrayList<>();
        for (Slot slot : containerScreen.getMenu().slots) {
            slots.add(new BundleUpSlot<>(
                    slot, isMainInventory(slot), slot == hoveredSlot, describe(slot.getItem())));
        }

        BundleUpResult<Slot> result = BundleUpPlan.plan(new BundleUpRequest<>(
                containerScreen.getMenu().getCarried().isEmpty(),
                screen.isInputCaptured(),
                remainingCapacity(bundle),
                slots));

        for (BundleUpInsertion<Slot> insertion : result.insertions()) {
            insertIntoBundle(containerScreen, hoveredSlot, insertion.identity());
        }
        if (result.playFailureSound()) {
            minecraft.player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
        }
    }

    private static boolean isMainInventory(Slot slot) {
        int containerSlot = slot.getContainerSlot();
        return slot.container instanceof Inventory
                && containerSlot >= MAIN_INVENTORY_FIRST
                && containerSlot <= MAIN_INVENTORY_LAST;
    }

    private static BundleUpSlotContents describe(ItemStack stack) {
        if (stack.isEmpty()) {
            return new BundleUpSlotContents.Empty();
        }
        if (!BundleContents.canItemBeInBundle(stack)) {
            return new BundleUpSlotContents.Rejected();
        }

        Fraction perItem = perItemWeight(stack);
        if (perItem == null) {
            return new BundleUpSlotContents.Rejected();
        }
        try {
            Fraction total = perItem.multiplyBy(Fraction.getFraction(stack.getCount(), 1));
            return new BundleUpSlotContents.Admitted(weightOf(total), weightOf(perItem), stack.getCount());
        } catch (ArithmeticException exception) {
            return new BundleUpSlotContents.Rejected();
        }
    }

    private static Fraction perItemWeight(ItemStack stack) {
        BundleContents nested = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (nested != null) {
            DataResult<Fraction> nestedWeight = nested.weight();
            if (nestedWeight.isError()) {
                return null;
            }
            return nestedWeight.getOrThrow().add(BUNDLE_IN_BUNDLE_WEIGHT);
        }
        if (!stack.getOrDefault(DataComponents.BEES, Bees.EMPTY).bees().isEmpty()) {
            return Fraction.ONE;
        }
        int maxStackSize = stack.getMaxStackSize();
        if (maxStackSize <= 0) {
            return null;
        }
        return Fraction.getFraction(1, maxStackSize);
    }

    private static BundleUpWeight remainingCapacity(ItemStack bundle) {
        BundleContents contents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null) {
            return BundleUpWeight.of(0, 1);
        }
        DataResult<Fraction> weight = contents.weight();
        if (weight.isError()) {
            return BundleUpWeight.of(0, 1);
        }
        Fraction remaining = Fraction.ONE.subtract(weight.getOrThrow());
        if (remaining.compareTo(Fraction.ZERO) <= 0) {
            return BundleUpWeight.of(0, 1);
        }
        return weightOf(remaining);
    }

    private static BundleUpWeight weightOf(Fraction fraction) {
        return BundleUpWeight.of(fraction.getNumerator(), fraction.getDenominator());
    }

    private static void insertIntoBundle(AbstractContainerScreen<?> screen, Slot bundleSlot, Slot targetSlot) {
        screen.slotClicked(bundleSlot, bundleSlot.index, PRIMARY_BUTTON, ContainerInput.PICKUP);
        screen.slotClicked(targetSlot, targetSlot.index, PRIMARY_BUTTON, ContainerInput.PICKUP);
        if (!screen.getMenu().getCarried().isEmpty()) {
            screen.slotClicked(bundleSlot, bundleSlot.index, PRIMARY_BUTTON, ContainerInput.PICKUP);
        }
    }
}
