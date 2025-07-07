package com.euphony.better_client.client.events;

import com.euphony.better_client.keymapping.BCKeyMappings;
import com.euphony.better_client.utils.records.BundleCandidate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.euphony.better_client.BetterClient.config;

@Environment(EnvType.CLIENT)
public class BundleUpEvent {
    public static void bundleUp(Minecraft minecraft, Screen screen, int keyCode, int scanCode, int modifiers) {
        if(!BCKeyMappings.BUNDLE_UP.matches(keyCode, scanCode) || !config.enableBundleUp) return;

        List<BundleCandidate> candidates = new ArrayList<>();

        Player player = minecraft.player;
        if(player == null || screen == null) return;

        if(screen instanceof AbstractContainerScreen<?> containerScreen) {
            if(containerScreen.hoveredSlot == null) return;

            Slot hoveredSlot = containerScreen.hoveredSlot;
            ItemStack selectedItem = hoveredSlot.getItem();
            if(!selectedItem.is(ItemTags.BUNDLES)) return;

            if (containerScreen.getMenu() instanceof ChestMenu chestMenu) {
                for(int i = 0; i < chestMenu.slots.size(); i++) {
                    Slot slot = chestMenu.getSlot(i);

                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;

                    Container container = slot.container;
                    if(container instanceof Inventory) {
                        if(slot.slot >= 9) {
                            continue;
                        }
                    }

                    double efficiency = (double) stack.getMaxStackSize() / stack.getCount();
                    candidates.add(new BundleCandidate(slot, i, stack, efficiency));
                }
            } else if(containerScreen instanceof InventoryScreen
                    || containerScreen instanceof CreativeModeInventoryScreen) {
                var menu = containerScreen.getMenu();

                for(int i = 0; i < menu.slots.size(); i++) {
                    Slot slot = menu.getSlot(i);
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;

                    Container container = slot.container;
                    if(container instanceof Inventory) {
                        if(slot.slot >= 9) {
                            double efficiency = (double) stack.getMaxStackSize() / stack.getCount();
                            candidates.add(new BundleCandidate(slot, i, stack, efficiency));
                        }
                    }
                }
            }
            candidates.sort(null);

            for(BundleCandidate candidate : candidates) {
                performSlotSwap(containerScreen, hoveredSlot, candidate.slot(), player);
            }
        }
    }

    public static void performSlotSwap(AbstractContainerScreen<?> screen, Slot bundleSlot, Slot targetSlot, Player player) {
        screen.slotClicked(bundleSlot, 1, 0, ClickType.PICKUP);

        screen.slotClicked(targetSlot, 0, 0, ClickType.PICKUP);

        if (!player.containerMenu.getCarried().isEmpty()) {
            screen.slotClicked(bundleSlot, 1, 0, ClickType.PICKUP);
        }
    }
}