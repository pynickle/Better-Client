package com.euphony.better_client;

import com.euphony.better_client.client.BCClientEvents;
import com.euphony.better_client.config.BetterClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.tutorial.TutorialSteps;

public final class BetterClient {
    public static final String MOD_ID = "better_client";

    public static void init() {
        BetterClientConfig.load();

        BCClientEvents.init();

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            // Tutorial#setStep saves options before loader key mappings have finished registering.
            minecraft.options.tutorialStep = TutorialSteps.NONE;
        });
    }
}
