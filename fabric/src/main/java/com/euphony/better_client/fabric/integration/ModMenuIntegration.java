package com.euphony.better_client.fabric.integration;

import com.euphony.better_client.config.BetterClientConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> BetterClientConfig.makeScreen().generateScreen(screen);
    }
}