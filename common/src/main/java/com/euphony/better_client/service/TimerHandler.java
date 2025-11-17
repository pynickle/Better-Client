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
 * 管理所有试炼刷怪笼的冷却计时器
 * 使用嵌套的 ConcurrentHashMap: Level -> BlockPos -> Timer
 */
public class TimerHandler {
    private static final Map<ResourceKey<Level>, Map<BlockPos, Timer>> timers = new ConcurrentHashMap<>();

    /**
     * 判断试炼刷怪笼切换到此状态时是否应该删除计时器
     *
     * @param state 要测试的状态
     * @return true 如果切换到该状态应删除计时器
     */
    public static boolean shouldReset(TrialSpawnerState state) {
        // 当刷怪笼进入非冷却状态时，删除计时器
        return state != TrialSpawnerState.COOLDOWN && state != TrialSpawnerState.EJECTING_REWARD;
    }

    /**
     * 判断该状态是否应该触发计时器创建（如果还没有计时器）
     *
     * @param state 要测试的状态
     * @return true 如果切换到该状态应创建计时器
     */
    public static boolean shouldCreate(TrialSpawnerState state) {
        return state == TrialSpawnerState.COOLDOWN || state == TrialSpawnerState.EJECTING_REWARD;
    }

    /**
     * 检查指定位置是否有活跃的冷却计时器
     *
     * @param level 世界/维度
     * @param pos 试炼刷怪笼的位置
     * @return true 如果存在活跃的计时器
     */
    public static boolean hasTimer(Level level, BlockPos pos) {
        return getTimer(level, pos) != null;
    }

    /**
     * 为指定位置的试炼刷怪笼创建计时器
     *
     * @param level 世界/维度
     * @param pos 试炼刷怪笼的位置
     * @param startTime 计时器开始时间（游戏 tick）
     * @param cooldownTicks 冷却时长（tick）
     */
    public static void insertTimer(Level level, BlockPos pos, long startTime, long cooldownTicks) {
        if (level == null || pos == null) {
            LOGGER.warn("尝试插入计时器时传入 null 参数");
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        Map<BlockPos, Timer> levelTimers = timers.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());

        Timer timer = new Timer(startTime, cooldownTicks);
        levelTimers.put(pos.immutable(), timer);

        LOGGER.debug("为位置 {} 创建计时器，冷却时长：{} ticks", pos, cooldownTicks);
    }

    /**
     * 获取指定位置的计时器
     *
     * @param level 世界/维度
     * @param pos 试炼刷怪笼的位置
     * @return Timer 对象，如果不存在则返回 null
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
     * 删除指定位置的计时器
     *
     * @param level 世界/维度
     * @param pos 试炼刷怪笼的位置
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
            LOGGER.debug("删除位置 {} 的计时器，剩余时间：{} ticks", pos, removedTimer.getRemainingTicks(level.getGameTime()));
        }

        // 如果该维度没有计时器了，清理整个 Map
        if (levelTimers.isEmpty()) {
            timers.remove(dimension);
        }
    }

    /**
     * 清理所有已过期的计时器
     * 可以定期调用以防止内存泄漏
     *
     * @param level 要清理的世界
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
                LOGGER.debug("清理过期计时器：{}", entry.getKey());
            }
            return expired;
        });

        // 如果该维度没有计时器了，清理整个 Map
        if (levelTimers.isEmpty()) {
            timers.remove(level.dimension());
        }
    }

    /**
     * 清理指定维度的所有计时器
     *
     * @param dimension 要清理的维度
     */
    public static void clearDimension(ResourceKey<Level> dimension) {
        timers.remove(dimension);
        LOGGER.debug("清理维度 {} 的所有计时器", dimension);
    }

    /**
     * 清理所有计时器
     */
    public static void clearAll() {
        timers.clear();
        LOGGER.debug("清理所有计时器");
    }

    // ==================== 刷怪笼状态更新处理 ====================

    /**
     * 处理试炼刷怪笼方块更新，检测是否需要创建/删除计时器
     *
     * @param level 试炼刷怪笼所在的世界
     * @param pos 试炼刷怪笼的位置
     * @param blockState 试炼刷怪笼的方块状态
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
            LOGGER.error("处理刷怪笼方块更新时出错：{}", pos, e);
        }
    }

    /**
     * 处理试炼刷怪笼状态更新，检测是否需要创建/删除计时器
     *
     * @param level 试炼刷怪笼所在的世界
     * @param pos 试炼刷怪笼的位置
     * @param state 试炼刷怪笼的状态
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

            // 如果应该重置计时器，则删除现有计时器
            if (shouldReset(state)) {
                deleteTimer(level, pos);
            }
        } catch (Exception e) {
            LOGGER.error("处理刷怪笼状态更新时出错：{} -> {}", pos, state, e);
        }
    }

    /**
     * 获取当前活跃的计时器数量
     *
     * @return 所有维度中活跃的计时器总数
     */
    public static int getActiveTimerCount() {
        return timers.values().stream().mapToInt(Map::size).sum();
    }

    /**
     * 获取指定维度的计时器数量
     *
     * @param dimension 维度
     * @return 该维度中的计时器数量
     */
    public static int getTimerCount(ResourceKey<Level> dimension) {
        Map<BlockPos, Timer> levelTimers = timers.get(dimension);
        return levelTimers == null ? 0 : levelTimers.size();
    }
}
