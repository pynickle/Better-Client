package com.euphony.better_client.fabric.client;

import com.euphony.better_client.client.events.BiomeTitleEvent;
import com.euphony.better_client.client.events.BundleUpEvent;
import com.euphony.better_client.client.events.TradingHudEvent;
import com.euphony.better_client.client.renderer.TradingHudRenderer;
import com.euphony.better_client.keymapping.BCKeyMappings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

public final class BetterClientFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(BiomeTitleEvent::renderBiomeInfo);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new BCResourceReloadListener());
        HudRenderCallback.EVENT.register(TradingHudRenderer::renderHud);
        ClientTickEvents.END_CLIENT_TICK.register(client -> TradingHudEvent.clientLevelPost(client.level));
        KeyBindingHelper.registerKeyBinding(BCKeyMappings.bundleUp());
        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) ->
                ScreenKeyboardEvents.afterKeyRelease(screen).register((registeredScreen, keyCode, scanCode, modifiers) ->
                        BundleUpEvent.bundleUp(Minecraft.getInstance(), registeredScreen, keyCode, scanCode)));
    }
}
