package com.euphony.better_client.client.events;

import com.euphony.better_client.utils.data.MerchantInfo;
import com.euphony.better_client.utils.mc.TradingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;

import static com.euphony.better_client.BetterClient.config;

/**
 * 交易 HUD 事件处理类
 */
public class TradingHudEvent {
    private static boolean isWindowOpen = false;

    /**
     * 客户端世界后处理事件
     * @param clientLevel 客户端世界
     */
    public static void clientLevelPost(ClientLevel clientLevel) {
        if (!config.enableTradingHud) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        // 如果有界面打开则不处理
        if (minecraft.screen != null || player == null) {
            return;
        }

        Entity tradableEntity = TradingUtils.getCrosshairTradableEntity(minecraft, isWindowOpen);
        handleTradableEntity(tradableEntity, player);
    }

    /**
     * 处理可交易实体
     * @param entity 实体
     * @param player 玩家
     */
    private static void handleTradableEntity(Entity entity, LocalPlayer player) {
        MerchantInfo merchantInfo = MerchantInfo.getInstance();

        if (entity != null) {
            // 如果是同一个实体，不重复处理
            if (merchantInfo.isSameEntity(entity.getId())) {
                return;
            }

            // 重置商人信息并设置新的实体 ID
            merchantInfo.reset();
            merchantInfo.setLastEntityId(entity.getId());

            // 发送交互包
            sendInteractionPacket(entity, player);
        } else {
            // 清除最后的实体 ID
            merchantInfo.setLastEntityId(null);
        }
    }

    /**
     * 发送交互数据包
     * @param entity 实体
     * @param player 玩家
     */
    private static void sendInteractionPacket(Entity entity, LocalPlayer player) {
        ServerboundInteractPacket packet = ServerboundInteractPacket.createInteractionPacket(
                entity, player.isShiftKeyDown(), InteractionHand.MAIN_HAND);
        player.connection.send(packet);
    }

    public static boolean isWindowOpen() {
        return isWindowOpen;
    }

    public static void setWindowOpen(boolean windowOpen) {
        isWindowOpen = windowOpen;
    }
}
