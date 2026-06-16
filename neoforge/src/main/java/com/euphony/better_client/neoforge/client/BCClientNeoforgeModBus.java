package com.euphony.better_client.neoforge.client;

import com.euphony.better_client.client.events.BiomeTitleEvent;
import com.euphony.better_client.client.property.AxolotlBucketVariant;
import com.euphony.better_client.client.renderer.PotionBarRenderer;
import com.euphony.better_client.client.renderer.TotemBarRenderer;
import com.euphony.better_client.client.renderer.TradingHudRenderer;
import com.euphony.better_client.keymapping.BCKeyMappings;
import com.euphony.better_client.utils.Utils;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class BCClientNeoforgeModBus {
    private BCClientNeoforgeModBus() {
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.TITLE, Utils.prefix("biome_title"), BiomeTitleEvent::renderBiomeInfo);
        event.registerAbove(VanillaGuiLayers.HOTBAR, Utils.prefix("trading_hud"), TradingHudRenderer::renderHud);
        event.registerAbove(VanillaGuiLayers.HOTBAR, Utils.prefix("totem_bar"), TotemBarRenderer::render);
        event.registerAbove(VanillaGuiLayers.HOTBAR, Utils.prefix("potion_bar"), PotionBarRenderer::render);
    }

    public static void onRegisterSelectItemModelProperty(RegisterSelectItemModelPropertyEvent event) {
        event.register(Utils.prefix("variant"), AxolotlBucketVariant.TYPE);
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BCKeyMappings.bundleUp());
        event.register(BCKeyMappings.fullBrightnessToggle());
        event.register(BCKeyMappings.itemFrameInvisibilityToggle());
    }
}
