package com.euphony.better_client.client.events;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Path;

import static com.euphony.better_client.BetterClient.LOGGER;
import static com.euphony.better_client.BetterClient.config;

public class WorldIconUpdateEvent {
    public static void onRenderLevelStage() {
        if (!config.enableWorldIconUpdate) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isLocalServer()) {
            IntegratedServer server = minecraft.getSingleplayerServer();
            if (server != null && !server.isStopped()) {
                server.getWorldScreenshotFile().ifPresent(WorldIconUpdateEvent::captureCleanScreenshot);
            }
        }
    }

    private static void captureCleanScreenshot(Path path) {
        Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget(), (nativeImage) -> Util.ioPool()
                .execute(() -> {
                    int i = nativeImage.getWidth();
                    int j = nativeImage.getHeight();
                    int k = 0;
                    int l = 0;
                    if (i > j) {
                        k = (i - j) / 2;
                        i = j;
                    } else {
                        l = (j - i) / 2;
                        j = i;
                    }

                    try {
                        NativeImage nativeImage2 = new NativeImage(64, 64, false);

                        try {
                            nativeImage.resizeSubRectTo(k, l, i, j, nativeImage2);
                            nativeImage2.writeToFile(path);
                        } catch (Throwable var15) {
                            try {
                                nativeImage2.close();
                            } catch (Throwable var14) {
                                var15.addSuppressed(var14);
                            }

                            throw var15;
                        }

                        nativeImage2.close();
                    } catch (IOException iOException) {
                        LOGGER.warn("Couldn't save auto screenshot", iOException);
                    } finally {
                        nativeImage.close();
                    }
                }));
    }
}
