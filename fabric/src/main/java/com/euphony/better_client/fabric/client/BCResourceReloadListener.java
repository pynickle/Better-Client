package com.euphony.better_client.fabric.client;

import static com.euphony.better_client.client.events.BiomeTitleEvent.NAME_CACHE;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.NotNull;

public class BCResourceReloadListener implements PreparableReloadListener {
    @Override
    public @NotNull CompletableFuture<Void> reload(
            SharedState sharedState, Executor executor, PreparationBarrier preparationBarrier, Executor executor2) {
        return CompletableFuture.runAsync(NAME_CACHE::clear, executor).thenCompose(preparationBarrier::wait);
    }
}
