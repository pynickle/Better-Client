package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IHasPlayTime;
import com.euphony.better_client.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

import static com.euphony.better_client.BetterClient.config;

@Mixin(WorldSelectionList.WorldListEntry.class)
public class WorldListEntryMixin {
    @Unique
    private static final ResourceLocation WORLD_PLAY_TIME_ICON = Utils.prefix("textures/gui/world_play_time.png");

    @Shadow
    @Final
    LevelSummary summary;

    @Redirect(
            method = "getNarration",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelSummary;isExperimental()Z"))
    private boolean disableExperimentalWarning(LevelSummary instance) {
        if (config.enableNoExperimentalWarning && !config.enableExperimentalDisplay) {
            return false;
        }
        return instance.isExperimental();
    }

    @Redirect(
            method = "<init>",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/storage/LevelSummary;getInfo()Lnet/minecraft/network/chat/Component;"))
    private Component modifyInfo(LevelSummary levelSummary) {
        if (config.enableNoExperimentalWarning && !config.enableExperimentalDisplay) {
            if (levelSummary.info == null) {
                levelSummary.info = this.better_client$createInfo(levelSummary);
            }

            return levelSummary.info;
        }
        return levelSummary.getInfo();
    }

    @Unique
    private Component better_client$createInfo(LevelSummary levelSummary) {
        if (levelSummary.isLocked()) {
            return Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
        } else if (levelSummary.requiresManualConversion()) {
            return Component.translatable("selectWorld.conversion").withStyle(ChatFormatting.RED);
        } else if (!levelSummary.isCompatible()) {
            return Component.translatable("selectWorld.incompatible.info", levelSummary.getWorldVersionName())
                    .withStyle(ChatFormatting.RED);
        } else {
            MutableComponent mutableComponent = levelSummary.isHardcore()
                    ? Component.empty()
                            .append(Component.translatable("gameMode.hardcore").withColor(-65536))
                    : Component.translatable(
                            "gameMode." + levelSummary.getGameMode().getName());
            if (levelSummary.hasCommands()) {
                mutableComponent.append(", ").append(Component.translatable("selectWorld.commands"));
            }

            MutableComponent mutableComponent2 = levelSummary.getWorldVersionName();
            MutableComponent mutableComponent3 = Component.literal(", ")
                    .append(Component.translatable("selectWorld.version"))
                    .append(CommonComponents.SPACE);
            if (levelSummary.shouldBackup()) {
                mutableComponent3.append(mutableComponent2.withStyle(
                        levelSummary.isDowngrade() ? ChatFormatting.RED : ChatFormatting.ITALIC));
            } else {
                mutableComponent3.append(mutableComponent2);
            }

            mutableComponent.append(mutableComponent3);
            return mutableComponent;
        }
    }

    @Inject(at = @At("TAIL"), method = "renderContent")
    public void renderContent(
            GuiGraphics guiGraphics, int mouseX, int mouseY, boolean pHovering, float pPartialTick, CallbackInfo ci) {
        if (!config.enableWorldPlayTime) return;

        if (!(this.summary instanceof IHasPlayTime hasPlayTime)) return;
        int ticks = hasPlayTime.better_client$getPlayTimeTicks();
        if (ticks <= 0) return;

        // 将 tick 转换为小时（保留 1 位小数）
        double hours = ticks / 72000.0; // 20 ticks * 3600 seconds
        String hourText = hours >= 100.0 ? String.valueOf((int) hours) : String.format(Locale.US, "%.1f", hours);
        Component component = Component.translatable("message.world_play_time", Component.literal(hourText));

        Minecraft mc = Minecraft.getInstance();
        int textWidth = mc.font.width(component);
        if (textWidth == 0) return;

        WorldSelectionList.WorldListEntry entry = (WorldSelectionList.WorldListEntry) (Object) this;
        int iconSize = 9;
        int spacing = 2;
        int totalWidth = iconSize + spacing + textWidth;
        int renderX = entry.getContentX() + entry.getContentWidth() - totalWidth - 4;
        int renderY = entry.getContentY();

        // 绘制图标
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                WORLD_PLAY_TIME_ICON,
                renderX,
                renderY,
                0,
                0,
                iconSize,
                iconSize,
                iconSize,
                iconSize,
                config.worldPlayTimeColor);
        // 绘制文字
        guiGraphics.drawString(
                mc.font, component, renderX + iconSize + spacing, renderY + 1, config.worldPlayTimeColor, false);
    }
}
