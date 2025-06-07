package com.euphony.better_client.mixin;

import com.euphony.better_client.config.BetterClientConfig;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @Inject(
            at = {@At("HEAD")},
            method = {"clearMessages(Z)V"},
            cancellable = true
    )
    public void clear(boolean clearHistory, CallbackInfo ci) {
        if (clearHistory && BetterClientConfig.HANDLER.instance().enableChatHistoryRetention) {
            ci.cancel();
        }
    }
}
