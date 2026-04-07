package com.euphony.better_client.mixin;

import com.euphony.better_client.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

import static com.euphony.better_client.BetterClient.config;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Unique
    @Final
    private static final int PLAYER_SLOT_EXTRA_WIDTH = 45;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract void extractPingIcon(
            GuiGraphicsExtractor graphics, int slotWidth, int xo, int yo, PlayerInfo info);

    @ModifyConstant(method = "extractRenderState", constant = @Constant(intValue = 13))
    private int modifySlotWidthConstant(int original) {
        if (!config.enableBetterPingDisplay) {
            return original;
        }

        return original + PLAYER_SLOT_EXTRA_WIDTH;
    }

    @Redirect(
            method = "extractRenderState",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/PlayerTabOverlay;extractPingIcon(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V"))
    private void redirectRenderPingIcon(
            PlayerTabOverlay overlay, GuiGraphicsExtractor graphics, int slotWidth, int xo, int yo, PlayerInfo info) {
        if (config.enableBetterPingDisplay) {
            better_client$render(minecraft, overlay, graphics, slotWidth, xo, yo, info);
        } else {
            this.extractPingIcon(graphics, slotWidth, xo, yo, info);
        }
    }

    @Unique
    private static final int PING_TEXT_RENDER_OFFSET = -13;

    @Unique
    private static void better_client$render(
            Minecraft mc,
            PlayerTabOverlay overlay,
            GuiGraphicsExtractor graphics,
            int width,
            int x,
            int y,
            PlayerInfo player) {
        String pingString = String.format("%dms", player.getLatency());
        int pingStringWidth = mc.font.width(pingString);
        int pingTextColor = ColorUtils.getPingTextColor(player.getLatency());

        int textX = width + x - pingStringWidth - 1;
        if (config.enableDefaultPingBars) {
            textX += PING_TEXT_RENDER_OFFSET;
        }

        graphics.text(mc.font, pingString, textX, y, pingTextColor);

        if (config.enableDefaultPingBars) {
            ((PlayerTabOverlayMixin) (Object) overlay).extractPingIcon(graphics, width, x, y, player);
        }
    }
}
