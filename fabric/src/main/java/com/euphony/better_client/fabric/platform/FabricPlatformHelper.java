package com.euphony.better_client.fabric.platform;

import com.euphony.better_client.platform.BetterClientPlatform;
import com.euphony.better_client.platform.PlatformType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public final class FabricPlatformHelper implements BetterClientPlatform {
    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Set<String> getLoadedModIds() {
        return FabricLoader.getInstance().getAllMods().stream()
                .map(mod -> mod.getMetadata().getId())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getModDisplayName(String modId) {
        return FabricLoader.getInstance()
                .getModContainer(modId)
                .map(container -> container.getMetadata().getName())
                .orElse(modId);
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.FABRIC;
    }
}
