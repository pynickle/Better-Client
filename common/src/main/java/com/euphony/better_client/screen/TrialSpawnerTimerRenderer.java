package com.euphony.better_client.screen;

import static com.euphony.better_client.BetterClient.config;

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

/**
 * 处理试炼刷怪笼计时器的渲染
 * 这些方法从 Mixin 钩子中调用
 */
public class TrialSpawnerTimerRenderer {
    /**
     * 在给定的试炼刷怪笼上方绘制冷却计时器
     *
     * @param level       方块所在的世界
     * @param pos         试炼刷怪笼的位置
     * @param poseStack   用于绘制文本的矩阵变换
     * @param camera      摄像机
     */
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

        // 格式化剩余时间为 MM:SS
        String timeText = TimeUtils.formatTicks(remainingTicks);
        Component text = Component.literal(timeText);

        int color = calculateTimerColor(timer, currentTime);

        // 绘制文本
        drawTextAboveBlock(text, color, poseStack, nodeCollector, camera);
    }

    /**
     * 在方块上方绘制面向玩家的彩色文本
     *
     * 参考：<a href="https://github.com/Diamondgoobird/TrialSpawnerTimer/blob/1.21.9/fabric/src/main/java/com/diamondgoobird/trialspawnertimer/TimerRenderer.java">TimerRenderer.java</a>
     *
     * @param text 要绘制的文本
     * @param color ARGB 格式的颜色
     * @param poseStack 矩阵变换栈
     * @param camera 摄像机
     */
    private static void drawTextAboveBlock(
            Component text, int color, PoseStack poseStack, SubmitNodeCollector nodeCollector, Camera camera) {
        poseStack.pushPose();

        float yRot = camera.getYRot();
        float xRot = camera.getXRot();

        Quaternionf rotation = new Quaternionf();
        rotation.rotationYXZ((float) (-Math.PI) / 180 * (yRot - 180F), (float) Math.PI / 180 * -xRot, 0.0f);
        poseStack.mulPose(rotation);

        Matrix4f matrix4f = poseStack.last().pose();
        matrix4f.rotate((float) Math.PI, 0.0F, 1.0F, 0.0F);
        matrix4f.scale(-0.025F, -0.025F, -0.025F);

        int m = Minecraft.getInstance().font.width(text.getString());
        matrix4f.translateLocal(0.5f, 1f, 0.5f);
        matrix4f.translate(1.0F - m / 2.0F, -9F, 0.0F);

        // 绘制文本
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

        double progress = timer.getProgress(currentTime); // 0.0 ~ 1.0

        int color;

        if (progress < 0.25) {
            // 阶段 1：红色（紧急）
            color = 0xFFFF0000; // 不透明红
        } else if (progress < 0.5) {
            // 阶段 2：橙色（注意）
            color = 0xFFFF8000; // 不透明橙
        } else if (progress < 0.75) {
            // 阶段 3：黄色（中等）
            color = 0xFFFFFF00; // 不透明黄
        } else {
            // 阶段 4：绿色（安全）
            color = 0xFF00FF00; // 不透明绿
        }

        return color;
    }
}
