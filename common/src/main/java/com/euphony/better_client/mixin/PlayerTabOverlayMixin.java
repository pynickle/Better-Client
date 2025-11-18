package com.euphony.better_client.mixin;

import com.euphony.better_client.config.BetterClientConfig;
import com.euphony.better_client.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Unique
    @Final
    private static final int PLAYER_SLOT_EXTRA_WIDTH = 45;

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyConstant(method = "render", constant = @Constant(intValue = 13))
    private int modifySlotWidthConstant(int original) {
        return original + PLAYER_SLOT_EXTRA_WIDTH;
    }

    @Redirect(
            method = "render",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/PlayerTabOverlay;renderPingIcon(Lnet/minecraft/client/gui/GuiGraphics;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V"))
    private void redirectRenderPingIcon(
            PlayerTabOverlay overlay, GuiGraphics graphics, int width, int x, int y, PlayerInfo player) {
        if (BetterClientConfig.HANDLER.instance().enableBetterPingDisplay) {
            enc_vanilla$render(minecraft, overlay, graphics, width, x, y, player);
        } else {
            overlay.renderPingIcon(graphics, width, x, y, player);
        }
    }

    @Unique
    private static final int PING_TEXT_RENDER_OFFSET = -13;

    @Unique
    private static void enc_vanilla$render(
            Minecraft mc, PlayerTabOverlay overlay, GuiGraphics graphics, int width, int x, int y, PlayerInfo player) {

        String pingString = String.format("%dms", player.getLatency());
        int pingStringWidth = mc.font.width(pingString);
        int pingTextColor = ColorUtils.getPingTextColor(player.getLatency());

        int textX = width + x - pingStringWidth - 1;
        if (BetterClientConfig.HANDLER.instance().enableDefaultPingBars) {
            textX += PING_TEXT_RENDER_OFFSET;
        }

        graphics.drawString(mc.font, pingString, textX, y, pingTextColor);

        if (BetterClientConfig.HANDLER.instance().enableDefaultPingBars) {
            overlay.renderPingIcon(graphics, width, x, y, player);
        }
    }
}
