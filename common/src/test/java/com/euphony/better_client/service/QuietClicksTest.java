package com.euphony.better_client.service;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuietClicksTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void repeatedSyntheticClicksDoNotLeaveLocalPlaybackMuted() {
        for (int click = 0; click < 1_000; click++) {
            assertFalse(QuietClicks.active());
            QuietClicks.run(() -> assertTrue(QuietClicks.active()));
            assertFalse(QuietClicks.active());
        }
    }

    @Test
    void nestedClicksAndFailuresRestoreTheOuterScope() {
        QuietClicks.run(() -> {
            assertTrue(QuietClicks.active());
            assertThrows(IllegalStateException.class, () -> QuietClicks.run(() -> {
                assertTrue(QuietClicks.active());
                throw new IllegalStateException("failed menu click");
            }));
            assertTrue(QuietClicks.active());
        });
        assertFalse(QuietClicks.active());

        assertThrows(IllegalStateException.class, () -> QuietClicks.run(() -> {
            throw new IllegalStateException("failed transfer");
        }));
        assertFalse(QuietClicks.active());
    }

    @Test
    void delayedBundleSoundsAreSuppressedForRegisteredAndDirectHolders() {
        for (SoundEvent sound : new SoundEvent[] {
                SoundEvents.BUNDLE_INSERT, SoundEvents.BUNDLE_REMOVE_ONE, SoundEvents.BUNDLE_INSERT_FAIL}) {
            QuietClicks.run(() -> {});
            assertTrue(QuietClicks.suppress(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound)));
            // Network sounds may carry their event directly instead of a registry reference.
            SoundEvent equivalent = SoundEvent.createVariableRangeEvent(sound.location());
            assertTrue(QuietClicks.suppress(Holder.direct(equivalent)), sound.location().toString());
        }
    }

    @Test
    void delayedWindowDoesNotMuteUnrelatedSounds() {
        QuietClicks.run(() -> {});
        assertFalse(QuietClicks.suppress(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_BUTTON_CLICK.value())));
        assertFalse(QuietClicks.suppress(Holder.direct(SoundEvents.BUNDLE_DROP_CONTENTS)));
        assertFalse(QuietClicks.suppress(null));
    }
}
