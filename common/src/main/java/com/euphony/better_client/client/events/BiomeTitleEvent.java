package com.euphony.better_client.client.events;

import com.euphony.better_client.utils.JsonUtils;
import com.euphony.better_client.utils.mc.BiomeUtils;
import com.euphony.better_client.utils.mc.DataUtils;
import com.euphony.better_client.utils.mc.LevelUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import org.joml.Matrix3x2fStack;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.euphony.better_client.BetterClient.LOGGER;
import static com.euphony.better_client.BetterClient.config;

public class BiomeTitleEvent {
    // Constants
    private static final int MAX_ALPHA = 255;
    private static final int TICKS_PER_SECOND = 20;

    // State Variables
    private static Biome previousBiome;
    private static ResourceKey<Biome> displayBiome;
    private static int displayTime = 0;
    private static int alpha = 0;
    private static int cooldownTime = 0;
    private static int fadeTimer = 0;
    private static boolean complete = false;
    private static boolean fadingIn = false;

    private static final Set<ResourceKey<Biome>> VISITED_BIOMES = Collections.synchronizedSet(new HashSet<>());

    // File Paths
    private static final Path BASE_PATH = DataUtils.getDataDir();
    private static final Path BIOME_VISITS_PATH = BASE_PATH.resolve("biome_visits.json");

    private BiomeTitleEvent() {}

