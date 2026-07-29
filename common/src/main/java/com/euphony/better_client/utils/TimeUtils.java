package com.euphony.better_client.utils;

public class TimeUtils {
    private static final int TICKS_PER_SECOND = 20;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 3600;

    public static String formatTicks(long ticks) {
        long totalSeconds = ticks / TICKS_PER_SECOND;
        long minutes = totalSeconds / SECONDS_PER_MINUTE;
        long seconds = totalSeconds % SECONDS_PER_MINUTE;
        return formatTwoPartTime(minutes, seconds);
    }

    public static String formatTicksLong(long ticks) {
        long totalSeconds = ticks / TICKS_PER_SECOND;
        long hours = totalSeconds / SECONDS_PER_HOUR;
        long minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        long seconds = totalSeconds % SECONDS_PER_MINUTE;

        if (hours > 0) {
            StringBuilder builder = new StringBuilder(8);
            appendTwoDigits(builder, hours);
            builder.append(':');
            appendTwoDigits(builder, minutes);
            builder.append(':');
            appendTwoDigits(builder, seconds);
            return builder.toString();
        } else {
            return formatTwoPartTime(minutes, seconds);
        }
    }

    public static String formatTicksCompact(long ticks) {
        long totalSeconds = ticks / TICKS_PER_SECOND;

        if (totalSeconds >= SECONDS_PER_HOUR) {
            return formatTicksLong(ticks);
        } else {
            return formatTicks(ticks);
        }
    }

    public static long ticksToSeconds(long ticks) {
        return ticks / TICKS_PER_SECOND;
    }

    public static long secondsToTicks(long seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    public static long minutesToTicks(long minutes) {
        return minutes * SECONDS_PER_MINUTE * TICKS_PER_SECOND;
    }

    private static String formatTwoPartTime(long first, long second) {
        StringBuilder builder = new StringBuilder(5);
        appendTwoDigits(builder, first);
        builder.append(':');
        appendTwoDigits(builder, second);
        return builder.toString();
    }

    private static void appendTwoDigits(StringBuilder builder, long value) {
        if (value < 10) {
            builder.append('0');
        }
        builder.append(value);
    }
}
