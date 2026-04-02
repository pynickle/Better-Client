package com.euphony.better_client.mixin;

import com.euphony.better_client.screen.TrialSpawnerTimerRenderer;
import com.euphony.better_client.service.TimerHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.TrialSpawnerRenderer;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.euphony.better_client.BetterClient.config;

@Mixin(TrialSpawnerRenderer.class)
public class TrailSpawnerRendererMixin {
    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderer;

    @Inject(
            method =
                    "submit(Lnet/minecraft/client/renderer/blockentity/state/SpawnerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("RETURN"))
    public void onRender(
            SpawnerRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera,
            CallbackInfo ci) {

        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;

        if (level == null) return;

        BlockPos pos = state.blockPos;

        // 绘制计时器（如果存在）
        TrialSpawnerTimerRenderer.drawTimer(level, pos, poseStack, submitNodeCollector, entityRenderer.camera);

        if (config.highSensitivityMode) {
            TimerHandler.onSpawnerStateUpdate(level, pos, null);
        }
    }
}
