package com.euphony.better_client.mixin;

import com.euphony.better_client.client.events.BeautifiedChatEvent;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatListener.class)
public class ChatListenerMixin {
    // Keep the vanilla player-chat call. Cancelling it drops the signature, chat log,
    // narration, and mods that read this invoke (Xaero waypoint shares).
    @ModifyArg(
            method = "showMessageToPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addPlayerMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"),
            index = 0)
    private Component better_client$decorateShownPlayerChat(Component message) {
        return BeautifiedChatEvent.processMessage(message);
    }
}
