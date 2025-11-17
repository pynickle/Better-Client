package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IHasPlayTime;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;

@Mixin(LevelStorageSource.class)
public class LevelStorageSourceMixin {
    @Inject(at = @At("RETURN"), method = "makeLevelSummary")
    public void onMakeLevelSummary(
            Dynamic<?> dynamic,
            LevelStorageSource.LevelDirectory levelDirectory,
            boolean locked,
            CallbackInfoReturnable<LevelSummary> cir) {
        LevelSummary summary = cir.getReturnValue();
        if (!(summary instanceof IHasPlayTime playTimeSummary)) return;

        Path statsDir = levelDirectory.resourcePath(LevelResource.PLAYER_STATS_DIR);
        File dir = statsDir.toFile();
        if (!dir.isDirectory()) return;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) return;

        int totalTicks = 0;
        for (File file : files) {
            try (Reader reader = new BufferedReader(new FileReader(file))) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject custom = root.getAsJsonObject("stats").getAsJsonObject("minecraft:custom");
                if (custom.has("minecraft:play_time")) {
                    totalTicks += custom.get("minecraft:play_time").getAsInt();
                }
            } catch (Exception ignored) {
                // 忽略损坏的统计文件
            }
        }

        if (totalTicks > 0) {
            playTimeSummary.better_client$setPlayTimeTicks(totalTicks);
        }
    }
}
