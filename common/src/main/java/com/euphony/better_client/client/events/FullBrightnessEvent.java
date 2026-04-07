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
    private static boolean fullBrightnessActive = false;
    private static double previousGamma = 1.0D;

    public static void clientLevelPre(ClientLevel clientLevel) {
        boolean isKeyPressed = BCKeyMappings.FULL_BRIGHTNESS_TOGGLE.isDown();

        Minecraft minecraft = Minecraft.getInstance();
        if (!config.enableFullBrightnessToggle) {
            restoreGamma(minecraft);
            wasKeyPressed = isKeyPressed;
            return;
        }

        // 检查是否有输入框正在被使用（额外的安全检查）
        if (minecraft.screen != null && isInputFieldFocused(minecraft)) {
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
        if (fullBrightnessActive) {
            options.gamma().value = previousGamma;
            fullBrightnessActive = false;
        } else {
            previousGamma = options.gamma().get();
            options.gamma().value = 15.0D;
            fullBrightnessActive = true;
        }
    }

    private static void restoreGamma(Minecraft minecraft) {
        if (!fullBrightnessActive) {
            return;
        }

        minecraft.options.gamma().value = previousGamma;
        fullBrightnessActive = false;
    }

    private static boolean isInputFieldFocused(Minecraft minecraft) {
        if (minecraft.screen == null) return false;

        // 检查当前焦点组件是否为输入框
        var focusedWidget = minecraft.screen.getFocused();
        return focusedWidget instanceof EditBox || focusedWidget instanceof MultiLineEditBox;
    }
}
