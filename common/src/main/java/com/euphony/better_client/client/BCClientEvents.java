package com.euphony.better_client.client;

import com.euphony.better_client.client.events.*;
import com.euphony.better_client.client.renderer.PotionBarRenderer;
import com.euphony.better_client.client.renderer.TotemBarRenderer;
import com.euphony.better_client.client.renderer.TradingHudRenderer;
import com.euphony.better_client.service.ChatHistoryManager;
import com.euphony.better_client.service.ItemFrameVisibilityManager;
import dev.architectury.event.events.client.*;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class BCClientEvents {
    private static ClientLevel knownLevel;

    public static void init() {
        ClientTickEvent.CLIENT_PRE.register(BCClientEvents::clientPre);
        ClientTickEvent.CLIENT_LEVEL_PRE.register(FullBrightnessEvent::clientLevelPre);
        ClientTickEvent.CLIENT_LEVEL_PRE.register(InvisibleItemFrameEvent::clientLevelPre);

        ClientChatEvent.RECEIVED.register(BeautifiedChatEvent::chatReceived);

        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(ItemFrameVisibilityManager::clientLevelLoad);
        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(BiomeTitleEvent::clientLevelLoad);

        TickEvent.PLAYER_PRE.register(FasterClimbingEvent::playerPre);

        ClientTooltipEvent.ITEM.register(DurabilityTooltipEvent::tooltip);
        ClientTooltipEvent.ITEM.register(CompassTooltipEvent::tooltip);

        ClientTickEvent.CLIENT_LEVEL_POST.register(TradingHudEvent::clientLevelPost);

        ClientGuiEvent.RENDER_HUD.register(TradingHudRenderer::renderHud);
        ClientGuiEvent.RENDER_HUD.register(TotemBarRenderer::render);
        ClientGuiEvent.RENDER_HUD.register(PotionBarRenderer::render);
    }

    private static void clientPre(Minecraft minecraft) {
        BiomeTitleEvent.clientPre(minecraft);

        ClientLevel currentLevel = minecraft.level;
        if (currentLevel == knownLevel) {
            return;
        }

        ChatHistoryManager.handleLevelTransition(knownLevel, currentLevel);
        knownLevel = currentLevel;
    }
}
