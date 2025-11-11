package com.euphony.better_client.utils;

/**
 * 时间转换和格式化工具类
 */
public class TimeUtils {
    private static final int TICKS_PER_SECOND = 20;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 3600;

    /**
     * 将 tick 转换为格式化的时间字符串（MM:SS）
     *
     * @param ticks 时间（tick）
     * @return 格式化的时间字符串，例如 "15:30"
     */
    public static String formatTicks(long ticks) {
        long totalSeconds = ticks / TICKS_PER_SECOND;
        long minutes = totalSeconds / SECONDS_PER_MINUTE;
        long seconds = totalSeconds % SECONDS_PER_MINUTE;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 将 tick 转换为格式化的时间字符串（HH:MM:SS）
     * 用于超过 1 小时的情况
     *
     * @param ticks 时间（tick）
     * @return 格式化的时间字符串，例如 "01:15:30"
     */
    public static String formatTicksLong(long ticks) {
        long totalSeconds = ticks / TICKS_PER_SECOND;
        long hours = totalSeconds / SECONDS_PER_HOUR;
        long minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        long seconds = totalSeconds % SECONDS_PER_MINUTE;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * 将 tick 转换为简洁的时间字符串
     * 自动选择合适的格式
     *
     * @param ticks 时间（tick）
     * @return 格式化的时间字符串
     */
    public static String formatTicksCompact(long ticks) {
        long totalSeconds = ticks / TICKS_PER_SECOND;

        if (totalSeconds >= SECONDS_PER_HOUR) {
            return formatTicksLong(ticks);
        } else {
            return formatTicks(ticks);
        }
    }

    /**
     * 将 tick 转换为秒
     *
     * @param ticks 时间（tick）
     * @return 秒数
     */
    public static long ticksToSeconds(long ticks) {
        return ticks / TICKS_PER_SECOND;
    }

    /**
     * 将秒转换为 tick
     *
     * @param seconds 秒数
     * @return tick 数
     */
    public static long secondsToTicks(long seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    /**
     * 将分钟转换为 tick
     *
     * @param minutes 分钟数
     * @return tick 数
     */
    public static long minutesToTicks(long minutes) {
        return minutes * SECONDS_PER_MINUTE * TICKS_PER_SECOND;
    }
}