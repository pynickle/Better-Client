package com.euphony.better_client.client.events;

import com.euphony.better_client.keymapping.BCKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.multiplayer.ClientLevel;

import static com.euphony.better_client.BetterClient.config;

public class FullBrightnessEvent {
    private static boolean wasKeyPressed = false;

    public static void clientLevelPre(ClientLevel clientLevel) {
        boolean isKeyPressed = BCKeyMappings.fullBrightnessToggle().isDown();

        if (!config.enableFullBrightnessToggle) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() != null && isInputFieldFocused(minecraft)) {
            wasKeyPressed = isKeyPressed;
            return;
        }

        if (isKeyPressed && !wasKeyPressed) {
            fullBrightnessToggle(minecraft);
        }

        wasKeyPressed = isKeyPressed;
    }

    public static void fullBrightnessToggle(Minecraft minecraft) {
        Options options = minecraft.options;
        if (options.gamma().get() > 1.0D) {
            options.gamma().value = 1.0D;
        } else {
            options.gamma().value = 15.0D;
        }
    }

    private static boolean isInputFieldFocused(Minecraft minecraft) {
        if (minecraft.gui.screen() == null) return false;

        var focusedWidget = minecraft.gui.screen().getFocused();
        return focusedWidget instanceof EditBox || focusedWidget instanceof MultiLineEditBox;
    }
}
