package com.euphony.better_client.client.events;

import com.euphony.better_client.screen.BundlePanelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import static com.euphony.better_client.BetterClient.config;

/** Both loaders share the survival-screen hooks and the same panel session. */
public final class BundlePanelEvent {
    private static InventoryScreen owner;
    private static BundlePanelScreen panel;

    private BundlePanelEvent() {}

    public static BundlePanelScreen panel(Screen screen) {
        if (!(screen instanceof InventoryScreen inventory) || !config.enableBundlePanel) {
            if (screen == owner && panel != null) close(screen);
            return null;
        }
        if (owner != inventory) {
            if (panel != null) panel.close();
            owner = inventory;
            panel = new BundlePanelScreen(inventory);
        }
        return panel;
    }

    public static void close(Screen screen) {
        if (screen != owner) return;
        panel.close();
        panel = null;
        owner = null;
    }
}
