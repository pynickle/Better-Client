package com.euphony.better_client.config.option;

public enum AutoAdaptMode {
    AUTO,
    ENABLED,
    DISABLED;

    public boolean resolve(boolean autoValue) {
        return switch (this) {
            case AUTO -> autoValue;
            case ENABLED -> true;
            case DISABLED -> false;
        };
    }
}
