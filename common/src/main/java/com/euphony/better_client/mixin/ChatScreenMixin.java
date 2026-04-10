package com.euphony.better_client.mixin;

import com.euphony.better_client.utils.mc.ChatMentionUtils;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.euphony.better_client.BetterClient.config;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Shadow
    protected EditBox input;

    @Shadow
    private CommandSuggestions commandSuggestions;

    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void better_client$initMentionFormatter(CallbackInfo ci) {
        this.input.addFormatter((text, firstCharacterIndex) -> {
            if (!config.enableChatMentionAutocomplete || !ChatMentionUtils.isAvailableInCurrentSession()) {
                return null;
            }

            return ChatMentionUtils.formatMentions(text, config.chatMentionColor);
        });
    }

    @Inject(method = "onEdited", at = @At("TAIL"))
    private void better_client$updateMentionSuggestions(String value, CallbackInfo ci) {
        if (!config.enableChatMentionAutocomplete || this.commandSuggestions == null) {
            return;
        }

        if (!ChatMentionUtils.isAvailableInCurrentSession()) {
            this.commandSuggestions.hide();
            return;
        }

        if (this.better_client$hasActiveMention(value)) {
            this.commandSuggestions.setAllowSuggestions(true);
            this.commandSuggestions.updateCommandInfo();
            this.commandSuggestions.showSuggestions(false);
            return;
        }

        this.commandSuggestions.hide();
    }

    @Unique
    private boolean better_client$hasActiveMention(String value) {
        if (value == null || value.isBlank() || value.startsWith("/")) {
            return false;
        }

        int cursor = this.input.getCursorPosition();
        int mentionIndex = value.lastIndexOf('@', Math.max(0, cursor - 1));
        if (mentionIndex < 0) {
            return false;
        }

        for (int index = mentionIndex + 1; index < cursor; index++) {
            char current = value.charAt(index);
            if (!Character.isLetterOrDigit(current) && current != '_') {
                return false;
            }
        }

        return mentionIndex == 0 || Character.isWhitespace(value.charAt(mentionIndex - 1));
    }
}
