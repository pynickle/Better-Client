package com.euphony.better_client.mixin;

import com.euphony.better_client.utils.mc.ChatMentionUtils;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.euphony.better_client.BetterClient.config;

@Mixin(ClientSuggestionProvider.class)
public abstract class ClientSuggestionProviderMixin {
    @ModifyReturnValue(method = "getCustomTabSuggestions", at = @At("RETURN"))
    private Collection<String> better_client$appendMentionSuggestions(Collection<String> original) {
        if (!config.enableChatMentionAutocomplete) {
            return original;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof ChatScreen)
                || !ChatMentionUtils.isAvailableInCurrentSession()
                || minecraft.player == null) {
            return original;
        }

        String selfName = minecraft.player.getGameProfile().name();
        Set<String> suggestions = new LinkedHashSet<>(original);
        minecraft.getConnection().getOnlinePlayers().forEach(playerInfo -> {
            String name = playerInfo.getProfile().name();
            if (name != null && !name.isBlank() && !name.equals(selfName)) {
                suggestions.add("@" + name + " ");
            }
        });
        return new ArrayList<>(suggestions);
    }
}
