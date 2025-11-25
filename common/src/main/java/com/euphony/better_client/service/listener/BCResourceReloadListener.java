package com.euphony.better_client.service.listener;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.euphony.better_client.client.events.BiomeTitleEvent.NAME_CACHE;

public class BCResourceReloadListener implements PreparableReloadListener {
    @Override
    public @NotNull CompletableFuture<Void> reload(
            SharedState sharedState, Executor executor, PreparationBarrier preparationBarrier, Executor executor2) {
        return CompletableFuture.runAsync(NAME_CACHE::clear, executor).thenCompose(preparationBarrier::wait);
    }
}
