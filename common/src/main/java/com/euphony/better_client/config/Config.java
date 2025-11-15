package com.euphony.better_client.config;

import static com.euphony.better_client.BetterClient.config;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path BASE_PATH = Platform.getConfigFolder().resolve(BetterClient.MOD_ID);
    private static final Path PATH = BASE_PATH.resolve("client.json");

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
    public int biomeTitleYOffset = -10;
    public int biomeTitleColor = 0xFFFFFF;
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

    public boolean enableTradingHud = true;
    public int tradingHudXOffset = 0;
    public int tradingHudYOffset = 0;
    public boolean renderRealCostDirectly = true;

    public boolean enableAxolotlBucketFix = true;

    public boolean enableChatHistoryRetention = true;

    public boolean enableBookSaveConfirmation = true;

    public boolean enableDisplayRemainingSales = true;

    public boolean enableGlowingEnderEye = true;

    public boolean enableWorldIconUpdate = false;

    public boolean enableFullBrightnessToggle = true;

    public boolean enableCompassTooltip = true;
    public boolean enableNormalCompassTooltip = true;
    public boolean enableLodestoneTooltip = true;
    public boolean enableRecoveryCompassTooltip = true;

    public boolean enableSuspiciousStewTooltip = true;

    public boolean enableChatFormatter = true;
    public String posFormat = "{x}, {y}, {z}";

    public boolean enableWorldPlayTime = true;
    public int worldPlayTimeColor = 0x808080;

    public boolean enableTrialSpawnerTimer = true;
    public boolean timerSeenThroughWalls = false;
    public boolean highSensitivityMode = true;
    public long trialSpawnerCooldown = 1800;
    public boolean enableDynamicTimerColor = true;
    public int timerColor = 0xFFFF88FF;
    public boolean enableDropShadow = true;

    public boolean enableInvisibleItemFrame = true;

    public Config() {}

    public static Config create() {
        if (!Platform.isModLoaded("yet_another_config_lib_v3")) return DEFAULTS;

        boolean accessibleInGame = false;
        if (Platform.isFabric()) {
            accessibleInGame = Platform.isModLoaded("modmenu")
                    || (Platform.isModLoaded("catalogue") && Platform.isModLoaded("menulogue"));
        } else if (Platform.isNeoForge()) {
            accessibleInGame = true;
        }
        config = accessibleInGame ? new YACLConfig() : DEFAULTS;

        load();

        return config;
    }

    public static void load() {
        if (Files.notExists(BASE_PATH)) {
            try {
                Files.createDirectories(BASE_PATH);
            } catch (Exception e) {
                BetterClient.LOGGER.error("Couldn't create config directory: ", e);
                return;
            }
        }
        if (Files.notExists(PATH)) {
            if (Utils.isAnyModLoaded("durabilitytooltip", "rmes-durability-tooltips")) {
                config.enableDurabilityTooltip = false;
            }
            if (Utils.isAnyModLoaded("hideexperimentalwarning")) {
                config.enableNoExperimentalWarning = false;
            }
            save();
        } else
            try {
                config = GSON.fromJson(Files.readString(PATH), config.getClass());
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
                    if (clicked) ConfirmLinkScreen.confirmLinkNow(parent, link);
                    else mc.setScreen(parent);
                },
                Component.translatable("text.better_client.help.missing"),
                Component.translatable("text.better_client.desc.help.missing"),
                Component.translatable("gui.continue"),
                Component.translatable("gui.back"));
    }
}
