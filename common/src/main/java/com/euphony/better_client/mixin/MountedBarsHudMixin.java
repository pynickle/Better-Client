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
 * Restores food and experience bars hidden by mounted HUD elements.
 * {@code extractAirBubbles} is the shared, once-per-frame hook because NeoForge splits {@code extractPlayerHealth}.
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

    /** NeoForge passes a fixed value of 10, so do not use extractAirBubbles' vehicleHearts argument. */
    @Unique
    private boolean better_client$isRidingVehicleWithHealth() {
        return this.getVehicleMaxHearts(this.getPlayerVehicleWithHealth()) > 0;
    }

    @Inject(method = "extractAirBubbles", at = @At("HEAD"))
    private void better_client$renderFoodWhileMounted(
            GuiGraphicsExtractor graphics,
            Player player,
            int vehicleHearts,
            int yLineAir,
            int xRight,
            CallbackInfo ci) {
        if (config.isFoodBarWhileMountedEnabled() && this.better_client$isRidingVehicleWithHealth()) {
            this.extractFood(graphics, player, this.getAirBubbleYLine(vehicleHearts, yLineAir), xRight);
            // Reserve the row in NeoForge so adjacent HUD elements move out of the way.
            Platform.addRightStatusBarHeight(10);
        }
    }

    @ModifyExpressionValue(
            method = "extractAirBubbles",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;getAirBubbleYLine(II)I"))
    private int better_client$raiseAirBubblesWhileMounted(int yLineAir) {
        if (config.isFoodBarWhileMountedEnabled() && this.better_client$isRidingVehicleWithHealth()) {
            return yLineAir - 10;
        }
        return yLineAir;
    }

    @ModifyExpressionValue(
            method = "nextContextualInfoState",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/player/LocalPlayer;jumpableVehicle()Lnet/minecraft/world/entity/PlayerRideableJumping;"))
    private @Nullable PlayerRideableJumping better_client$keepExperienceBarWhileMounted(
            @Nullable PlayerRideableJumping jumpableVehicle) {
        if (config.isExperienceBarWhileMountedEnabled() && !this.willPrioritizeJumpInfo()) {
            return null;
        }
        return jumpableVehicle;
    }
}
