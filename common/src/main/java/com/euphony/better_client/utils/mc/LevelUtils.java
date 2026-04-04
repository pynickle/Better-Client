package com.euphony.better_client.utils.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public class LevelUtils {
    public static String getCurrentSessionKey() {
        Minecraft client = Minecraft.getInstance();
        return getCurrentSessionKey(client, client.level);
    }

    public static String getCurrentSessionKey(ClientLevel level) {
        return getCurrentSessionKey(Minecraft.getInstance(), level);
    }

    public static String getCurrentSessionKey(Minecraft client, ClientLevel level) {
        if (level == null) {
            return "unknown";
        }

        if (client.hasSingleplayerServer() && client.getSingleplayerServer() != null) {
            Path worldPath = client.getSingleplayerServer()
                    .getWorldPath(LevelResource.LEVEL_DATA_FILE)
                    .getParent()
                    .getFileName();
            return "local:" + worldPath;
        }

        String serverAddr = client.getCurrentServer() != null ? client.getCurrentServer().ip : "unknown_server";
        return "remote:" + serverAddr;
    }

    public static String getCurrentSessionName() {
        Minecraft client = Minecraft.getInstance();
        return getCurrentSessionName(client, client.level);
    }

    public static String getCurrentSessionName(Minecraft client, ClientLevel level) {
        if (level == null) {
            return "Unknown";
        }

        if (client.hasSingleplayerServer() && client.getSingleplayerServer() != null) {
            Path worldPath = client.getSingleplayerServer()
                    .getWorldPath(LevelResource.LEVEL_DATA_FILE)
                    .getParent()
                    .getFileName();
            return String.valueOf(worldPath);
        }

        if (client.getCurrentServer() != null) {
            String serverName = client.getCurrentServer().name;
            if (serverName != null && !serverName.isBlank()) {
                return serverName;
            }
            return client.getCurrentServer().ip;
        }

        return "Unknown";
    }

    public static String getCurrentWorldKey() {
        Minecraft client = Minecraft.getInstance();
        return getCurrentWorldKey(client, client.level);
    }

    public static String getCurrentWorldKey(ClientLevel level) {
        return getCurrentWorldKey(Minecraft.getInstance(), level);
    }

    /**
     * 获取当前世界的唯一标识符
     */
    public static String getCurrentWorldKey(Minecraft client, ClientLevel level) {
        if (level == null) {
            return "unknown";
        }

        // 组合维度和服务器信息作为唯一标识
        String dimension = level.dimension().registry().toString();
        return getCurrentSessionKey(client, level) + "_" + dimension;
    }
}
