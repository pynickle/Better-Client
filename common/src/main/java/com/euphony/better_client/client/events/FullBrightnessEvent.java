package com.euphony.better_client.client.events;

import static com.euphony.better_client.BetterClient.config;

import com.euphony.better_client.keymapping.BCKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.multiplayer.ClientLevel;

public class FullBrightnessEvent {
    private static boolean wasKeyPressed = false;

    public static void clientLevelPre(ClientLevel clientLevel) {
        boolean isKeyPressed = BCKeyMappings.FULL_BRIGHTNESS_TOGGLE.isDown();

        if (!config.enableFullBrightnessToggle) return;

        Minecraft minecraft = Minecraft.getInstance();
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
        if (options.gamma().get() > 1.0D) {
            options.gamma().value = 1.0D;
        } else {
            options.gamma().value = 15.0D;
        }
    }

    private static boolean isInputFieldFocused(Minecraft minecraft) {
        if (minecraft.screen == null) return false;

        // 检查当前焦点组件是否为输入框
        var focusedWidget = minecraft.screen.getFocused();
        return focusedWidget instanceof EditBox || focusedWidget instanceof MultiLineEditBox;
    }
}
