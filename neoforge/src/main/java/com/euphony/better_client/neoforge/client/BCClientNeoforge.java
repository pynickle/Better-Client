package com.euphony.better_client.neoforge.client;

import com.euphony.better_client.client.command.ClientWeatherCommand;
import com.euphony.better_client.client.events.*;
import com.euphony.better_client.client.property.AxolotlBucketVariant;
import com.euphony.better_client.client.renderer.TotemBarRenderer;
import com.euphony.better_client.client.renderer.TradingHudRenderer;
import com.euphony.better_client.keymapping.BCKeyMappings;
import com.euphony.better_client.service.ItemFrameVisibilityManager;
import com.euphony.better_client.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class BCClientNeoforge {
    private ClientLevel knownLevel;

    public void register(IEventBus modBus) {
        modBus.addListener(this::onRegisterGuiLayers);
        modBus.addListener(this::onRegisterSelectItemModelProperty);
        modBus.addListener(this::onRegisterKeyMappings);

        NeoForge.EVENT_BUS.addListener(this::onRegisterClientCommands);
        NeoForge.EVENT_BUS.addListener(this::onClientTickPre);
        NeoForge.EVENT_BUS.addListener(this::onClientTickPost);
        NeoForge.EVENT_BUS.addListener(this::onChatReceived);
        NeoForge.EVENT_BUS.addListener(this::onItemTooltip);
        NeoForge.EVENT_BUS.addListener(this::onKeyReleased);
        NeoForge.EVENT_BUS.addListener(this::onRenderLevelStage);
    }

    private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.TITLE, Utils.prefix("biome_title"), BiomeTitleEvent::renderBiomeInfo);
        event.registerAbove(VanillaGuiLayers.HOTBAR, Utils.prefix("trading_hud"), TradingHudRenderer::renderHud);
        event.registerAbove(VanillaGuiLayers.HOTBAR, Utils.prefix("totem_bar"), TotemBarRenderer::render);
    }

    private void onRegisterSelectItemModelProperty(RegisterSelectItemModelPropertyEvent event) {
        event.register(Utils.prefix("variant"), AxolotlBucketVariant.TYPE);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BCKeyMappings.BUNDLE_UP);
        event.register(BCKeyMappings.FULL_BRIGHTNESS_TOGGLE);
        event.register(BCKeyMappings.ITEM_FRAME_INVISIBILITY_TOGGLE);
    }

    private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ClientWeatherCommand.register(event.getDispatcher());
    }

    private void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
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

    private void onClientTickPost(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        FullBrightnessEvent.clientLevelPre(level);
        InvisibleItemFrameEvent.clientLevelPre(level);
        TradingHudEvent.clientLevelPost(level);
    }

    private void onChatReceived(ClientChatReceivedEvent.Player event) {
        var result = BeautifiedChatEvent.chatReceived(event.getBoundChatType(), event.getMessage());
        if (result.isInterrupted()) {
            event.setMessage(result.object());
        }
    }

    private void onItemTooltip(ItemTooltipEvent event) {
        DurabilityTooltipEvent.tooltip(event.getItemStack(), event.getToolTip(), event.getContext(), event.getFlags());
        CompassTooltipEvent.tooltip(event.getItemStack(), event.getToolTip(), event.getContext(), event.getFlags());
    }

    private void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        BundleUpEvent.bundleUp(Minecraft.getInstance(), event.getScreen(), event.getKeyEvent());
    }

    private void onRenderLevelStage(RenderLevelStageEvent.AfterLevel event) {
        WorldIconUpdateEvent.onRenderLevelStage();
    }
}
