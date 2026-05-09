package com.euphony.better_client.neoforge.client;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.client.events.BiomeTitleEvent;
import com.euphony.better_client.client.events.BundleUpEvent;
import com.euphony.better_client.client.events.TradingHudEvent;
import com.euphony.better_client.client.renderer.TradingHudRenderer;
import com.euphony.better_client.keymapping.BCKeyMappings;
import com.euphony.better_client.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = BetterClient.MOD_ID, value = Dist.CLIENT)
public class BCClientNeoforge {
    @SubscribeEvent
    public static void onResourceManagerReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(
                (barrier, manager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) ->
                        CompletableFuture.runAsync(BiomeTitleEvent.NAME_CACHE::clear, backgroundExecutor)
                                .thenCompose(barrier::wait));
    }

    @SubscribeEvent
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

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BCKeyMappings.BUNDLE_UP);
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            TradingHudEvent.clientLevelPost(level);
        }
    }

    @SubscribeEvent
    public static void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        BundleUpEvent.bundleUp(
                Minecraft.getInstance(), event.getScreen(), event.getKeyCode(), event.getScanCode());
    }
}
