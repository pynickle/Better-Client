package com.euphony.better_client.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.euphony.better_client.BetterClient.config;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {
    @ModifyVariable(method = "confirmWorldCreation", at = @At("HEAD"), argsOnly = true)
    private static Lifecycle alwaysStable(Lifecycle lifecycle) {
        if(config.enableNoExperimentalWarning) {
            return Lifecycle.stable();
        }
        return lifecycle;
    }

    @Redirect(
            method = "openWorldCheckWorldStemCompatibility",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/WorldData;worldGenSettingsLifecycle()Lcom/mojang/serialization/Lifecycle;"
            )
    )
    private Lifecycle alwaysReturnStableLifecycle(WorldData worldData) {
        if(config.enableNoExperimentalWarning) {
            return Lifecycle.stable();
        }
        return worldData.worldGenSettingsLifecycle();
    }
}
