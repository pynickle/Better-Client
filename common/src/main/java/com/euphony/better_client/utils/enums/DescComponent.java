package com.euphony.better_client.utils.enums;

import com.euphony.better_client.BetterClient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum DescComponent {
    TICK_EXPLANATION("tick_explanation", ChatFormatting.GRAY);

    private final String translationKey;
    private final ChatFormatting[] formattings;

    DescComponent(String translationKey, ChatFormatting... formattings) {
        this.translationKey = translationKey;
        this.formattings = formattings;
    }

    public Component getText() {
        return Component.translatable(
                        String.format("yacl3.config.%s:description.%s", BetterClient.MOD_ID, this.translationKey))
                .withStyle(this.formattings);
    }
}
