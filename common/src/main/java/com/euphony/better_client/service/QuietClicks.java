package com.euphony.better_client.service;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Suppresses the item sounds that vanilla plays from the bundle panel's synthetic menu clicks.
 *
 * <p>Bundle clicks play insertion/removal/failure sounds through {@code LocalPlayer.playSound}.
 * That override uses {@code ClientLevel.playLocalSound}, bypassing {@code playSeededSound}.
 * LocalPlayerMixin checks the active scope before playback so automated transfers do not allocate
 * a sound channel for each bundle click. Ordinary local clicks are audible as soon as the scope ends.
 *
 * <p>Scope is a simple depth counter rather than a boolean so nested menu clicks stay quiet, and it
 * is a plain static because the panel drives the menu on the client thread only.
 *
 * <p>ClientLevelMixin also filters bundle sounds received from the server for a short window.
 * Vanilla normally excludes the acting player from its broadcast; this covers servers that echo
 * those sounds anyway. The window applies only to that network route, not ordinary local clicks.
 */
public final class QuietClicks {
    private static int depth;
    /** Nano-time before which bundle item sounds arriving from the server are suppressed. */
    private static long quietUntil;

    private QuietClicks() {}

    /** Runs {@code action} with the item sounds of any menu click it performs suppressed. */
    public static void run(Runnable action) {
        depth++;
        // Refresh the window for every synthetic click, including server-side follow-ups.
        quietUntil = Math.max(quietUntil, System.nanoTime() + 2_000_000_000L);
        try {
            action.run();
        } finally {
            depth--;
        }
    }

    /** True while a panel-issued menu click is in progress. */
    public static boolean active() {
        return depth > 0;
    }

    /** Whether the given world sound should be dropped (client prediction or a delayed server echo). */
    public static boolean suppress(Holder<SoundEvent> sound) {
        if (depth > 0) {
            return true;
        }
        if (sound == null || System.nanoTime() >= quietUntil) {
            return false;
        }
        // Holder.Direct.is(identifier) always returns false; compare the event's own identifier.
        Identifier location = sound.value().location();
        return location.equals(SoundEvents.BUNDLE_INSERT.location())
                || location.equals(SoundEvents.BUNDLE_REMOVE_ONE.location())
                || location.equals(SoundEvents.BUNDLE_INSERT_FAIL.location());
    }
}
