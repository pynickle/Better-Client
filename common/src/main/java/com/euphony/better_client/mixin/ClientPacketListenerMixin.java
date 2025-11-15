package com.euphony.better_client.mixin;

import static com.euphony.better_client.BetterClient.config;

import com.euphony.better_client.client.events.TradingHudEvent;
import com.euphony.better_client.utils.FormatUtils;
import com.euphony.better_client.utils.data.MerchantInfo;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 客户端数据包监听器混入类，用于处理交易相关的数据包
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    /**
     * 处理商人交易报价数据包
     * @param packet 商人交易报价数据包
     * @param ci 回调信息
     */
    @Inject(at = @At("HEAD"), method = "handleMerchantOffers", cancellable = true)
    public void onHandleMerchantOffers(ClientboundMerchantOffersPacket packet, CallbackInfo ci) {
        if (!config.enableTradingHud) return;

        MerchantInfo.getInstance().setOffers(packet.getOffers());

        if (!TradingHudEvent.isWindowOpen()) {
            ci.cancel();
        }
    }

    /**
     * 处理打开界面数据包
     * @param packet 打开界面数据包
     * @param ci 回调信息
     */
    @Inject(at = @At("HEAD"), method = "handleOpenScreen", cancellable = true)
    public void onHandleOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (!config.enableTradingHud) return;

        if (!TradingHudEvent.isWindowOpen() && packet.getType() == MenuType.MERCHANT) {
            ci.cancel();
            better_client$closeContainer(packet.getContainerId());
        }
    }

    /**
     * 关闭容器
     * @param containerId 容器 ID
     */
    @Unique
    private void better_client$closeContainer(int containerId) {
        if (!config.enableTradingHud) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.connection.send(new ServerboundContainerClosePacket(containerId));
        }
    }

    @ModifyVariable(method = "sendChat", at = @At("HEAD"), argsOnly = true)
    public String sendPublicMessage(String message) {
        if (!config.enableChatFormatter) return message;

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return message;
        }

        BlockPos pos = player.getOnPos();

        return FormatUtils.format(
                message,
                Map.of(
                        "pos",
                        FormatUtils.format(
                                config.posFormat,
                                Map.of(
                                        "x", pos.getX(),
                                        "y", pos.getY(),
                                        "z", pos.getZ()))));
    }
}
