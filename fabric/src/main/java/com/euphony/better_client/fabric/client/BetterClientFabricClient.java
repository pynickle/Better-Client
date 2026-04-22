package com.euphony.better_client.fabric.client;

import com.euphony.better_client.client.events.BiomeTitleEvent;
import com.euphony.better_client.client.events.TradingHudEvent;
import com.euphony.better_client.client.renderer.TradingHudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public final class BetterClientFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(BiomeTitleEvent::renderBiomeInfo);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new BCResourceReloadListener());
        HudRenderCallback.EVENT.register(TradingHudRenderer::renderHud);
        ClientTickEvents.END_CLIENT_TICK.register(client -> TradingHudEvent.clientLevelPost(client.level));
    }
}
