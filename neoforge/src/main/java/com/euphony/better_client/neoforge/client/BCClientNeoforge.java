package com.euphony.better_client.neoforge.client;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.client.events.BiomeTitleEvent;
import com.euphony.better_client.client.events.BundleUpEvent;
import com.euphony.better_client.client.events.WorldIconUpdateEvent;
import com.euphony.better_client.client.property.AxolotlBucketVariant;
import com.euphony.better_client.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@EventBusSubscriber(modid = BetterClient.MOD_ID, value = Dist.CLIENT)
public class BCClientNeoforge {
    @SubscribeEvent
    public static void onResourceManagerReload(AddClientReloadListenersEvent event) {
        event.addListener(Utils.prefix("clear_name_cache"), (PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecuter) -> CompletableFuture.runAsync(BiomeTitleEvent.NAME_CACHE::clear, backgroundExecutor).thenCompose(barrier::wait));
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.TITLE, Utils.prefix("biome_title"), BiomeTitleEvent::renderBiomeInfo);
    }

    @SubscribeEvent
    public static void onRegisterSelectItemModelProperty(RegisterSelectItemModelPropertyEvent event) {
        event.register(Utils.prefix("variant"), AxolotlBucketVariant.TYPE);
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyReleased.Post event) {
        BundleUpEvent.bundleUp(Minecraft.getInstance(), event.getScreen(), event.getKeyCode(), event.getScanCode(), event.getModifiers());
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterLevel event) {
        WorldIconUpdateEvent.onRenderLevelStage();
    }
}
