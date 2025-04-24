package com.euphony.better_client.fabric.client;

import com.euphony.better_client.client.events.BiomeTitleEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public final class BetterClientFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(BiomeTitleEvent::renderBiomeInfo);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new BCResourceReloadListener());

    }
}
