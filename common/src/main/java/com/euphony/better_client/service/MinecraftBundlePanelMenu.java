package com.euphony.better_client.service;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

import java.util.ArrayList;
import java.util.List;

import static com.euphony.better_client.service.BundlePanelSession.*;

/** Only this adapter knows menu slots, component snapshots, and vanilla packets. */
public final class MinecraftBundlePanelMenu implements MenuAdapter {
    private final Minecraft minecraft = Minecraft.getInstance();
    private final InventoryScreen screen;

    public MinecraftBundlePanelMenu(InventoryScreen screen) { this.screen = screen; }

    @Override
    public Snapshot read() {
        boolean valid = minecraft.player != null && minecraft.gui.screen() == screen
                && minecraft.player.containerMenu == screen.getMenu() && minecraft.getConnection() != null;
        List<Slot> slots = new ArrayList<>();
        for (net.minecraft.world.inventory.Slot slot : screen.getMenu().slots) {
            int position = slot.getContainerSlot();
            Region region = Region.OTHER;
            if (minecraft.player != null && slot.container == minecraft.player.getInventory()) {
                if (position >= 9 && position <= 35) region = Region.MAIN;
                else if (position >= 0 && position <= 8) region = Region.HOTBAR;
                else if (position == 40) region = Region.OFFHAND;
            }
            ItemStack item = slot.getItem();
            BundleContents contents = item.get(DataComponents.BUNDLE_CONTENTS);
            List<Stack> entries = contents == null ? List.of() : contents.itemCopies().map(MinecraftBundlePanelMenu::describe).toList();
            slots.add(new Slot(slot.index, region, position, describe(item), entries,
                    slot.getMaxStackSize(), contents == null ? -1 : contents.getSelectedItemIndex()));
        }
        // State IDs acknowledge predictions, so they must not invalidate an otherwise equal snapshot.
        return new Snapshot(screen.getMenu().containerId, 0, valid, describe(screen.getMenu().getCarried()), slots);
    }

    @Override
    public void select(int id, int index) {
        ItemStack bag = screen.getMenu().getSlot(id).getItem();
        BundleContents contents = bag.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null || index < 0 || index >= contents.size() || minecraft.getConnection() == null) return;
        // Selection is a toggle and is not serialized in BundleContents. Clear both ends first,
        // including when the locally selected index already looks right after a server update.
        BundleItem.toggleSelectedItem(bag, -1);
        minecraft.getConnection().send(new ServerboundSelectBundleItemPacket(id, -1));
        BundleItem.toggleSelectedItem(bag, index);
        minecraft.getConnection().send(new ServerboundSelectBundleItemPacket(id, index));
    }

    @Override
    public void click(int id, int button) {
        if (!read().valid()) return;
        net.minecraft.world.inventory.Slot slot = screen.getMenu().getSlot(id);
        // A transfer is many clicks; vanilla would play a bundle sound per item and exhaust the
        // sound pool, so the panel's own clicks stay silent.
        QuietClicks.run(() -> ((AbstractContainerScreen<?>) screen).slotClicked(slot, id, button, ContainerInput.PICKUP));
    }

    @Override
    public int capacity(int id, Item item) {
        BundleContents contents = screen.getMenu().getSlot(id).getItem().get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null || contents.weight().isError()) return 0;
        ItemStack candidate = sample(item).copyWithCount(item.maxStack());
        return contents.asMutable().tryInsert(candidate);
    }

    public static ItemStack sample(Item item) { return ((StackKey) item.key()).sample().copy(); }

    private static Stack describe(ItemStack item) {
        if (item.isEmpty()) return Stack.EMPTY;
        boolean bundle = item.is(ItemTags.BUNDLES) && item.has(DataComponents.BUNDLE_CONTENTS);
        return new Stack(new Item(new StackKey(item.copyWithCount(1)), item.getHoverName().getString(),
                item.getMaxStackSize(), item.isStackable(), bundle), item.getCount());
    }

    private record StackKey(ItemStack sample) {
        @Override public boolean equals(Object other) {
            return other instanceof StackKey key && ItemStack.isSameItemSameComponents(sample, key.sample);
        }
        @Override public int hashCode() { return sample.getItem().hashCode(); }
        @Override public String toString() {
            return BuiltInRegistries.ITEM.getKey(sample.getItem()) + ":" + sample.getComponents();
        }
    }
}
