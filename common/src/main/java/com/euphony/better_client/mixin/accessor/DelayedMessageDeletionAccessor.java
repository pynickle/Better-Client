package com.euphony.better_client.mixin.accessor;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatComponent.DelayedMessageDeletion.class)
public interface DelayedMessageDeletionAccessor {
    @Accessor("signature")
    MessageSignature better_client$getSignature();

    @Accessor("deletableAfter")
    int better_client$getDeletableAfter();

    @Invoker("<init>")
    static ChatComponent.DelayedMessageDeletion better_client$create(MessageSignature signature, int deletableAfter) {
        throw new AssertionError();
    }
}
