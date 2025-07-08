package com.euphony.better_client.client;

import com.euphony.better_client.client.events.*;
import com.euphony.better_client.client.renderer.TradingHudRenderer;
import dev.architectury.event.events.client.*;
import dev.architectury.event.events.common.TickEvent;

public class BCClientEvents {
    public static void init() {
        ClientTickEvent.CLIENT_PRE.register(BiomeTitleEvent::clientPre);

        ClientChatEvent.RECEIVED.register(BeautifiedChatEvent::chatReceived);

        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(BiomeTitleEvent::clientLevelLoad);

        TickEvent.PLAYER_PRE.register(FasterClimbingEvent::playerPre);

        ClientTooltipEvent.ITEM.register(DurabilityTooltipEvent::tooltip);

        ClientTickEvent.CLIENT_LEVEL_POST.register(TradingHudEvent::clientLevelPost);

        ClientGuiEvent.RENDER_HUD.register(TradingHudRenderer::renderHud);
    }
}
