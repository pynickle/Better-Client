package com.euphony.better_client.platform;

import java.nio.file.Path;
import java.util.Set;

public interface BetterClientPlatform {
    Path getConfigDirectory();

    boolean isModLoaded(String modId);

    Set<String> getLoadedModIds();

    String getModDisplayName(String modId);

    PlatformType getPlatformType();
}
