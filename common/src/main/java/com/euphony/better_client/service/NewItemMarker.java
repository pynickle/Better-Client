package com.euphony.better_client.service;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.BitSet;

import static com.euphony.better_client.BetterClient.config;

public final class NewItemMarker {
    private static final int INVENTORY_SIZE = 36;
    private static final BitSet MARKED_SLOTS = new BitSet(INVENTORY_SIZE);
    private static boolean better_client$acceptInventoryAdditions;

    private NewItemMarker() {
    }

    public static void beginInventoryAddition() {
        better_client$acceptInventoryAdditions = true;
    }

    public static void endInventoryAddition() {
        better_client$acceptInventoryAdditions = false;
    }

    public static void markInventoryAddition(int slot) {
        if (better_client$acceptInventoryAdditions) {
            mark(slot);
        }
    }

    public static void mark(int slot) {
        if (config.enableNewItemMarker && slot >= 0 && slot < INVENTORY_SIZE) {
            MARKED_SLOTS.set(slot);
        }
    }

    public static void markCreativeMenuSlot(int slot) {
        if (slot >= 36 && slot < 45) {
            mark(slot - 36);
        } else if (slot >= 9 && slot < INVENTORY_SIZE) {
            mark(slot);
        }
    }

    public static void clear(int slot) {
        if (slot >= 0 && slot < INVENTORY_SIZE) {
            MARKED_SLOTS.clear(slot);
        }
    }

    public static void clearAll() {
        MARKED_SLOTS.clear();
        better_client$acceptInventoryAdditions = false;
    }

    public static boolean isMarked(int slot) {
        return config.enableNewItemMarker && slot >= 0 && slot < INVENTORY_SIZE && MARKED_SLOTS.get(slot);
    }

    public static ItemStack[] snapshot(Inventory inventory) {
        ItemStack[] snapshot = new ItemStack[INVENTORY_SIZE];
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            snapshot[slot] = inventory.getItem(slot).copy();
        }
        return snapshot;
    }

    public static void markAddedSlots(Inventory inventory, ItemStack[] before) {
        if (!config.enableNewItemMarker || before == null || before.length < INVENTORY_SIZE) {
            return;
        }

        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack after = inventory.getItem(slot);
            if (after.isEmpty() || !slotGainedStack(before[slot], after)) {
                continue;
            }

            if (countMatching(inventory, after) > countMatching(before, after)) {
                mark(slot);
            }
        }
    }

    public static boolean isPlayerInventorySlot(Slot slot) {
        return slot.container instanceof Inventory
                && slot.getContainerSlot() >= 0
                && slot.getContainerSlot() < INVENTORY_SIZE;
    }

    public static void clearOnHover(Slot slot) {
        if (config.clearNewItemMarkerOnHover) {
            clearSlot(slot);
        }
    }

    public static void clearOnSelect(Inventory inventory) {
        if (config.clearNewItemMarkerOnSelect) {
            clear(inventory.getSelectedSlot());
        }
    }

    public static void clearEmptySlots(Inventory inventory) {
        for (int slot = MARKED_SLOTS.nextSetBit(0);
                slot >= 0 && slot < INVENTORY_SIZE;
                slot = MARKED_SLOTS.nextSetBit(slot + 1)) {
            if (inventory.getItem(slot).isEmpty()) {
                MARKED_SLOTS.clear(slot);
            }
        }
    }

    public static void clearOnClose() {
        if (config.clearNewItemMarkerOnInventoryClose) {
            clearAll();
        }
    }

    public static void clearSlot(Slot slot) {
        if (slot != null && slot.container instanceof Inventory) {
            clear(slot.getContainerSlot());
        }
    }

    public static void renderSlotMarker(GuiGraphicsExtractor graphics, Slot slot) {
        if (!isMarked(slot.getContainerSlot())) {
            return;
        }

        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) {
            clear(slot.getContainerSlot());
            return;
        }

        draw(graphics, stack, slot.x, slot.y);
    }

    public static void renderHotbarMarker(GuiGraphicsExtractor graphics, int slot, ItemStack stack, int x, int y) {
        if (!config.showNewItemMarkerOnHotbar) {
            return;
        }
        if (!isMarked(slot)) {
            return;
        }
        if (stack.isEmpty()) {
            clear(slot);
            return;
        }

        draw(graphics, stack, x, y);
    }

    private static void draw(GuiGraphicsExtractor graphics, ItemStack stack, int slotX, int slotY) {
        int color = colorFor(stack.getRarity());
        int rgb = color & 0x00FFFFFF;
        float pulse = 0.5F + 0.5F * (float) Math.sin(System.currentTimeMillis() * 0.0028D);
        int halo = ((int) (40 + pulse * 50) << 24) | rgb;
        int core = ((int) (140 + pulse * 60) << 24) | rgb;

        int centerX = slotX + markerXOffset();
        int centerY = slotY + markerYOffset();

        graphics.fill(centerX - 2, centerY - 2, centerX + 3, centerY + 3, halo);
        graphics.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, core);
        graphics.fill(centerX, centerY, centerX + 1, centerY + 1, color);
    }

    private static int markerXOffset() {
        return switch (config.newItemMarkerPosition) {
            case UPPER_LEFT, LOWER_LEFT -> 3;
            case UPPER_RIGHT, LOWER_RIGHT -> 12;
        };
    }

    private static int markerYOffset() {
        return switch (config.newItemMarkerPosition) {
            case UPPER_LEFT, UPPER_RIGHT -> 3;
            case LOWER_LEFT, LOWER_RIGHT -> 12;
        };
    }

    private static boolean slotGainedStack(ItemStack before, ItemStack after) {
        return before.isEmpty()
                || !ItemStack.isSameItemSameComponents(before, after)
                || after.getCount() > before.getCount();
    }

    private static int countMatching(Inventory inventory, ItemStack stack) {
        int count = 0;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            count += matchingCount(inventory.getItem(slot), stack);
        }
        return count;
    }

    private static int countMatching(ItemStack[] stacks, ItemStack stack) {
        int count = 0;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            count += matchingCount(stacks[slot], stack);
        }
        return count;
    }

    private static int matchingCount(ItemStack candidate, ItemStack stack) {
        return !candidate.isEmpty() && ItemStack.isSameItemSameComponents(candidate, stack)
                ? candidate.getCount()
                : 0;
    }

    private static int colorFor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0xFFFFFFFF;
            case UNCOMMON -> 0xFFFFD700;
            case RARE -> 0xFF00BFFF;
            case EPIC -> 0xFFA020F0;
        };
    }
}
