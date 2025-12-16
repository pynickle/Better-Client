package com.euphony.better_client.service;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.euphony.better_client.BetterClient.LOGGER;

public class ItemFrameVisibilityManager {
    private static final ItemFrameVisibilityManager INSTANCE = new ItemFrameVisibilityManager();

    private final Map<String, Set<BlockPos>> hiddenFramesByWorld = new HashMap<>();

    private ItemFrameVisibilityManager() {
        loadFromFile();
    }

    public static ItemFrameVisibilityManager getInstance() {
        return INSTANCE;
    }

    /**
     * 获取当前世界的唯一标识符
     */
    private String getCurrentWorldKey() {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;

        if (level == null) {
            return "unknown";
        }

        // 组合维度和服务器信息作为唯一标识
        String dimension = level.dimension().registry().toString();
        String server;
        if (client.hasSingleplayerServer()) {
            // 单机存档
            Path worldPath = client.getSingleplayerServer()
                    .getWorldPath(LevelResource.LEVEL_DATA_FILE)
                    .getParent()
                    .getFileName();
            server = "local:" + worldPath;
        } else {
            // 服务器
            String serverAddr = client.getCurrentServer() != null ? client.getCurrentServer().ip : "unknown_server";
            server = "remote:" + serverAddr;
        }

        return server + "_" + dimension;
    }

    /**
     * 切换物品展示框的隐形状态
     */
    public void toggleFrameVisibility(BlockPos pos) {
        String worldKey = getCurrentWorldKey();
        Set<BlockPos> hiddenFrames = hiddenFramesByWorld.computeIfAbsent(worldKey, k -> new HashSet<>());

        if (hiddenFrames.contains(pos)) {
            hiddenFrames.remove(pos);
        } else {
            hiddenFrames.add(pos);
        }
        saveToFile();
    }

    /**
     * 检查物品展示框是否应该隐形
     */
    public boolean isFrameHidden(BlockPos pos) {
        String worldKey = getCurrentWorldKey();
        Set<BlockPos> hiddenFrames = hiddenFramesByWorld.get(worldKey);
        return hiddenFrames != null && hiddenFrames.contains(pos);
    }

    /**
     * 清理当前世界的数据（可选，用于内存管理）
     */
    public void clearCurrentWorld() {
        String worldKey = getCurrentWorldKey();
        hiddenFramesByWorld.remove(worldKey);
        saveToFile();
    }

    /**
     * 清理所有数据
     */
    public void clearAll() {
        hiddenFramesByWorld.clear();
        saveToFile();
    }

    /**
     * 将所有世界数据保存到文件
     */
    private void saveToFile() {
        Path savePath = getSavePath();

        try {
            Files.createDirectories(savePath.getParent());
            JsonObject root = getJsonObject();

            try (Writer writer = Files.newBufferedWriter(savePath, StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }

        } catch (IOException e) {
            LOGGER.warn("[BetterClient] 保存展示框数据失败", e);
        }
    }

    private @NotNull JsonObject getJsonObject() {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, Set<BlockPos>> entry : hiddenFramesByWorld.entrySet()) {
            JsonArray positions = new JsonArray();
            for (BlockPos pos : entry.getValue()) {
                JsonObject p = new JsonObject();
                p.addProperty("x", pos.getX());
                p.addProperty("y", pos.getY());
                p.addProperty("z", pos.getZ());
                positions.add(p);
            }
            root.add(entry.getKey(), positions);
        }
        return root;
    }

    /**
     * 从文件加载数据
     */
    private void loadFromFile() {
        Path savePath = getSavePath();

        if (!Files.exists(savePath)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            hiddenFramesByWorld.clear();

            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String worldKey = entry.getKey();
                JsonArray positions = entry.getValue().getAsJsonArray();
                Set<BlockPos> posSet = new HashSet<>();
                for (JsonElement elem : positions) {
                    JsonObject obj = elem.getAsJsonObject();
                    int x = obj.get("x").getAsInt();
                    int y = obj.get("y").getAsInt();
                    int z = obj.get("z").getAsInt();
                    posSet.add(new BlockPos(x, y, z));
                }
                hiddenFramesByWorld.put(worldKey, posSet);
            }
        } catch (Exception e) {
            LOGGER.error("[BetterClient] 加载展示框数据失败: ", e);
        }
    }

    private static Path getSavePath() {
        Minecraft client = Minecraft.getInstance();
        return client.gameDirectory.toPath().resolve("better-client").resolve("item_frame_visibility.json");
    }
}
