package com.euphony.better_client.mixin;

import static com.euphony.better_client.BetterClient.config;

import com.euphony.better_client.screen.TrialSpawnerTimerRenderer;
import com.euphony.better_client.service.TimerHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.TrialSpawnerRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrialSpawnerRenderer.class)
public class TrailSpawnerRendererMixin {
    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderer;

    @Inject(
            method =
                    "submit(Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("RETURN"))
    public void onRender(
            BlockEntityRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            CameraRenderState cameraRenderState,
            CallbackInfo ci) {

        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;

        if (level == null) return;

        BlockPos pos = renderState.blockPos;

        // 绘制计时器（如果存在）
        TrialSpawnerTimerRenderer.drawTimer(level, pos, poseStack, nodeCollector, entityRenderer.camera);

        if (config.highSensitivityMode) {
            TrialSpawnerState spawnerState = renderState.blockState.getValue(TrialSpawnerBlock.STATE);
            TimerHandler.onSpawnerStateUpdate(level, pos, spawnerState);
        }
    }
}
