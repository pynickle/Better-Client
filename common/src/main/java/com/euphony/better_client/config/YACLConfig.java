package com.euphony.better_client.config;

import com.euphony.better_client.config.screen.YACLConfigScreen;
import net.minecraft.client.gui.screens.Screen;

public class YACLConfig extends Config {
    public YACLConfig() {
        super();
    }

    public static final String CLIENT_CATEGORY = "client";

    @Override
    public Screen makeScreen(Screen parent) {
        return new YACLConfigScreen(parent);

    }
}

