package com.euphony.better_client.mixin;

import com.euphony.better_client.platform.Platform;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.euphony.better_client.BetterClient.config;

/**
 * HUD 混入类，用于在骑乘坐骑时保留饥饿条与经验条
 * <p>
 * 原版骑乘时会用坐骑血量挤掉饥饿条、用跳跃充能条挤掉经验条，这里把两者还原。
 * 饥饿条一侧注入在 {@code extractAirBubbles} 上：NeoForge 会把 {@code extractPlayerHealth}
 * 拆成若干 HUD 图层并绕开它，只有这个方法在两个加载器上都保持原样且每帧只被调用一次。
 */
@Mixin(Hud.class)
public class MountedBarsHudMixin {

    @Shadow
    private void extractFood(GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight) {
        throw new AssertionError();
    }

    @Shadow
    private int getAirBubbleYLine(int vehicleHearts, int yLineAir) {
        throw new AssertionError();
    }

    @Shadow
    private @Nullable LivingEntity getPlayerVehicleWithHealth() {
        throw new AssertionError();
    }

    @Shadow
    private int getVehicleMaxHearts(@Nullable LivingEntity vehicle) {
        throw new AssertionError();
    }

    @Shadow
    private boolean willPrioritizeJumpInfo() {
        throw new AssertionError();
    }

    /**
     * 当前是否骑着一个会占用右侧血量行的坐骑，也就是原版会藏起饥饿条的情况
     * <p>
     * 不能直接用 {@code extractAirBubbles} 的 vehicleHearts 参数：NeoForge 在调用处固定传 10
     */
    @Unique
    private boolean better_client$isRidingVehicleWithHealth() {
        return this.getVehicleMaxHearts(this.getPlayerVehicleWithHealth()) > 0;
    }

    /**
     * 骑乘时把饥饿条补画在原版氧气泡那一行，也就是坐骑血量最上一排的正上方
     *
     * @param vehicleHearts 调用方传入的坐骑血量格数，仅用于换算行偏移
     * @param yLineAir 氧气泡所在行的基准 Y 坐标
     */
    @Inject(method = "extractAirBubbles", at = @At("HEAD"))
    private void better_client$renderFoodWhileMounted(
            GuiGraphicsExtractor graphics,
            Player player,
            int vehicleHearts,
            int yLineAir,
            int xRight,
            CallbackInfo ci) {
        if (config.enableFoodBarWhileMounted && this.better_client$isRidingVehicleWithHealth()) {
            this.extractFood(graphics, player, this.getAirBubbleYLine(vehicleHearts, yLineAir), xRight);
            // 告诉 NeoForge 的右侧状态栏累加器我们占了一行，手持物品名等元素才会正确让位
            Platform.addRightStatusBarHeight(10);
        }
    }

    /**
     * 饥饿条占掉了氧气泡原来的那一行，氧气泡再往上让一行
     *
     * @param yLineAir 原版算出的氧气泡 Y 坐标
     * @return 让位给饥饿条之后的氧气泡 Y 坐标
     */
    @ModifyExpressionValue(
            method = "extractAirBubbles",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;getAirBubbleYLine(II)I"))
    private int better_client$raiseAirBubblesWhileMounted(int yLineAir) {
        if (config.enableFoodBarWhileMounted && this.better_client$isRidingVehicleWithHealth()) {
            return yLineAir - 10;
        }
        return yLineAir;
    }

    /**
     * 只在真正蓄力跳跃（或处于跳跃冷却）时才让跳跃条顶掉经验条
     *
     * @param jumpableVehicle 当前可跳跃的坐骑，返回 null 即视为没有骑乘可跳跃的坐骑
     * @return 参与状态栏优先级判定的坐骑
     */
    @ModifyExpressionValue(
            method = "nextContextualInfoState",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/player/LocalPlayer;jumpableVehicle()Lnet/minecraft/world/entity/PlayerRideableJumping;"))
    private @Nullable PlayerRideableJumping better_client$keepExperienceBarWhileMounted(
            @Nullable PlayerRideableJumping jumpableVehicle) {
        if (config.enableExperienceBarWhileMounted && !this.willPrioritizeJumpInfo()) {
            return null;
        }
        return jumpableVehicle;
    }
}
