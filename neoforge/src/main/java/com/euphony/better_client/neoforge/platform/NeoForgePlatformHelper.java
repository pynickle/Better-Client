package com.euphony.better_client.neoforge.platform;

import com.euphony.better_client.platform.BetterClientPlatform;
import com.euphony.better_client.platform.PlatformType;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public final class NeoForgePlatformHelper implements BetterClientPlatform {
    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Set<String> getLoadedModIds() {
        return ModList.get().getMods().stream().map(info -> info.getModId()).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getModDisplayName(String modId) {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(modId);
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.NEOFORGE;
    }
}
