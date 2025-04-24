package com.euphony.better_client.fabric.client;

import com.euphony.better_client.utils.Utils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.euphony.better_client.client.events.BiomeTitleEvent.NAME_CACHE;

public class BCResourceReloadListener implements IdentifiableResourceReloadListener {
    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        return CompletableFuture.runAsync(NAME_CACHE::clear, executor).thenCompose(preparationBarrier::wait);
    }

    @Override
    public ResourceLocation getFabricId() {
        return Utils.prefix("name_cache_clear");
    }
}
