package com.euphony.better_client.mixin;

import com.euphony.better_client.client.events.TradingHudEvent;
import com.euphony.better_client.service.NewItemMarker;
import com.euphony.better_client.utils.FormatUtils;
import com.euphony.better_client.utils.data.MerchantInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import static com.euphony.better_client.BetterClient.config;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(
            method = "handleTakeItemEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getEntity(I)Lnet/minecraft/world/entity/Entity;",
                    ordinal = 0))
    private void better_client$markPickedUpItem(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.level == null
                || packet.getPlayerId() != minecraft.player.getId()) {
            return;
        }

        Entity entity = minecraft.level.getEntity(packet.getItemId());
        if (entity instanceof ItemEntity itemEntity) {
            NewItemMarker.markPickup(minecraft.player.getInventory(), itemEntity.getItem());
        }
    }

    @Inject(at = @At("HEAD"), method = "handleMerchantOffers", cancellable = true)
    public void onHandleMerchantOffers(ClientboundMerchantOffersPacket packet, CallbackInfo ci) {
        if (!config.enableTradingHud) return;

        MerchantInfo.getInstance().setOffers(packet.getOffers());

        if (!TradingHudEvent.isWindowOpen()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "handleOpenScreen", cancellable = true)
    public void onHandleOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (!config.enableTradingHud) return;

        if (!TradingHudEvent.isWindowOpen() && packet.getType() == MenuType.MERCHANT) {
            ci.cancel();
            better_client$closeContainer(packet.getContainerId());
        }
    }

    @Unique
    private void better_client$closeContainer(int containerId) {
        if (!config.enableTradingHud) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.connection.send(new ServerboundContainerClosePacket(containerId));
        }
    }

    @ModifyVariable(method = "sendChat", at = @At("HEAD"), argsOnly = true)
    public String sendPublicMessage(String content) {
        if (!config.enableChatFormatter) return content;

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return content;
        }

        BlockPos pos = player.getOnPos();

        return FormatUtils.format(
                content,
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
