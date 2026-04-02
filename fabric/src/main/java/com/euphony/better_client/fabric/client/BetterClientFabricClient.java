package com.euphony.better_client.fabric.client;

import net.fabricmc.api.ClientModInitializer;

public final class BetterClientFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BetterClientFabricBootstrap.initClient();
    }
}
