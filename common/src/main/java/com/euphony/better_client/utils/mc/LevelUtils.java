package com.euphony.better_client.utils.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public class LevelUtils {
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
}
