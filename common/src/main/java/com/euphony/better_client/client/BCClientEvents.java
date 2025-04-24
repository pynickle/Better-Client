package com.euphony.better_client.client;

import com.euphony.better_client.client.events.*;
import dev.architectury.event.events.client.*;
import dev.architectury.event.events.common.TickEvent;

public class BCClientEvents {
    public static void init() {
        ClientTooltipEvent.ITEM.register(BeeInfoEvent::item);

        ClientTickEvent.CLIENT_LEVEL_PRE.register(BiomeTitleEvent::clientPre);

        ClientChatEvent.RECEIVED.register(BeautifiedChatEvent::chatReceived);

        ClientGuiEvent.RENDER_PRE.register(BiomeTitleEvent::renderPre);

        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(BiomeTitleEvent::clientLevelLoad);
        ClientLifecycleEvent.CLIENT_SETUP.register(ItemPropertiesEvent::clientSetup);

        TickEvent.PLAYER_PRE.register(FasterClimbingEvent::playerPre);
    }
}
