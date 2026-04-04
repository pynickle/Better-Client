package com.euphony.better_client.mixin.accessor;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChatComponent.State.class)
public interface ChatComponentStateAccessor {
    @Accessor("messages")
    List<GuiMessage> better_client$getMessages();

    @Accessor("history")
    List<String> better_client$getHistory();
}
