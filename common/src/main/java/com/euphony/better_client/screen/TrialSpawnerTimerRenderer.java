package com.euphony.better_client.screen;

import com.euphony.better_client.service.TimerHandler;
import com.euphony.better_client.utils.TimeUtils;
import com.euphony.better_client.utils.records.Timer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import static com.euphony.better_client.BetterClient.config;

public class TrialSpawnerTimerRenderer {
    private static final Map<Timer, CachedTimerText> TEXT_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    public static void drawTimer(
            Level level, BlockPos pos, PoseStack poseStack, SubmitNodeCollector nodeCollector, Camera camera) {
        if (!config.enableTrialSpawnerTimer) return;

        if (Minecraft.getInstance().player == null || level == null) return;

        Timer timer = TimerHandler.getTimer(level, pos);
        if (timer == null) return;

        long currentTime = level.getGameTime();
        long remainingTicks = timer.getRemainingTicks(currentTime);

        if (remainingTicks == 0) {
            TimerHandler.deleteTimer(level, pos);
            return;
        }

        Component text = better_client$getCachedTimerText(timer, remainingTicks);

        int color = calculateTimerColor(timer, currentTime);

        drawTextAboveBlock(text, color, poseStack, nodeCollector, camera);
    }

    // Based on https://github.com/Diamondgoobird/TrialSpawnerTimer/blob/1.21.9/fabric/src/main/java/com/diamondgoobird/trialspawnertimer/TimerRenderer.java
    private static void drawTextAboveBlock(
            Component text, int color, PoseStack poseStack, SubmitNodeCollector nodeCollector, Camera camera) {
        poseStack.pushPose();

        float yRot = camera.yRot();
        float xRot = camera.xRot();

        Quaternionf rotation = new Quaternionf();
        rotation.rotationYXZ((float) (-Math.PI) / 180 * (yRot - 180F), (float) Math.PI / 180 * -xRot, 0.0f);
        poseStack.mulPose(rotation);

        Matrix4f matrix4f = poseStack.last().pose();
        matrix4f.rotate((float) Math.PI, 0.0F, 1.0F, 0.0F);
        matrix4f.scale(-0.025F, -0.025F, -0.025F);

        int m = Minecraft.getInstance().font.width(text.getString());
        matrix4f.translateLocal(0.5f, 1f, 0.5f);
        matrix4f.translate(1.0F - m / 2.0F, -9F, 0.0F);

        nodeCollector.submitText(
                poseStack,
                0.5F,
                0.5F,
                text.getVisualOrderText(),
                config.enableDropShadow,
                getDisplayMode(),
                15728880,
                color,
                0,
                0);

        poseStack.popPose();
    }

    private static Font.DisplayMode getDisplayMode() {
        return config.timerSeenThroughWalls ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
    }

    private static int calculateTimerColor(Timer timer, long currentTime) {
        if (!config.enableDynamicTimerColor) return config.timerColor;

        double progress = timer.getProgress(currentTime);

        int color;

        if (progress < 0.25) {
            color = 0xFFFF0000;
        } else if (progress < 0.5) {
            color = 0xFFFF8000;
        } else if (progress < 0.75) {
            color = 0xFFFFFF00;
        } else {
            color = 0xFF00FF00;
        }

        return color;
    }

    private static Component better_client$getCachedTimerText(Timer timer, long remainingTicks) {
        long remainingSeconds = TimeUtils.ticksToSeconds(remainingTicks);
        CachedTimerText cached = TEXT_CACHE.get(timer);
        if (cached != null && cached.remainingSeconds() == remainingSeconds) {
            return cached.text();
        }

        Component text = Component.literal(TimeUtils.formatTicks(remainingTicks));
        TEXT_CACHE.put(timer, new CachedTimerText(remainingSeconds, text));
        return text;
    }

    private record CachedTimerText(long remainingSeconds, Component text) {
    }
}
