package com.euphony.better_client.fabric.client;

import com.euphony.better_client.client.command.ClientWeatherCommand;
import com.euphony.better_client.client.events.BeautifiedChatEvent;
import com.euphony.better_client.client.events.BiomeTitleEvent;
import com.euphony.better_client.client.events.BundleUpEvent;
import com.euphony.better_client.client.events.CompassTooltipEvent;
import com.euphony.better_client.client.events.DurabilityTooltipEvent;
import com.euphony.better_client.client.events.FasterClimbingEvent;
import com.euphony.better_client.client.events.FullBrightnessEvent;
import com.euphony.better_client.client.events.InvisibleItemFrameEvent;
import com.euphony.better_client.client.events.TradingHudEvent;
import com.euphony.better_client.client.property.AxolotlBucketVariant;
import com.euphony.better_client.client.renderer.TotemBarRenderer;
import com.euphony.better_client.client.renderer.TradingHudRenderer;
import com.euphony.better_client.keymapping.BCKeyMappings;
import com.euphony.better_client.service.ItemFrameVisibilityManager;
import com.euphony.better_client.utils.Utils;
import com.euphony.better_client.event.CompoundEventResult;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;

public final class BetterClientFabricBootstrap {
    private static ClientLevel knownLevel;

    private BetterClientFabricBootstrap() {}

    public static void initClient() {
        registerHud();
        registerItemProperties();
        registerKeyMappings();
        registerCommands();
        registerScreenCallbacks();
        registerClientEvents();
    }

    private static void registerHud() {
        HudElementRegistry.addFirst(Utils.prefix("biome_title"), BiomeTitleEvent::renderBiomeInfo);
        HudElementRegistry.addFirst(Utils.prefix("trading_hud"), TradingHudRenderer::renderHud);
        HudElementRegistry.addFirst(Utils.prefix("totem_bar"), TotemBarRenderer::render);
    }

    private static void registerItemProperties() {
        SelectItemModelProperties.ID_MAPPER.put(Utils.prefix("variant"), AxolotlBucketVariant.TYPE);
    }

    private static void registerKeyMappings() {
        KeyMappingHelper.registerKeyMapping(BCKeyMappings.BUNDLE_UP);
        KeyMappingHelper.registerKeyMapping(BCKeyMappings.FULL_BRIGHTNESS_TOGGLE);
        KeyMappingHelper.registerKeyMapping(BCKeyMappings.ITEM_FRAME_INVISIBILITY_TOGGLE);
    }

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, buildContext) -> ClientWeatherCommand.register(dispatcher));
    }

    private static void registerScreenCallbacks() {
        ScreenEvents.BEFORE_INIT.register(
                (client, screen, width, height) -> ScreenKeyboardEvents.afterKeyRelease(screen)
                        .register((registeredScreen, keyEvent) ->
                                BundleUpEvent.bundleUp(Minecraft.getInstance(), registeredScreen, keyEvent)));
    }

    private static void registerClientEvents() {
        ClientTickEvents.START_CLIENT_TICK.register(BetterClientFabricBootstrap::onClientTickStart);
        ClientTickEvents.START_LEVEL_TICK.register(level -> {
            FullBrightnessEvent.clientLevelPre(level);
            InvisibleItemFrameEvent.clientLevelPre(level);
        });
        ClientTickEvents.END_LEVEL_TICK.register(TradingHudEvent::clientLevelPost);
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            DurabilityTooltipEvent.tooltip(stack, lines, context, type);
            CompassTooltipEvent.tooltip(stack, lines, context, type);
        });
        ClientReceiveMessageEvents.ALLOW_CHAT.register(
                (message, playerChatMessage, sender, boundChatType, timeStamp) -> {
                    CompoundEventResult<net.minecraft.network.chat.Component> result =
                            BeautifiedChatEvent.chatReceived(boundChatType, message);
                    if (result.isInterrupted()) {
                        Minecraft.getInstance().gui.getChat().addClientSystemMessage(result.object());
                        return false;
                    }
                    return true;
                });
    }

    private static void onClientTickStart(Minecraft minecraft) {
        BiomeTitleEvent.clientPre(minecraft);

        LocalPlayer player = minecraft.player;
        if (player != null) {
            FasterClimbingEvent.playerPre(player);
        }

        ClientLevel currentLevel = minecraft.level;
        if (currentLevel == knownLevel) {
            return;
        }

        knownLevel = currentLevel;
        if (currentLevel != null) {
            ItemFrameVisibilityManager.clientLevelLoad(currentLevel);
            BiomeTitleEvent.clientLevelLoad(currentLevel);
        }
    }
}