    /**
     * Saves visited biome data to a JSON file asynchronously.
     * Moved to a background thread to prevent game freeze/stutter during rendering.
     */
    private static void saveBiomeVisits() {
        if (!config.enableFirstEntryOnly) return;

        // Run I/O in a separate thread
        CompletableFuture.runAsync(() -> {
            try {
                if (Files.notExists(BASE_PATH)) {
                    Files.createDirectories(BASE_PATH);
                }

                JsonObject root;
                if (Files.exists(BIOME_VISITS_PATH)) {
                    try (Reader reader = Files.newBufferedReader(BIOME_VISITS_PATH, StandardCharsets.UTF_8)) {
                        root = JsonUtils.GSON.fromJson(reader, JsonObject.class);
                    }
                    if (root == null) root = new JsonObject();
                } else {
                    root = new JsonObject();
                }

                String worldId = LevelUtils.getCurrentWorldKey();

                JsonArray biomeArray = new JsonArray();
                synchronized (VISITED_BIOMES) {
                    for (ResourceKey<Biome> biome : VISITED_BIOMES) {
                        biomeArray.add(biome.identifier().toString());
                    }
                }
                root.add(worldId, biomeArray);

                Files.writeString(BIOME_VISITS_PATH, JsonUtils.GSON.toJson(root), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.error("Couldn't save biome visits: ", e);
            }
        });
    }

    /**
     * Loads visited biome data from disk.
     */
    private static void loadBiomeVisits(ClientLevel level) {
        if (!config.enableFirstEntryOnly) return;

        // Corrected logic: Return if file does NOT exist
        if (Files.notExists(BIOME_VISITS_PATH)) return;

        try (Reader reader = Files.newBufferedReader(BIOME_VISITS_PATH, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            VISITED_BIOMES.clear();

            String worldId = LevelUtils.getCurrentWorldKey(level);

            if (root.has(worldId)) {
                JsonArray biomes = root.getAsJsonArray(worldId);
                for (JsonElement elem : biomes) {
                    Identifier biomeLoc = Identifier.tryParse(elem.getAsString());
                    if (biomeLoc != null) {
                        ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, biomeLoc);
                        VISITED_BIOMES.add(biomeKey);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Couldn't load biome visits: ", e);
        }
    }

    /**
     * Tick update logic.
     */
    public static void clientPre(Minecraft minecraft) {
        if (!complete || minecraft.isPaused()) return;

        if (fadingIn) {
            handleFadeIn();
        } else {
            handleDisplay();
        }
    }

    /**
     * Main rendering method.
     */
    public static void renderBiomeInfo(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!shouldRender()) return;

        Minecraft mc = Minecraft.getInstance();
        Entity player = mc.getCameraEntity();
        if (player == null) return;

        BlockPos pos = player.getOnPos();
        if (mc.level == null || !mc.level.isLoaded(pos)) return;

        Holder<Biome> biomeHolder = mc.level.getBiome(pos);
        if (!biomeHolder.isBound()) return;

        Biome currentBiome = biomeHolder.value();

        if (shouldUpdateBiome(currentBiome, pos, mc)) {
            updateBiomeDisplay(biomeHolder);
        }

        if (alpha > 0) {
            renderBiomeTitle(guiGraphics, mc);
        }
    }

    /**
     * Reset state on level load.
     */
    public static void clientLevelLoad(ClientLevel level) {
        complete = true;
        // Clear runtime cache for fresh start if needed, or just load persistence
        loadBiomeVisits(level);
    }

    // ================= Private Helper Methods =================

    private static void handleFadeIn() {
        if (fadeTimer < config.fadeInTime) {
            fadeTimer++;
            alpha = calculateAlpha(fadeTimer, config.fadeInTime);
        } else {
            completeFadeIn();
        }
    }

    private static void handleDisplay() {
        if (displayTime > 0) {
            displayTime--;
        } else if (fadeTimer > 0) {
            fadeTimer--;
            alpha = calculateAlpha(fadeTimer, config.fadeOutTime);
        } else if (cooldownTime > 0) {
            cooldownTime--;
        }
    }

    private static void completeFadeIn() {
        fadeTimer = config.fadeOutTime;
        fadingIn = false;
        displayTime = (int) (config.displayDuration * TICKS_PER_SECOND);
        alpha = MAX_ALPHA;
    }

    private static int calculateAlpha(int timer, int maxTime) {
        if (maxTime == 0) return MAX_ALPHA;
        return (int) ((float) MAX_ALPHA / maxTime * timer);
    }

    private static boolean shouldRender() {
        return complete && config.enableBiomeTitle;
    }

    private static boolean shouldUpdateBiome(Biome currentBiome, BlockPos pos, Minecraft mc) {
        if (previousBiome == currentBiome || cooldownTime > 0 || mc.level == null) return false;

        boolean isUnderground = mc.level.dimensionType().hasSkyLight() && !mc.level.canSeeSky(pos);
        if (!(config.enableUndergroundUpdate || !isUnderground)) return false;

        // Check if we should only display on first entry
        if (config.enableFirstEntryOnly) {
            Holder<Biome> biomeHolder = mc.level.getBiome(pos);
            return biomeHolder
                    .unwrapKey()
                    .map(key -> {
                        synchronized (VISITED_BIOMES) {
                            if (VISITED_BIOMES.contains(key)) {
                                return false;
                            }
                            VISITED_BIOMES.add(key);
                        }
                        // Save asynchronously
                        saveBiomeVisits();
                        return true;
                    })
                    .orElse(false);
        }

        return true;
    }

    private static void updateBiomeDisplay(Holder<Biome> biomeHolder) {
        previousBiome = biomeHolder.value();
        biomeHolder.unwrapKey().ifPresent(BiomeTitleEvent::initializeBiomeDisplay);
    }

    private static void initializeBiomeDisplay(ResourceKey<Biome> key) {
        cooldownTime = (int) (config.cooldownTime * TICKS_PER_SECOND);
        displayBiome = key;
        displayTime = 0;
        alpha = 0;
        fadeTimer = 0;
        fadingIn = true;
    }

    private static void renderBiomeTitle(GuiGraphics guiGraphics, Minecraft mc) {
        if (shouldHideDisplay(mc)) return;

        Font font = mc.font;
        float scale = (float) config.scale;

        // Use PoseStack (standard MC mapping)
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();

        // Center on screen
        pose.translate((float) (guiGraphics.guiWidth() / 2D), (float) (guiGraphics.guiHeight() / 2D));
        pose.scale(scale, scale);

        Component biomeName = getBiomeName(displayBiome);
        int textWidth = font.width(biomeName);
        int y = -font.wordWrapHeight(FormattedText.of(biomeName.getString()), 999) / 2 + config.biomeTitleYOffset;

        // Render text centered
        guiGraphics.drawString(font, biomeName, (-textWidth / 2), y, config.biomeTitleColor | (alpha << 24), true);

        pose.popMatrix();
    }

    private static boolean shouldHideDisplay(Minecraft mc) {
        return (mc.options.hideGui && config.hideInF1) || (mc.getDebugOverlay().showDebugScreen() && config.hideInF3);
    }

    private static Component getBiomeName(ResourceKey<Biome> key) {
        return BiomeUtils.createBiomeDisplayComponent(key, config.enableModName);
    }
}
