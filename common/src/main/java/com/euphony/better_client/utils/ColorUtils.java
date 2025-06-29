package com.euphony.better_client.utils;

public class ColorUtils {
    public static final int COLOR_UNKNOWN = 6953882;
    public static final int COLOR_GOOD = 5025616;
    public static final int COLOR_NORMAL = 16771899;
    public static final int COLOR_BAD = 16088064;
    public static final int COLOR_VERY_BAD = 12000284;

    public static int getPingTextColor(int latency) {
        if(latency < 0) {
            return COLOR_UNKNOWN;
        } else if (latency < 150) {
            return COLOR_GOOD;
        } else if (latency < 300) {
            return COLOR_NORMAL;
        } else if(latency < 600) {
            return COLOR_BAD;
        } else {
            return COLOR_VERY_BAD;
        }
    }
}
