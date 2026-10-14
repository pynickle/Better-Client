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
 * Keeps the bundle panel's synthetic clicks out of the sound engine.
 *
 * <p>A panel transfer is one vanilla menu click per item, and each bundle click plays a real
 * {@code BUNDLE_INSERT}, {@code BUNDLE_REMOVE_ONE} or {@code BUNDLE_INSERT_FAIL}. Requesting a
 * sound per item in a burst filled the 247-channel static pool faster than OpenAL could reclaim
 * the finished sources, so the session then logged "Maximum sound pool size 247 reached" and
 * "Failed to create new sound handle" for every later sound.
 *
 * <p>{@code ClientLevel#playSeededSound} is the single funnel every world sound passes through,
 * which covers the {@code Player#playSound} and positional ({@code BUNDLE_DROP_CONTENTS}) routes
 * alike.
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
        if (QuietClicks.active()) {
            ci.cancel();
        }
    }
}
