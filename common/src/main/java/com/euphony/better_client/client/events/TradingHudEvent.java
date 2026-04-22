package com.euphony.better_client.client.events;

import com.euphony.better_client.config.BetterClientConfig;
import com.euphony.better_client.utils.MerchantInfo;
import com.euphony.better_client.utils.TradingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;

/**
 * Client-tick driven Trading HUD controller.
 * <p>
 * While no screen is open, probes the crosshair target each tick; on a new tradable
 * merchant we silently send a {@link ServerboundInteractPacket} so the server replies
 * with offers. The reply is intercepted by {@code ClientPacketListenerMixin} and stored
 * in {@link MerchantInfo#INSTANCE} without actually opening the merchant UI.
 */
public final class TradingHudEvent {

    /** Cooldown between repeat interaction requests, in client ticks. */
    private static final int REQUEST_COOLDOWN_TICKS = 5;

    private static boolean windowOpen = false;
    private static int interactionCooldown = 0;

    private TradingHudEvent() {}

    /**
     * Called once per client tick. Detects the merchant under the crosshair and triggers
     * a silent interaction when the target changes.
     */
    public static void clientLevelPost(ClientLevel level) {
        BetterClientConfig config = BetterClientConfig.HANDLER.instance();
        if (!config.enableTradingHud || level == null) {
            return;
        }

        if (interactionCooldown > 0) {
            interactionCooldown--;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        // Any active screen (including the trade screen itself) blocks probing.
        if (minecraft.screen != null || player == null) {
            return;
        }

        Entity tradableEntity = TradingUtils.getCrosshairTradableEntity(minecraft, windowOpen);
        handleTradableEntity(tradableEntity, player);
    }

    private static void handleTradableEntity(Entity entity, LocalPlayer player) {
        MerchantInfo merchantInfo = MerchantInfo.INSTANCE;

        if (entity == null) {
            merchantInfo.setLastEntityId(null);
            return;
        }

        // Same merchant we already have offers for — nothing to do.
        if (merchantInfo.isSameEntity(entity.getId())) {
            return;
        }

        if (interactionCooldown > 0) {
            return;
        }

        merchantInfo.reset();
        merchantInfo.setLastEntityId(entity.getId());
        interactionCooldown = REQUEST_COOLDOWN_TICKS;
        sendInteractionPacket(entity, player);
    }

    private static void sendInteractionPacket(Entity entity, LocalPlayer player) {
        ClientPacketListener connection = player.connection;
        if (connection == null) {
            return;
        }
        ServerboundInteractPacket packet = ServerboundInteractPacket.createInteractionPacket(
                entity, player.isShiftKeyDown(), InteractionHand.MAIN_HAND);
        connection.send(packet);
    }

    public static boolean isWindowOpen() {
        return windowOpen;
    }

    public static void setWindowOpen(boolean open) {
        windowOpen = open;
    }

    /**
     * Consumed by {@code ClientPacketListenerMixin} (Worker D) to decide whether to
     * suppress an incoming {@link net.minecraft.network.protocol.game.ClientboundOpenScreenPacket}
     * for a merchant the player did not actually choose to open.
     */
    public static boolean shouldSkipOpenMerchantPacket() {
        return !windowOpen;
    }
}
