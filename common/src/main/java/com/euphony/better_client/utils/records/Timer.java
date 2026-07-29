package com.euphony.better_client.utils.records;

public record Timer(long startTime, long cooldownTicks) {
    public Timer {
    }

    public long getTimerEnd() {
        return startTime + cooldownTicks;
    }

    public long getRemainingTicks(long currentTime) {
        long remaining = getTimerEnd() - currentTime;
        return Math.max(0, remaining);
    }

    public boolean isExpired(long currentTime) {
        return currentTime >= getTimerEnd();
    }

    public double getProgress(long currentTime) {
        if (cooldownTicks == 0) {
            return 1.0;
        }
        long elapsed = currentTime - startTime;
        return Math.min(1.0, Math.max(0.0, (double) elapsed / cooldownTicks));
    }
}
