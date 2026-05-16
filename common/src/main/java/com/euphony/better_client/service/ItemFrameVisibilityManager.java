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
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.euphony.better_client.BetterClient.LOGGER;

public class ItemFrameVisibilityManager {
    // Only holds data for the currently active world to save memory
    private static final Set<BlockPos> currentWorldHiddenFrames = ConcurrentHashMap.newKeySet();
    private static final Object SAVE_LOCK = new Object();

    private static final Path BASE_PATH = DataUtils.getDataDir();
    private static final Path ITEM_FRAME_INVISIBILITY_PATH = BASE_PATH.resolve("item_frame_visibility.json");
    private static JsonObject persistedFrameData = new JsonObject();
    private static boolean persistedFrameDataLoaded;
    private static boolean saveScheduled;
    private static boolean saveDirty;
    private static String currentWorldId = "";

    private ItemFrameVisibilityManager() {}

    public static void clientLevelLoad(ClientLevel level) {
        loadForCurrentWorld(level);
    }

    /**
     * Toggles the visibility state of an item frame at the given position.
     * Triggers an asynchronous save.
     */
    public static void toggleFrameVisibility(BlockPos pos) {
        if (currentWorldHiddenFrames.contains(pos)) {
            currentWorldHiddenFrames.remove(pos);
        } else {
            currentWorldHiddenFrames.add(pos);
        }
        saveToFileAsync();
    }

    /**
     * Checks if the item frame at the given position should be hidden.
     */
    public static boolean isFrameHidden(BlockPos pos) {
        return currentWorldHiddenFrames.contains(pos);
    }

    /**
     * Reloads data specific to the current world.
     * Should be called when the client joins a world/server.
     */
    public static void loadForCurrentWorld(ClientLevel level) {
        currentWorldHiddenFrames.clear();
        ensureFrameDataLoaded();

        String worldId = LevelUtils.getCurrentWorldKey(level);
        synchronized (SAVE_LOCK) {
            currentWorldId = worldId;
            if (persistedFrameData.has(worldId)) {
                JsonArray positions = persistedFrameData.getAsJsonArray(worldId);
                for (JsonElement elem : positions) {
                    JsonObject obj = elem.getAsJsonObject();
                    int x = obj.get("x").getAsInt();
                    int y = obj.get("y").getAsInt();
                    int z = obj.get("z").getAsInt();
                    currentWorldHiddenFrames.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    /**
     * Saves the current world's data to file asynchronously.
     * Reads the existing file, updates the current world key, and writes back.
     */
    private static void saveToFileAsync() {
        ensureFrameDataLoaded();
        synchronized (SAVE_LOCK) {
            updateCurrentWorldSnapshot(currentWorldId);
            saveDirty = true;
            if (saveScheduled) {
                return;
            }
            saveScheduled = true;
        }

        Util.ioPool().execute(() -> {
            while (true) {
                JsonObject snapshot;
                synchronized (SAVE_LOCK) {
                    if (!saveDirty) {
                        saveScheduled = false;
                        return;
                    }

                    saveDirty = false;
                    snapshot = persistedFrameData.deepCopy();
                }

                try {
                    if (Files.notExists(BASE_PATH)) {
                        Files.createDirectories(BASE_PATH);
                    }
                    Files.writeString(ITEM_FRAME_INVISIBILITY_PATH, JsonUtils.GSON.toJson(snapshot), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    LOGGER.warn("[BetterClient] Failed to save item frame data", e);
                    return;
                }
            }
        });
    }

    private static void ensureFrameDataLoaded() {
        synchronized (SAVE_LOCK) {
            if (persistedFrameDataLoaded) {
                return;
            }

            if (Files.notExists(ITEM_FRAME_INVISIBILITY_PATH)) {
                persistedFrameData = new JsonObject();
                persistedFrameDataLoaded = true;
                return;
            }

            try (Reader reader = Files.newBufferedReader(ITEM_FRAME_INVISIBILITY_PATH, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                persistedFrameData = root == null ? new JsonObject() : root;
            } catch (Exception e) {
                persistedFrameData = new JsonObject();
                LOGGER.error("[BetterClient] Failed to load item frame data: ", e);
            }

            persistedFrameDataLoaded = true;
        }
    }

    private static void updateCurrentWorldSnapshot(String worldId) {
        JsonArray positions = new JsonArray();
        for (BlockPos pos : currentWorldHiddenFrames) {
            JsonObject p = new JsonObject();
            p.addProperty("x", pos.getX());
            p.addProperty("y", pos.getY());
            p.addProperty("z", pos.getZ());
            positions.add(p);
        }
        persistedFrameData.add(worldId, positions);
    }
}
