package com.euphony.better_client.fabric.client;

import com.euphony.better_client.client.events.BiomeTitleEvent;
import com.euphony.better_client.client.events.BundleUpEvent;
import com.euphony.better_client.client.property.AxolotlBucketVariant;
import com.euphony.better_client.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public final class BetterClientFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudElementRegistry.addFirst(ResourceLocation.withDefaultNamespace("title"), BiomeTitleEvent::renderBiomeInfo);
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
                .registerReloader(Utils.prefix("name_cache_clear"), new BCResourceReloadListener());

        SelectItemModelProperties.ID_MAPPER.put(Utils.prefix("variant"), AxolotlBucketVariant.TYPE);

        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            ScreenKeyboardEvents.afterKeyRelease(screen).register((s, keyEvent) -> {
                BundleUpEvent.bundleUp(Minecraft.getInstance(), s, keyEvent);
            });
        });
    }
}
