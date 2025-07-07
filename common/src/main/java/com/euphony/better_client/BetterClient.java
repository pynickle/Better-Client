package com.euphony.better_client;

import com.euphony.better_client.client.BCClientEvents;
import com.euphony.better_client.client.init.KeyMapping;
import com.euphony.better_client.config.Config;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class BetterClient {
    public static final String MOD_ID = "better_client";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Config config = Config.create();

    public static void init() {
        KeyMapping.registerKeyMapping();

        BCClientEvents.init();
    }
}
