package com.euphony.better_client.neoforge.client;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.client.events.BiomeTitleEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = BetterClient.MOD_ID, value = Dist.CLIENT)
public class BCClientNeoforge {
    @SubscribeEvent
    public static void onResourceManagerReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((barrier, manager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> CompletableFuture.runAsync(BiomeTitleEvent.NAME_CACHE::clear, backgroundExecutor).thenCompose(barrier::wait));
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.TITLE, ResourceLocation.fromNamespaceAndPath(BetterClient.MOD_ID, "overlay"), BiomeTitleEvent::renderBiomeInfo);
    }
}
