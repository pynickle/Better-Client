package com.euphony.better_client.service;

import com.euphony.better_client.utils.JsonUtils;
import com.euphony.better_client.utils.mc.DataUtils;
import com.euphony.better_client.utils.mc.LevelUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.euphony.better_client.BetterClient.LOGGER;

public class ItemFrameVisibilityManager {
    // Only holds data for the currently active world to save memory
    private static final Set<BlockPos> currentWorldHiddenFrames = new HashSet<>();

    private static final Path BASE_PATH = DataUtils.getDataDir();
    private static final Path ITEM_FRAME_INVISIBILITY_PATH = BASE_PATH.resolve("item_frame_visibility.json");

    private ItemFrameVisibilityManager() {}

    public static void clientLevelLoad(ClientLevel level) {
        loadForCurrentWorld(level);
    }

    /**
     * Toggles the visibility state of an item frame at the given position.
     * Triggers an asynchronous save.
     */
    public static void toggleFrameVisibility(BlockPos pos) {
        synchronized (currentWorldHiddenFrames) {
            if (currentWorldHiddenFrames.contains(pos)) {
                currentWorldHiddenFrames.remove(pos);
            } else {
                currentWorldHiddenFrames.add(pos);
            }
        }
        saveToFileAsync();
    }

    /**
     * Checks if the item frame at the given position should be hidden.
     */
    public static boolean isFrameHidden(BlockPos pos) {
        synchronized (currentWorldHiddenFrames) {
            return currentWorldHiddenFrames.contains(pos);
        }
    }

    /**
     * Reloads data specific to the current world.
     * Should be called when the client joins a world/server.
     */
    public static void loadForCurrentWorld(ClientLevel level) {
        synchronized (currentWorldHiddenFrames) {
            currentWorldHiddenFrames.clear();

            if (Files.notExists(ITEM_FRAME_INVISIBILITY_PATH)) {
                return;
            }

            try (Reader reader = Files.newBufferedReader(ITEM_FRAME_INVISIBILITY_PATH, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                String worldId = LevelUtils.getCurrentWorldKey(level);
                if (root.has(worldId)) {
                    JsonArray positions = root.getAsJsonArray(worldId);
                    for (JsonElement elem : positions) {
                        JsonObject obj = elem.getAsJsonObject();
                        int x = obj.get("x").getAsInt();
                        int y = obj.get("y").getAsInt();
                        int z = obj.get("z").getAsInt();
                        currentWorldHiddenFrames.add(new BlockPos(x, y, z));
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[BetterClient] Failed to load item frame data: ", e);
            }
        }
    }

    /**
     * Saves the current world's data to file asynchronously.
     * Reads the existing file, updates the current world key, and writes back.
     */
    private static void saveToFileAsync() {
        // Create a snapshot of the set to avoid ConcurrentModificationException during async write
        final Set<BlockPos> framesSnapshot;
        synchronized (currentWorldHiddenFrames) {
            framesSnapshot = new HashSet<>(currentWorldHiddenFrames);
        }

        CompletableFuture.runAsync(() -> {
            try {
                if (Files.notExists(BASE_PATH)) {
                    Files.createDirectories(BASE_PATH);
                }

                // 1. Read existing data (to preserve other worlds)
                JsonObject root;
                if (Files.exists(ITEM_FRAME_INVISIBILITY_PATH)) {
                    try (Reader reader =
                            Files.newBufferedReader(ITEM_FRAME_INVISIBILITY_PATH, StandardCharsets.UTF_8)) {
                        root = JsonUtils.GSON.fromJson(reader, JsonObject.class);
                    } catch (Exception e) {
                        root = new JsonObject();
                    }
                } else {
                    root = new JsonObject();
                }

                if (root == null) root = new JsonObject();

                // 2. Update current world data
                JsonArray positions = new JsonArray();
                for (BlockPos pos : framesSnapshot) {
                    JsonObject p = new JsonObject();
                    p.addProperty("x", pos.getX());
                    p.addProperty("y", pos.getY());
                    p.addProperty("z", pos.getZ());
                    positions.add(p);
                }
                root.add(LevelUtils.getCurrentWorldKey(), positions);

                // 3. Write back to file
                Files.writeString(ITEM_FRAME_INVISIBILITY_PATH, JsonUtils.GSON.toJson(root), StandardCharsets.UTF_8);

            } catch (IOException e) {
                LOGGER.warn("[BetterClient] Failed to save item frame data", e);
            }
        });
    }
}
