package com.euphony.better_client.neoforge.client;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.client.events.BiomeTitleEvent;
import com.euphony.better_client.client.property.AxolotlBucketVariant;
import com.euphony.better_client.utils.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@EventBusSubscriber(modid = BetterClient.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BCClientNeoforge {
    @SubscribeEvent
    public static void onResourceManagerReload(AddClientReloadListenersEvent event) {
        event.addListener(Utils.prefix("clear_name_cache"), (PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecuter) -> CompletableFuture.runAsync(BiomeTitleEvent.NAME_CACHE::clear, backgroundExecutor).thenCompose(barrier::wait));
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.TITLE, Utils.prefix("overlay"), BiomeTitleEvent::renderBiomeInfo);
    }

    @SubscribeEvent
    public static void register(RegisterSelectItemModelPropertyEvent event) {
        event.register(Utils.prefix("variant"), AxolotlBucketVariant.TYPE);
    }
}
