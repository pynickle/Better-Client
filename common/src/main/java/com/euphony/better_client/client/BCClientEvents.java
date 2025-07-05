package com.euphony.better_client.client;

import com.euphony.better_client.client.events.BeautifiedChatEvent;
import com.euphony.better_client.client.events.BiomeTitleEvent;
import com.euphony.better_client.client.events.FasterClimbingEvent;
import dev.architectury.event.events.client.ClientChatEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.TickEvent;

public class BCClientEvents {
    public static void init() {
        ClientTickEvent.CLIENT_PRE.register(BiomeTitleEvent::clientPre);

        ClientChatEvent.RECEIVED.register(BeautifiedChatEvent::chatReceived);

        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(BiomeTitleEvent::clientLevelLoad);

        TickEvent.PLAYER_PRE.register(FasterClimbingEvent::playerPre);

        // ClientScreenInputEvent.KEY_RELEASED_POST.register(BundleUpEvent::bundleUp);
    }
}
