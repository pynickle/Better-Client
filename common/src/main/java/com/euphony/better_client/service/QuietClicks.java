package com.euphony.better_client.service;

/**
 * Suppresses the item sounds that vanilla plays from the bundle panel's synthetic menu clicks.
 *
 * <p>The panel performs an ordinary menu click per transferred item, and every bundle click runs
 * {@code BundleItem.overrideOtherStackedOnMe} / {@code overrideStackedOnOther}, which plays a real
 * {@code BUNDLE_INSERT}, {@code BUNDLE_REMOVE_ONE} or {@code BUNDLE_INSERT_FAIL} world sound. A
 * single stack transfer therefore requested dozens of sounds in a burst, and the sound engine only
 * reclaims an OpenAL channel once its source reports stopped. Repeated transfers exhausted the
 * static channel pool, after which every request logged "Maximum sound pool size 247 reached" and
 * "Failed to create new sound handle" for the rest of the session.
 *
 * <p>Scope is a simple depth counter rather than a boolean so nested menu clicks stay quiet, and it
 * is a plain static because the panel drives the menu on the client thread only.
 */
public final class QuietClicks {
    private static int depth;

    private QuietClicks() {}

    /** Runs {@code action} with the item sounds of any menu click it performs suppressed. */
    public static void run(Runnable action) {
        depth++;
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
}
