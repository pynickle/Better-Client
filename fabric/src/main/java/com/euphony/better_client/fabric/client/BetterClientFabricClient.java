package com.euphony.better_client.fabric.client;

import com.euphony.better_client.client.events.BiomeTitleEvent;
import com.euphony.better_client.client.property.AxolotlBucketVariant;
import com.euphony.better_client.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public final class BetterClientFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudElementRegistry.addFirst(ResourceLocation.withDefaultNamespace("title"), BiomeTitleEvent::renderBiomeInfo);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new BCResourceReloadListener());

        SelectItemModelProperties.ID_MAPPER.put(Utils.prefix("variant"), AxolotlBucketVariant.TYPE);
    }
}
