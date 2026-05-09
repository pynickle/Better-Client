package com.euphony.better_client.client.events;

import com.euphony.better_client.config.BetterClientConfig;
import com.euphony.better_client.keymapping.BCKeyMappings;
import com.euphony.better_client.utils.records.BundleCandidate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class BundleUpEvent {
    private BundleUpEvent() {
    }

    public static void bundleUp(Minecraft minecraft, Screen screen, int keyCode, int scanCode) {
        if (!BCKeyMappings.BUNDLE_UP.matches(keyCode, scanCode)
                || !BetterClientConfig.HANDLER.instance().enableBundleUp) {
            return;
        }

        Player player = minecraft.player;
        if (player == null || !(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        Slot hoveredSlot = containerScreen.hoveredSlot;
        if (hoveredSlot == null || !hoveredSlot.getItem().is(Items.BUNDLE)) {
            return;
        }

        List<BundleCandidate> candidates = collectCandidates(containerScreen, hoveredSlot);
        candidates.sort(null);

        for (BundleCandidate candidate : candidates) {
            performSlotSwap(containerScreen, hoveredSlot, candidate.slot(), player);
        }
    }

    private static List<BundleCandidate> collectCandidates(AbstractContainerScreen<?> containerScreen, Slot bundleSlot) {
        List<BundleCandidate> candidates = new ArrayList<>();

        if (containerScreen.getMenu() instanceof ChestMenu chestMenu) {
            for (Slot slot : chestMenu.slots) {
                addChestCandidate(candidates, bundleSlot, slot);
            }
        } else if (containerScreen instanceof InventoryScreen || containerScreen instanceof CreativeModeInventoryScreen) {
            for (Slot slot : containerScreen.getMenu().slots) {
                addInventoryCandidate(candidates, bundleSlot, slot);
            }
        }

        return candidates;
    }

    private static void addChestCandidate(List<BundleCandidate> candidates, Slot bundleSlot, Slot slot) {
        ItemStack stack = slot.getItem();
        if (stack.isEmpty() || slot == bundleSlot) {
            return;
        }

        Container container = slot.container;
        if (container instanceof Inventory && slot.slot >= Inventory.getSelectionSize()) {
            return;
        }

        candidates.add(new BundleCandidate(slot, stack, getEfficiency(stack)));
    }

    private static void addInventoryCandidate(List<BundleCandidate> candidates, Slot bundleSlot, Slot slot) {
        ItemStack stack = slot.getItem();
        if (stack.isEmpty() || slot == bundleSlot || !(slot.container instanceof Inventory)) {
            return;
        }

        if (slot.slot >= Inventory.getSelectionSize()) {
            candidates.add(new BundleCandidate(slot, stack, getEfficiency(stack)));
        }
    }

    private static double getEfficiency(ItemStack stack) {
        return (double) stack.getMaxStackSize() / stack.getCount();
    }

    private static void performSlotSwap(
            AbstractContainerScreen<?> screen, Slot bundleSlot, Slot targetSlot, Player player) {
        screen.slotClicked(bundleSlot, bundleSlot.index, 0, ClickType.PICKUP);
        screen.slotClicked(targetSlot, targetSlot.index, 1, ClickType.PICKUP);

        if (!player.containerMenu.getCarried().isEmpty()) {
            screen.slotClicked(bundleSlot, bundleSlot.index, 0, ClickType.PICKUP);
        }
    }
}
