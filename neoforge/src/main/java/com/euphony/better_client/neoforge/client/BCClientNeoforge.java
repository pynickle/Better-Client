package com.euphony.better_client.neoforge.client;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.client.command.ClientWeatherCommand;
import com.euphony.better_client.client.events.*;
import com.euphony.better_client.service.ChatHistoryManager;
import com.euphony.better_client.service.ItemFrameVisibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.TutorialSteps;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = BetterClient.MOD_ID, value = Dist.CLIENT)
public final class BCClientNeoforge {
    private static ClientLevel knownLevel;

    @SubscribeEvent
    private static void onClientStarted(ClientStartedEvent event) {
        event.getClient().execute(() -> {
            Minecraft.getInstance().getTutorial().setStep(TutorialSteps.NONE);
        });
    }

    @SubscribeEvent
    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ClientWeatherCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    private static void onClientTickPre(ClientTickEvent.Pre event) {
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

        ChatHistoryManager.handleLevelTransition(knownLevel, currentLevel);
        knownLevel = currentLevel;
        if (currentLevel != null) {
            ItemFrameVisibilityManager.clientLevelLoad(currentLevel);
            BiomeTitleEvent.clientLevelLoad(currentLevel);
        }
    }

    @SubscribeEvent
    private static void onClientTickPost(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        FullBrightnessEvent.clientLevelPre(level);
        InvisibleItemFrameEvent.clientLevelPre(level);
        TradingHudEvent.clientLevelPost(level);
    }

    @SubscribeEvent
    private static void onChatReceived(ClientChatReceivedEvent.Player event) {
        var result = BeautifiedChatEvent.chatReceived(event.getBoundChatType(), event.getMessage());
        if (result.isInterrupted()) {
            event.setMessage(result.object());
        }
    }

    @SubscribeEvent
    private static void onItemTooltip(ItemTooltipEvent event) {
        DurabilityTooltipEvent.tooltip(event.getItemStack(), event.getToolTip(), event.getContext(), event.getFlags());
        CompassTooltipEvent.tooltip(event.getItemStack(), event.getToolTip(), event.getContext(), event.getFlags());
    }

    @SubscribeEvent
    private static void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        BundleUpEvent.bundleUp(Minecraft.getInstance(), event.getScreen(), event.getKeyEvent());
    }

}
