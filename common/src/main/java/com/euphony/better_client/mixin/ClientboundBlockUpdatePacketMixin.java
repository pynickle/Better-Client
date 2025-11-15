package com.euphony.better_client.mixin;

import com.euphony.better_client.service.TimerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundBlockUpdatePacket.class)
public abstract class ClientboundBlockUpdatePacketMixin {
    @Shadow
    public abstract BlockPos getPos();

    @Shadow
    public abstract BlockState getBlockState();

    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V", at = @At("HEAD"))
    public void onBlockUpdate(ClientGamePacketListener handler, CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;

        if (getBlockState().getBlock() instanceof AirBlock) {
            if (TimerHandler.hasTimer(level, getPos())) {
                TimerHandler.deleteTimer(level, getPos());
            }
        }

        if (!(getBlockState().getBlock() instanceof TrialSpawnerBlock)) {
            return;
        }

        TimerHandler.onSpawnerBlockUpdate(level, getPos(), getBlockState());
    }
}
