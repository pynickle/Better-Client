package com.euphony.better_client.client.events;

import com.euphony.better_client.utils.BiomeUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import org.joml.Matrix3x2fStack;

import java.util.HashMap;
import java.util.Map;

import static com.euphony.better_client.BetterClient.config;

public class BiomeTitleEvent {
    // 常量定义
    private static final int MAX_ALPHA = 255;
    private static final int TICKS_PER_SECOND = 20;

    // 状态变量
    private static Biome previousBiome;
    private static ResourceKey<Biome> displayBiome;
    private static int displayTime = 0;
    private static int alpha = 0;
    private static int cooldownTime = 0;
    private static int fadeTimer = 0;
    private static boolean complete = false;
    private static boolean fadingIn = false;

    // 缓存
    public static final Map<ResourceKey<Biome>, Component> NAME_CACHE = new HashMap<>();

    private BiomeTitleEvent() {}

    public static void clientPre(Minecraft minecraft) {
        if (!complete) return;

        if (fadingIn) {
            handleFadeIn();
        } else {
            handleDisplay();
        }
    }

    public static void renderBiomeInfo(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!shouldRender()) return;

        Minecraft mc = Minecraft.getInstance();
        Entity player = mc.getCameraEntity();
        if (player == null) return;

        BlockPos pos = player.getOnPos();
        if (mc.level == null || !mc.level.isLoaded(pos)) return;

        Holder<Biome> biomeHolder = mc.level.getBiome(pos);
        if (!biomeHolder.isBound()) return;

        Biome currentBiome = biomeHolder.value();

        if (shouldUpdateBiome(currentBiome, pos, mc)) {
            updateBiomeDisplay(biomeHolder);
        }

        if (alpha > 0) {
            renderBiomeTitle(guiGraphics, mc);
        }
    }

    public static void clientLevelLoad(ClientLevel clientLevel) {
        complete = true;
    }

    // 私有辅助方法
    private static void handleFadeIn() {
        if (fadeTimer < config.fadeInTime) {
            fadeTimer++;
            alpha = calculateAlpha(fadeTimer, config.fadeInTime);
        } else {
            completeFadeIn();
        }
    }

    private static void handleDisplay() {
        if (displayTime > 0) {
            displayTime--;
        } else if (fadeTimer > 0) {
            fadeTimer--;
            alpha = calculateAlpha(fadeTimer, config.fadeOutTime);
        } else if (cooldownTime > 0) {
            cooldownTime--;
        }
    }

    private static void completeFadeIn() {
        fadeTimer = config.fadeOutTime;
        fadingIn = false;
        displayTime = (int) (config.displayDuration * TICKS_PER_SECOND);
        alpha = MAX_ALPHA;
    }

    private static int calculateAlpha(int timer, int maxTime) {
        return (int) ((float) MAX_ALPHA / maxTime * timer);
    }

    private static boolean shouldRender() {
        return complete && config.enableBiomeTitle;
    }

    private static boolean shouldUpdateBiome(Biome currentBiome, BlockPos pos, Minecraft mc) {
        if (previousBiome == currentBiome || cooldownTime > 0) return false;

        boolean isUnderground = mc.level.dimensionType().hasSkyLight() && !mc.level.canSeeSky(pos);
        return config.enableUndergroundUpdate || !isUnderground;
    }

    private static void updateBiomeDisplay(Holder<Biome> biomeHolder) {
        previousBiome = biomeHolder.value();
        biomeHolder.unwrapKey().ifPresent(BiomeTitleEvent::initializeBiomeDisplay);
    }

    private static void initializeBiomeDisplay(ResourceKey<Biome> key) {
        cooldownTime = (int) (config.cooldownTime * TICKS_PER_SECOND);
        displayBiome = key;
        displayTime = 0;
        alpha = 0;
        fadeTimer = 0;
        fadingIn = true;
    }

    private static void renderBiomeTitle(GuiGraphics guiGraphics, Minecraft mc) {
        if (shouldHideDisplay(mc)) return;

        Font font = mc.font;
        float scale = (float) config.scale;

        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate((float) (guiGraphics.guiWidth() / 2D), (float) (guiGraphics.guiHeight() / 2D));
        pose.scale(scale, scale);

        Component biomeName = getBiomeName(displayBiome);
        int textWidth = font.width(biomeName);
        int y = -font.wordWrapHeight(biomeName.getString(), 999) / 2 + config.biomeTitleYOffset;

        guiGraphics.drawString(font, biomeName, (-textWidth / 2), y,
            config.biomeTitleColor | (alpha << 24), true);
        pose.popMatrix();
    }

    private static boolean shouldHideDisplay(Minecraft mc) {
        return (mc.options.hideGui && config.hideInF1) ||
               (mc.getDebugOverlay().showDebugScreen() && config.hideInF3);
    }

    private static Component getBiomeName(ResourceKey<Biome> key) {
        return NAME_CACHE.computeIfAbsent(key, k ->
            BiomeUtils.createBiomeDisplayComponent(k, config.enableModName));
    }
}