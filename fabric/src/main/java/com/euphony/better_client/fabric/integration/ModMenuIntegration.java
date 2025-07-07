package com.euphony.better_client.fabric.integration;

import com.euphony.better_client.BetterClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> BetterClient.config.makeScreen(screen);
    }
}