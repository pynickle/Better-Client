package com.euphony.better_client.utils.records;

/**
 * 表示试炼刷怪笼的冷却计时器
 * 存储开始时间和冷却时长（单位：tick）
 *
 * @param startTime     计时器开始时的游戏时间（tick）
 * @param cooldownTicks 冷却时长（tick）
 */
public record Timer(long startTime, long cooldownTicks) {
    /**
     * 创建一个新的计时器
     *
     * @param startTime     计时器开始时的游戏时间（level.getGameTime()）
     * @param cooldownTicks 冷却时长（tick，30 分钟 = 36000 ticks）
     */
    public Timer {
    }

    /**
     * 获取计时器结束时的游戏时间
     *
     * @return 计时器应该结束的游戏时间（tick）
     */
    public long getTimerEnd() {
        return startTime + cooldownTicks;
    }

    /**
     * 获取剩余时间
     *
     * @param currentTime 当前游戏时间（level.getGameTime()）
     * @return 剩余时间（tick），如果已过期则返回 0
     */
    public long getRemainingTicks(long currentTime) {
        long remaining = getTimerEnd() - currentTime;
        return Math.max(0, remaining);
    }

    /**
     * 检查计时器是否已结束
     *
     * @param currentTime 当前游戏时间（level.getGameTime()）
     * @return true 如果计时器已结束
     */
    public boolean isExpired(long currentTime) {
        return currentTime >= getTimerEnd();
    }

    /**
     * 获取冷却进度（0.0 到 1.0）
     *
     * @param currentTime 当前游戏时间
     * @return 已完成的冷却进度百分比
     */
    public double getProgress(long currentTime) {
        if (cooldownTicks == 0) {
            return 1.0;
        }
        long elapsed = currentTime - startTime;
        return Math.min(1.0, Math.max(0.0, (double) elapsed / cooldownTicks));
    }
}