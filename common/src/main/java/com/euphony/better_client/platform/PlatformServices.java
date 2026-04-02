package com.euphony.better_client.platform;

import java.util.ServiceLoader;

public final class PlatformServices {
    private static final BetterClientPlatform PLATFORM = ServiceLoader.load(BetterClientPlatform.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No Better Client platform service found"));

    private PlatformServices() {}

    public static BetterClientPlatform getPlatform() {
        return PLATFORM;
    }
}
