package com.euphony.better_client.service;

import com.euphony.better_client.utils.records.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.euphony.better_client.BetterClient.LOGGER;
import static com.euphony.better_client.BetterClient.config;

/**
 * Manages cooldown timers for all trial spawners
 * Uses nested ConcurrentHashMap: Level -> BlockPos -> Timer
 */
public class TimerHandler {
    private static final Map<ResourceKey<Level>, Map<BlockPos, Timer>> timers = new ConcurrentHashMap<>();

    /**
 * Determines if the timer should be deleted when the trial spawner switches to this state
 *
 * @param state The state to test
 * @return true if switching to this state should delete the timer
 */
    public static boolean shouldReset(TrialSpawnerState state) {
        // Remove timer when spawner enters non-cooldown state
        return state != TrialSpawnerState.COOLDOWN && state != TrialSpawnerState.EJECTING_REWARD;
    }

    /**
 * Determines if this state should trigger timer creation (if no timer exists yet)
 *
 * @param state The state to test
 * @return true if switching to this state should create a timer
 */
    public static boolean shouldCreate(TrialSpawnerState state) {
        return state == TrialSpawnerState.COOLDOWN || state == TrialSpawnerState.EJECTING_REWARD;
    }

    /**
 * Checks if there's an active cooldown timer at the specified position
 *
 * @param level World/dimension
 * @param pos Trial spawner position
 * @return true if an active timer exists
 */
    public static boolean hasTimer(Level level, BlockPos pos) {
        return getTimer(level, pos) != null;
    }

    /**
 * Creates a timer for the trial spawner at the specified position
 *
 * @param level World/dimension
 * @param pos Trial spawner position
 * @param startTime Timer start time (game ticks)
 * @param cooldownTicks Cooldown duration (ticks)
 */
    public static void insertTimer(Level level, BlockPos pos, long startTime, long cooldownTicks) {
        if (level == null || pos == null) {
            LOGGER.warn("Null parameters passed when trying to insert timer");
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        Map<BlockPos, Timer> levelTimers = timers.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());

        Timer timer = new Timer(startTime, cooldownTicks);
        levelTimers.put(pos.immutable(), timer);

        LOGGER.debug("Created timer for position {}, cooldown duration: {} ticks", pos, cooldownTicks);
    }

    /**
 * Gets the timer at the specified position
 *
 * @param level World/dimension
 * @param pos Trial spawner position
 * @return Timer object, or null if none exists
 */
    public static Timer getTimer(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }

        Map<BlockPos, Timer> levelTimers = timers.get(level.dimension());
        if (levelTimers == null) {
            return null;
        }

        return levelTimers.get(pos);
    }

    /**
     * Deletes the timer at the specified position
     *
     * @param level World/dimension
     * @param pos Trial spawner position
     */
    public static void deleteTimer(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        Map<BlockPos, Timer> levelTimers = timers.get(dimension);

        if (levelTimers == null) {
            return;
        }

        Timer removedTimer = levelTimers.remove(pos);

        if (removedTimer != null) {
            LOGGER.debug("Deleted timer for position {}, remaining time: {} ticks", pos, removedTimer.getRemainingTicks(level.getGameTime()));
        }

        // If no timers remain in this dimension, clean up the entire Map
        if (levelTimers.isEmpty()) {
            timers.remove(dimension);
        }
    }

    /**
     * Cleans up all expired timers
     * Can be called periodically to prevent memory leaks
     *
     * @param level World to clean up
     */
    public static void cleanupExpiredTimers(Level level) {
        if (level == null) {
            return;
        }

        Map<BlockPos, Timer> levelTimers = timers.get(level.dimension());
        if (levelTimers == null) {
            return;
        }

        long currentTime = level.getGameTime();
        levelTimers.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired(currentTime);
            if (expired) {
                    LOGGER.debug("Cleaning up expired timer: {}", entry.getKey());
                }
            return expired;
        });

        // If no timers remain in this dimension, clean up the entire Map
        if (levelTimers.isEmpty()) {
            timers.remove(level.dimension());
        }
    }

    /**
     * Cleans up all timers in the specified dimension
     *
     * @param dimension Dimension to clean up
     */
    public static void clearDimension(ResourceKey<Level> dimension) {
        timers.remove(dimension);
        LOGGER.debug("Cleaned up all timers in dimension {}", dimension);
    }

    /**
     * Cleans up all timers
     */
    public static void clearAll() {
        timers.clear();
        LOGGER.debug("Cleaned up all timers");
    }

    // ==================== Spawner State Update Handling ====================

    /**
 * Handles trial spawner block updates, checking if timers need to be created/deleted
 *
 * @param level World containing the trial spawner
 * @param pos Trial spawner position
 * @param blockState Trial spawner block state
 */
    public static void onSpawnerBlockUpdate(Level level, BlockPos pos, BlockState blockState) {
        if (!config.enableTrialSpawnerTimer) {
            return;
        }

        try {
            TrialSpawnerState spawnerState =
                    blockState.getValue(net.minecraft.world.level.block.TrialSpawnerBlock.STATE);
            onSpawnerStateUpdate(level, pos, spawnerState);
        } catch (Exception e) {
            LOGGER.error("Error handling spawner block update at: {}", pos, e);
        }
    }

    /**
 * Handles trial spawner state updates, checking if timers need to be created/deleted
 *
 * @param level World containing the trial spawner
 * @param pos Trial spawner position
 * @param state Trial spawner state
 */
    public static void onSpawnerStateUpdate(Level level, BlockPos pos, TrialSpawnerState state) {
        if (!config.enableTrialSpawnerTimer || level == null || pos == null || state == null) {
            return;
        }

        try {
            // 如果应该创建计时器且当前没有计时器，则创建
            if (shouldCreate(state) && !hasTimer(level, pos)) {
                long cooldownTicks = config.trialSpawnerCooldown * 20;
                insertTimer(level, pos, level.getGameTime(), cooldownTicks);
                return;
            }

            // If timer should be reset, delete existing timer
            if (shouldReset(state)) {
                deleteTimer(level, pos);
            }
        } catch (Exception e) {
            LOGGER.error("Error handling spawner state update at: {} -> {}", pos, state, e);
        }
    }

    /**
     * Gets the number of currently active timers
     *
     * @return Total number of active timers across all dimensions
     */
    public static int getActiveTimerCount() {
        return timers.values().stream().mapToInt(Map::size).sum();
    }

    /**
     * Gets the number of timers in the specified dimension
     *
     * @param dimension Dimension
     * @return Number of timers in this dimension
     */
    public static int getTimerCount(ResourceKey<Level> dimension) {
        Map<BlockPos, Timer> levelTimers = timers.get(dimension);
        return levelTimers == null ? 0 : levelTimers.size();
    }
}
