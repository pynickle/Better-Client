package com.euphony.better_client;

import com.euphony.better_client.client.BCClientEvents;
import com.euphony.better_client.client.init.KeyMapping;
import com.euphony.better_client.config.BetterClientConfig;

public final class BetterClient {
    public static final String MOD_ID = "better_client";

    public static void init() {
        BetterClientConfig.load();
        KeyMapping.registerKeyMapping();

        BCClientEvents.init();
    }
}
