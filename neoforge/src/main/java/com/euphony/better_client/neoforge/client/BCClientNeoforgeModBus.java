package com.euphony.better_client.neoforge.client;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.client.events.BiomeTitleEvent;
import com.euphony.better_client.client.renderer.TradingHudRenderer;
import com.euphony.better_client.keymapping.BCKeyMappings;
import com.euphony.better_client.utils.Utils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.concurrent.CompletableFuture;

public final class BCClientNeoforgeModBus {
    private BCClientNeoforgeModBus() {
    }

    public static void onResourceManagerReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(
                (barrier, manager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) ->
                        CompletableFuture.runAsync(BiomeTitleEvent.NAME_CACHE::clear, backgroundExecutor)
                                .thenCompose(barrier::wait));
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.TITLE,
                ResourceLocation.fromNamespaceAndPath(BetterClient.MOD_ID, "overlay"),
                BiomeTitleEvent::renderBiomeInfo);
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                Utils.prefix("trading_hud"),
                TradingHudRenderer::renderHud);
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BCKeyMappings.bundleUp());
    }
}
