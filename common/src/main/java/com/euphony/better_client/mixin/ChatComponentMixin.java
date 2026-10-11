package com.euphony.better_client.mixin;

import com.euphony.better_client.config.Config;
import com.euphony.better_client.service.ChatHistoryManager;
import com.euphony.better_client.service.HistoryBoundaryRenderer;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static com.euphony.better_client.BetterClient.config;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private List<GuiMessage> allMessages;

    @Shadow
    private List<GuiMessage.Line> trimmedMessages;

    @Shadow
    private int chatScrollbarPos;

    @Shadow
    public int getLinesPerPage() {
        return 0;
    }

    @Shadow
    private int getLineHeight() {
        return 0;
    }

    @Shadow
    private int getWidth() {
        return 0;
    }

    @Shadow
    private double getScale() {
        return 0.0;
    }

    @Unique
    private HistoryBoundaryRenderer.Layout better_client$historyBoundaryLayout = HistoryBoundaryRenderer.NONE;

    @ModifyExpressionValue(
            method = {
                    "addMessageToDisplayQueue(Lnet/minecraft/client/multiplayer/chat/GuiMessage;)V",
                    "addMessageToQueue(Lnet/minecraft/client/multiplayer/chat/GuiMessage;)V",
                    "addRecentChat(Ljava/lang/String;)V"
            },
            at = @At(value = "CONSTANT", args = "intValue=100"))
    private int moreMessages(int chatMaxMessages) {
        if (config.enableLongerChatHistory) {
            return Config.clampChatMaxMessages(config.chatMaxMessages);
        }
        return chatMaxMessages;
    }

    @Inject(
            at = {@At("HEAD")},
            method = {"clearMessages(Z)V"},
            cancellable = true)
    public void clear(boolean history, CallbackInfo ci) {
        if (ChatHistoryManager.shouldCancelVanillaClear(history)) {
            ci.cancel();
        }
    }

    @Unique
    private int better_client$getOffset() {
        LocalPlayer player = minecraft.player;
        if (player == null || player.isCreative() || player.isSpectator()) return 0;

        int offset = player.getArmorValue() > 0 ? 10 : 0;
        if (player.getAbsorptionAmount() > 0) offset += 10;
        return offset;
    }

    @ModifyConstant(
            method =
                    "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
            constant = @Constant(intValue = 40))
    private int textBottomOffset(int constant) {
        return constant + better_client$getOffset();
    }

    @Inject(method = "forEachLine", at = @At("HEAD"))
    private void better_client$prepareHistoryBoundary(CallbackInfoReturnable<Integer> cir) {
        this.better_client$historyBoundaryLayout = HistoryBoundaryRenderer.layout(
                this.minecraft,
                this.allMessages,
                this.trimmedMessages,
                this.chatScrollbarPos,
                this.getLinesPerPage(),
                this.getWidth(),
                this.getScale());
    }

    @Redirect(
            method = "forEachLine",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent$LineConsumer;accept(Lnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V"))
    private void better_client$shiftHistoryBoundary(
            ChatComponent.LineConsumer consumer, GuiMessage.Line line, int lineIndex, float alpha) {
        int shift = HistoryBoundaryRenderer.shiftFor(lineIndex, this.chatScrollbarPos, this.better_client$historyBoundaryLayout);
        int shifted = lineIndex + shift;
        if (shifted >= this.getLinesPerPage()) {
            return;
        }
        consumer.accept(line, shifted, alpha);
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;forEachLine(Lnet/minecraft/client/gui/components/ChatComponent$AlphaCalculator;Lnet/minecraft/client/gui/components/ChatComponent$LineConsumer;)I",
                    ordinal = 1,
                    shift = At.Shift.AFTER))
    private void better_client$drawHistoryBoundary(
            ChatComponent.ChatGraphicsAccess graphics,
            int screenHeight,
            int ticks,
            ChatComponent.DisplayMode displayMode,
            CallbackInfo ci) {
        HistoryBoundaryRenderer.draw(
                graphics,
                displayMode,
                screenHeight,
                ticks,
                this.better_client$getOffset(),
                this.getLineHeight(),
                this.getScale(),
                this.getWidth(),
                this.chatScrollbarPos,
                this.getLinesPerPage(),
                this.better_client$historyBoundaryLayout);
    }
}
