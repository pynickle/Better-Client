package com.euphony.better_client.mixin;

import com.euphony.better_client.config.BetterClientConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @ModifyExpressionValue(
            method = "shouldEntityAppearGlowing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isCurrentlyGlowing()Z"))
    private boolean seenByEyeOfTheForestThenGlow(boolean original, Entity entity) {
        return BetterClientConfig.HANDLER.instance().enableGlowingEnderEye
                ? entity.getType() == EntityType.EYE_OF_ENDER || original
                : original;
    }
}
