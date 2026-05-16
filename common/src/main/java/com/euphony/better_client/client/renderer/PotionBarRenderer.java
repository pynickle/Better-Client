package com.euphony.better_client.client.renderer;

import com.euphony.better_client.config.option.PotionBarPos;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.euphony.better_client.BetterClient.config;

public class PotionBarRenderer {
    private static final int ROW_WIDTH = 78;
    private static final int ROW_HEIGHT = 16;
    private static final int ROW_GAP = 2;
    private static final int ICON_SIZE = 9;
    private static final int BAR_WIDTH = 45;
    private static final int BAR_HEIGHT = 4;
    private static final int AMP_BADGE_WIDTH = 10;
    private static final int MARGIN = 6;

    private static final int BACKGROUND_COLOR = 0x8F101014;
    private static final int BORDER_COLOR = 0xAAFFFFFF;
    private static final int BAR_BACKGROUND_COLOR = 0x8F000000;
    private static final int[] AMP_COLORS = {
        0xFF7FC7FF, 0xFF73D98A, 0xFFE6CF63, 0xFFFFA44D, 0xFFFF6B7A
    };
    private static final String[] ROMAN_LEVELS = {
        "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    };
    private static final int FALLBACK_BENEFICIAL_COLOR = 0xFF62D88B;
    private static final int FALLBACK_HARMFUL_COLOR = 0xFFE35B64;
    private static final int AMBIENT_COLOR = 0xFF7DB9FF;

    private static final Map<EffectKey, Integer> better_client$maxDurations = new HashMap<>();
    private static final Map<Holder<MobEffect>, Integer> VANILLA_EFFECT_COLORS = new HashMap<>();
    private static ClientLevel better_client$lastLevel;
    private static UUID better_client$lastPlayerId;

    static {
        VANILLA_EFFECT_COLORS.put(MobEffects.ABSORPTION, 0xFFE8C653);
        VANILLA_EFFECT_COLORS.put(MobEffects.BAD_OMEN, 0xFF6B516F);
        VANILLA_EFFECT_COLORS.put(MobEffects.BLINDNESS, 0xFF4A4A54);
        VANILLA_EFFECT_COLORS.put(MobEffects.CONDUIT_POWER, 0xFF4DB8D7);
        VANILLA_EFFECT_COLORS.put(MobEffects.DARKNESS, 0xFF3A3347);
        VANILLA_EFFECT_COLORS.put(MobEffects.DOLPHINS_GRACE, 0xFF58BFE5);
        VANILLA_EFFECT_COLORS.put(MobEffects.FIRE_RESISTANCE, 0xFFE98143);
        VANILLA_EFFECT_COLORS.put(MobEffects.GLOWING, 0xFFE0D46B);
        VANILLA_EFFECT_COLORS.put(MobEffects.HASTE, 0xFFE4C957);
        VANILLA_EFFECT_COLORS.put(MobEffects.HEALTH_BOOST, 0xFFE86473);
        VANILLA_EFFECT_COLORS.put(MobEffects.HERO_OF_THE_VILLAGE, 0xFF65D06C);
        VANILLA_EFFECT_COLORS.put(MobEffects.HUNGER, 0xFF9A6D35);
        VANILLA_EFFECT_COLORS.put(MobEffects.INFESTED, 0xFF7B6F61);
        VANILLA_EFFECT_COLORS.put(MobEffects.INSTANT_DAMAGE, 0xFFD04650);
        VANILLA_EFFECT_COLORS.put(MobEffects.INSTANT_HEALTH, 0xFFEF6F86);
        VANILLA_EFFECT_COLORS.put(MobEffects.INVISIBILITY, 0xFF8BA3B4);
        VANILLA_EFFECT_COLORS.put(MobEffects.JUMP_BOOST, 0xFF74D46F);
        VANILLA_EFFECT_COLORS.put(MobEffects.LEVITATION, 0xFFB58EE6);
        VANILLA_EFFECT_COLORS.put(MobEffects.LUCK, 0xFF54C77D);
        VANILLA_EFFECT_COLORS.put(MobEffects.MINING_FATIGUE, 0xFF657086);
        VANILLA_EFFECT_COLORS.put(MobEffects.NAUSEA, 0xFF8F6AB6);
        VANILLA_EFFECT_COLORS.put(MobEffects.NIGHT_VISION, 0xFF5EA8E8);
        VANILLA_EFFECT_COLORS.put(MobEffects.OOZING, 0xFF82B456);
        VANILLA_EFFECT_COLORS.put(MobEffects.POISON, 0xFF7EC655);
        VANILLA_EFFECT_COLORS.put(MobEffects.RAID_OMEN, 0xFF8D5576);
        VANILLA_EFFECT_COLORS.put(MobEffects.REGENERATION, 0xFFEF7BC6);
        VANILLA_EFFECT_COLORS.put(MobEffects.RESISTANCE, 0xFFB88962);
        VANILLA_EFFECT_COLORS.put(MobEffects.SATURATION, 0xFFF08D4B);
        VANILLA_EFFECT_COLORS.put(MobEffects.SLOW_FALLING, 0xFFC2DDF1);
        VANILLA_EFFECT_COLORS.put(MobEffects.SLOWNESS, 0xFF6F9CC8);
        VANILLA_EFFECT_COLORS.put(MobEffects.SPEED, 0xFF62D8A6);
        VANILLA_EFFECT_COLORS.put(MobEffects.STRENGTH, 0xFFD7594D);
        VANILLA_EFFECT_COLORS.put(MobEffects.TRIAL_OMEN, 0xFF5D7DB8);
        VANILLA_EFFECT_COLORS.put(MobEffects.UNLUCK, 0xFF6F8A7B);
        VANILLA_EFFECT_COLORS.put(MobEffects.WATER_BREATHING, 0xFF4FA8E2);
        VANILLA_EFFECT_COLORS.put(MobEffects.WEAKNESS, 0xFF8A8E9D);
        VANILLA_EFFECT_COLORS.put(MobEffects.WEAVING, 0xFFC7C0A7);
        VANILLA_EFFECT_COLORS.put(MobEffects.WIND_CHARGED, 0xFF88DDE7);
        VANILLA_EFFECT_COLORS.put(MobEffects.WITHER, 0xFF58505B);
    }

    private PotionBarRenderer() {}

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!config.enablePotionBar) return;

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui) return;
        if (minecraft.screen != null && minecraft.screen.showsActiveEffects()) return;

        better_client$resetStateWhenContextChanges(minecraft, player);

        List<MobEffectInstance> effects = player.getActiveEffects().stream()
                .filter(MobEffectInstance::showIcon)
                .sorted(Comparator.comparing(PotionBarRenderer::better_client$sortId)
                        .thenComparingInt(MobEffectInstance::getAmplifier))
                .toList();
        if (effects.isEmpty()) {
            better_client$maxDurations.clear();
            return;
        }

        Set<EffectKey> currentKeys = new HashSet<>();
        for (MobEffectInstance effect : effects) {
            EffectKey key = EffectKey.from(effect);
            currentKeys.add(key);
            if (!effect.isInfiniteDuration()) {
                better_client$maxDurations.merge(key, effect.getDuration(), Math::max);
            }
        }
        better_client$maxDurations.keySet().removeIf(key -> !currentKeys.contains(key));

        int hudHeight = effects.size() * ROW_HEIGHT + (effects.size() - 1) * ROW_GAP;
        int x = better_client$getX(minecraft, ROW_WIDTH);
        int y = better_client$getY(minecraft, hudHeight);

        for (MobEffectInstance effect : effects) {
            better_client$drawRow(graphics, effect, x, y);
            y += ROW_HEIGHT + ROW_GAP;
        }
    }

    public static boolean shouldHideVanillaEffectHud() {
        return config.enablePotionBar && !config.showVanillaEffectHud;
    }

    private static void better_client$resetStateWhenContextChanges(Minecraft minecraft, LocalPlayer player) {
        UUID playerId = player.getUUID();
        if (minecraft.level != better_client$lastLevel || !playerId.equals(better_client$lastPlayerId)) {
            better_client$maxDurations.clear();
            better_client$lastLevel = minecraft.level;
            better_client$lastPlayerId = playerId;
        }
    }

    private static int better_client$getX(Minecraft minecraft, int hudWidth) {
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int x = switch (config.potionBarPos) {
            case TOP_LEFT, BOTTOM_LEFT -> MARGIN;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - hudWidth - MARGIN;
            case CENTER -> (screenWidth - hudWidth) / 2;
            case CUSTOM -> config.potionBarXOffset;
        };
        if (config.potionBarPos != PotionBarPos.CUSTOM) {
            x += config.potionBarXOffset;
        }
        return Mth.clamp(x, 0, Math.max(0, screenWidth - hudWidth));
    }

    private static int better_client$getY(Minecraft minecraft, int hudHeight) {
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int y = switch (config.potionBarPos) {
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - hudHeight - MARGIN;
            case TOP_LEFT, TOP_RIGHT, CENTER -> MARGIN;
            case CUSTOM -> config.potionBarYOffset;
        };
        if (config.potionBarPos != PotionBarPos.CUSTOM) {
            y += config.potionBarYOffset;
        }
        return Mth.clamp(y, 0, Math.max(0, screenHeight - hudHeight));
    }

    private static void better_client$drawRow(GuiGraphics graphics, MobEffectInstance effect, int x, int y) {
        graphics.fill(x, y, x + ROW_WIDTH, y + ROW_HEIGHT, BACKGROUND_COLOR);
        graphics.renderOutline(x, y, ROW_WIDTH, ROW_HEIGHT, BORDER_COLOR);
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                Gui.getMobEffectSprite(effect.getEffect()),
                x + 3,
                y + 3,
                ICON_SIZE,
                ICON_SIZE);

        better_client$drawAmplifierBadge(graphics, effect.getAmplifier(), x + 16, y);

        int barX = x + 29;
        int barY = y + 6;
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, BAR_BACKGROUND_COLOR);
        int fillWidth = Math.round(BAR_WIDTH * better_client$getProgress(effect));
        if (fillWidth > 0) {
            graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, better_client$getBarColor(effect));
        }
        graphics.renderOutline(barX, barY, BAR_WIDTH, BAR_HEIGHT, 0x99FFFFFF);
    }

    private static void better_client$drawAmplifierBadge(GuiGraphics graphics, int amplifier, int x, int rowY) {
        int level = amplifier + 1;
        if (level > 5) {
            better_client$drawRomanAmplifier(graphics, level, x, rowY);
            return;
        }

        int y = rowY + 4;
        for (int i = 0; i < level; i++) {
            int cellX = x + i * 2;
            int height = 2 + i;
            int color = AMP_COLORS[Math.min(i, AMP_COLORS.length - 1)];
            graphics.fill(cellX, y + 6 - height, cellX + 1, y + 6, color);
        }
    }

    private static void better_client$drawRomanAmplifier(GuiGraphics graphics, int level, int x, int y) {
        Font font = Minecraft.getInstance().font;
        String text = level <= ROMAN_LEVELS.length ? ROMAN_LEVELS[level - 1] : "X+";
        float scale = Math.min(0.65F, (float) AMP_BADGE_WIDTH / Math.max(1, font.width(text)));
        float centeredX = x + (AMP_BADGE_WIDTH - font.width(text) * scale) / 2.0F;
        float centeredY = y + (ROW_HEIGHT - font.lineHeight * scale) / 2.0F;

        graphics.pose().pushMatrix();
        graphics.pose().translate(centeredX, centeredY);
        graphics.pose().scale(scale, scale);
        graphics.drawString(font, text, 0, 0, AMP_COLORS[AMP_COLORS.length - 1], false);
        graphics.pose().popMatrix();
    }

    private static float better_client$getProgress(MobEffectInstance effect) {
        if (effect.isInfiniteDuration()) return 1.0F;

        int maxDuration = better_client$maxDurations.getOrDefault(EffectKey.from(effect), effect.getDuration());
        if (maxDuration <= 0) return 0.0F;

        return Mth.clamp((float) effect.getDuration() / maxDuration, 0.0F, 1.0F);
    }

    private static int better_client$getBarColor(MobEffectInstance effect) {
        if (effect.isAmbient() || effect.isInfiniteDuration()) return AMBIENT_COLOR;

        Integer color = VANILLA_EFFECT_COLORS.get(effect.getEffect());
        if (color != null) return color;

        return effect.getEffect().value().isBeneficial() ? FALLBACK_BENEFICIAL_COLOR : FALLBACK_HARMFUL_COLOR;
    }

    private static String better_client$sortId(MobEffectInstance effect) {
        return better_client$getEffectId(effect).toString();
    }

    private static Identifier better_client$getEffectId(MobEffectInstance effect) {
        return effect.getEffect()
                .unwrapKey()
                .map(key -> key.identifier())
                .orElseGet(() -> Gui.getMobEffectSprite(effect.getEffect()));
    }

    private record EffectKey(Identifier id, int amplifier) {
        private static EffectKey from(MobEffectInstance effect) {
            return new EffectKey(better_client$getEffectId(effect), effect.getAmplifier());
        }
    }
}
