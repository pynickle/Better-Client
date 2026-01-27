package com.euphony.better_client.utils.mc;

import net.minecraft.client.Minecraft;

import java.nio.file.Path;

public class DataUtils {
    public static Path getDataDir() {
        Minecraft client = Minecraft.getInstance();
        return client.gameDirectory.toPath().resolve("better-client");
    }
}
