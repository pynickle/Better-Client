package com.euphony.better_client.fabric;

import com.euphony.better_client.BetterClient;
import net.fabricmc.api.ModInitializer;

public final class BetterClientFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BetterClient.init();
    }
}
