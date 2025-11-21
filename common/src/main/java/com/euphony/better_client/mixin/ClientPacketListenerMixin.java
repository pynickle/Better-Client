package com.euphony.better_client.mixin;

import com.euphony.better_client.config.BetterClientConfig;
import com.euphony.better_client.utils.FormatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

/**
 * 客户端数据包监听器混入类，用于处理交易相关的数据包
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @ModifyVariable(method = "sendChat", at = @At("HEAD"), argsOnly = true)
    public String sendPublicMessage(String message) {
        if (!BetterClientConfig.HANDLER.instance().enableChatFormatter) return message;

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return message;
        }

        BlockPos pos = player.getOnPos();

        return FormatUtils.format(message, Map.of(
                "pos", FormatUtils.format(BetterClientConfig.HANDLER.instance().posFormat, Map.of(
                        "x", pos.getX(),
                        "y", pos.getY(),
                        "z", pos.getZ()
                ))
        ));
    }
}