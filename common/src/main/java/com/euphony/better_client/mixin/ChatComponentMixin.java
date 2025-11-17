package com.euphony.better_client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.euphony.better_client.BetterClient.config;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @ModifyExpressionValue(
            method = {
                "addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V",
                "addMessageToQueue(Lnet/minecraft/client/GuiMessage;)V",
                "addRecentChat(Ljava/lang/String;)V"
            },
            at = @At(value = "CONSTANT", args = "intValue=100"))
    private int moreMessages(int chatMaxMessages) {
        if (config.enableLongerChatHistory) {
            return config.chatMaxMessages;
        }
        return chatMaxMessages;
    }

    @Inject(
            at = {@At("HEAD")},
            method = {"clearMessages(Z)V"},
            cancellable = true)
    public void clear(boolean clearHistory, CallbackInfo ci) {
        if (clearHistory && config.enableChatHistoryRetention) {
            ci.cancel();
        }
    }

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private int better_client$getOffset() {
        LocalPlayer player = minecraft.player;
        if (player == null || player.isCreative() || player.isSpectator()) return 0;

        int offset = player.getArmorValue() > 0 ? 10 : 0;
        if (player.getAbsorptionAmount() > 0) offset += 10;
        return offset;
    }

    @ModifyArg(
            method = "render",
            index = 1,
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;",
                            ordinal = 0))
    private float offsetY(float y) {
        return y - better_client$getOffset();
    }

    @ModifyConstant(method = "screenToChatY", constant = @Constant(doubleValue = 40.0))
    private double textBottomOffset(double original) {
        return original + better_client$getOffset();
    }
}
