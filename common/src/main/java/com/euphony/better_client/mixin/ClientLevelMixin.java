package com.euphony.better_client.mixin;

import com.euphony.better_client.service.QuietClicks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Filters server-sent bundle sounds following synthetic menu clicks.
 *
 * <p>The two sound packet handlers use the positional and entity-bound playSeededSound overloads.
 * Locally predicted bundle clicks bypass both; LocalPlayerMixin handles those separately.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(
            method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("HEAD"),
            cancellable = true)
    private void better_client$silencePanelClicks(
            Entity except, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source,
            float volume, float pitch, long seed, CallbackInfo ci) {
        if (QuietClicks.suppress(sound)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "playSeededSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("HEAD"),
            cancellable = true)
    private void better_client$silencePanelEntityClicks(
            Entity except, Entity sourceEntity, Holder<SoundEvent> sound, SoundSource source,
            float volume, float pitch, long seed, CallbackInfo ci) {
        if (QuietClicks.suppress(sound)) {
            ci.cancel();
        }
    }
}
