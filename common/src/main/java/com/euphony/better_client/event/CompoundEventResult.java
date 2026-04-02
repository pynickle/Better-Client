package com.euphony.better_client.event;

public final class CompoundEventResult<T> {
    private static final CompoundEventResult<?> PASS = new CompoundEventResult<>(false, null);

    private final boolean interrupted;
    private final T object;

    private CompoundEventResult(boolean interrupted, T object) {
        this.interrupted = interrupted;
        this.object = object;
    }

    public static <T> CompoundEventResult<T> interruptTrue(T object) {
        return new CompoundEventResult<>(true, object);
    }

    @SuppressWarnings("unchecked")
    public static <T> CompoundEventResult<T> pass() {
        return (CompoundEventResult<T>) PASS;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public T object() {
        return object;
    }
}
