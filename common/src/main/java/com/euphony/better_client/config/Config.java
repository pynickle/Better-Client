package com.euphony.better_client.config;


import com.euphony.better_client.BetterClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static com.euphony.better_client.BetterClient.config;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = Platform.getConfigFolder().resolve(BetterClient.MOD_ID).resolve("client.json");

    public static Config DEFAULTS = new Config();

    public boolean enableFadingNightVision = true;
    public double fadingOutDuration = 3.0D;

    public boolean enableBetterPingDisplay = true;
    public boolean enableDefaultPingBars = false;

    public boolean enableLongerChatHistory = true;
    public int chatMaxMessages = 4096;
    public boolean enableTimeStamp = true;
    public int timeStampColor = 0xEE46FF;

    public boolean enableFasterClimbing = false;
    public boolean enableFasterUpward = true;
    public boolean enableFasterDownward = true;
    public double speedMultiplier = 2.0D;

    public boolean enableBiomeTitle = true;
    public boolean hideInF3 = true;
    public boolean hideInF1 = true;
    public double displayDuration = 1.5;
    public int fadeInTime = 20;
    public int fadeOutTime = 20;
    public double scale = 2.1D;
    public int yOffset = -10;
    public int color = 0xFFFFFF;
    public double cooldownTime = 1.5D;
    public boolean enableModName = false;
    public boolean enableUndergroundUpdate = false;

    public boolean enableBookScroll = true;
    public int ctrlSpeedMultiplier = 5;
    public boolean enablePageTurnSound = true;

    public boolean enableMusicPause = true;
    public boolean pauseUiSound = false;

    public boolean enableFastTrading = true;
    public boolean enableAltKey = true;

    public boolean enableNoExperimentalWarning = true;
    public boolean enableExperimentalDisplay = true;

    public boolean enableBundleUp = true;

    public boolean enableDurabilityTooltip = true;
    public boolean showDurabilityWhenNotDamaged = true;
    public boolean showDurabilityHint = true;

    public boolean enableAxolotlBucketFix = true;
    public boolean enableChatHistoryRetention = true;
    public boolean enableBookSaveConfirmation = true;
    public boolean enableDisplayRemainingSales = true;
    public boolean enableVisibleTrade = true;

    public Config() {}

    public static Config create() {
        if(!Platform.isModLoaded("yet_another_config_lib_v3")) return DEFAULTS;

        boolean accessibleInGame = false;
        if(Platform.isFabric()) {
            accessibleInGame = Platform.isModLoaded("modmenu") || (Platform.isModLoaded("catalogue") && Platform.isModLoaded("menulogue"));
        } else if(Platform.isNeoForge()) {
            accessibleInGame = true;
        }
        config = accessibleInGame ? new YACLConfig() : DEFAULTS;

        load();

        return config;
    }

    public static void load() {
        if (Files.notExists(PATH)) {
            save();
        } else try {
            config = GSON.fromJson(Files.readString(PATH), Config.class);
        } catch (Exception e) {
            BetterClient.LOGGER.error("Couldn't load config file: ", e);
        }
    }

    public static void save() {
        try {
            Files.write(PATH, Collections.singleton(GSON.toJson(config)));
        } catch (Exception e) {
            BetterClient.LOGGER.error("Couldn't save config file: ", e);
        }
    }

    public Screen makeScreen(Screen parent) {
        Minecraft mc = Minecraft.getInstance();
        String link = "https://modrinth.com/mod/yacl";

        return new ConfirmScreen(
                clicked -> {
                    if(clicked)
                        ConfirmLinkScreen.confirmLinkNow(parent, link);
                    else
                        mc.setScreen(parent);
                },
                Component.translatable("text.better_client.help.missing"),
                Component.translatable("text.better_client.desc.help.missing"),
                Component.translatable("gui.continue"),
                Component.translatable("gui.back")
        );
    }
}
