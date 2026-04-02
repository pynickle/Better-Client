package com.euphony.better_client.platform;

import java.nio.file.Path;
import java.util.Set;

public final class Platform {
    private Platform() {}

    public static Path getConfigFolder() {
        return PlatformServices.getPlatform().getConfigDirectory();
    }

    public static boolean isModLoaded(String modId) {
        return PlatformServices.getPlatform().isModLoaded(modId);
    }

    public static boolean isFabric() {
        return PlatformServices.getPlatform().getPlatformType() == PlatformType.FABRIC;
    }

    public static boolean isNeoForge() {
        return PlatformServices.getPlatform().getPlatformType() == PlatformType.NEOFORGE;
    }

    public static Set<String> getModIds() {
        return PlatformServices.getPlatform().getLoadedModIds();
    }

    public static LoadedMod getMod(String modId) {
        return new LoadedMod(PlatformServices.getPlatform().getModDisplayName(modId));
    }

    public record LoadedMod(String name) {
        public String getName() {
            return name;
        }
    }
}
