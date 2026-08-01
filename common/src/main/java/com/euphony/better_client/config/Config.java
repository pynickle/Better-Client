package com.euphony.better_client.config;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.config.option.AutoAdaptMode;
import com.euphony.better_client.config.option.NewItemMarkerPosition;
import com.euphony.better_client.config.option.PotionBarPos;
import com.euphony.better_client.config.option.TotemBarRenderMode;
import com.euphony.better_client.config.option.TradingHudPos;
import com.euphony.better_client.platform.Platform;
import com.euphony.better_client.utils.Utils;
import com.euphony.better_client.utils.enums.DurabilityTooltipStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.euphony.better_client.BetterClient.LOGGER;
import static com.euphony.better_client.BetterClient.config;

public class Config {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(
                    AutoAdaptMode.class,
                    (JsonDeserializer<AutoAdaptMode>) (json, type, context) -> {
                        // Legacy booleans predate the automatic mode and migrate to its new default.
                        if (json.isJsonNull()
                                || (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean())) {
                            return AutoAdaptMode.AUTO;
                        }
                        return AutoAdaptMode.valueOf(json.getAsString());
                    })
            .setPrettyPrinting()
            .create();
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
    public boolean enableChatMentionAutocomplete = true;
    public int chatMentionColor = 0x55FFFF;

    public boolean enableFasterClimbing = false;
    public boolean enableFasterUpward = true;
    public boolean enableFasterDownward = true;
    public boolean enableScaffolding = true;
    public double speedMultiplier = 2.0D;

    public AutoAdaptMode enableBiomeTitle = AutoAdaptMode.AUTO;
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
    public boolean enableFirstEntryOnly = false;

    public boolean enableBookScroll = true;
    public int ctrlSpeedMultiplier = 5;
    public boolean enablePageTurnSound = true;

    public boolean enableMusicPause = true;
    public boolean pauseUiSound = false;

    public boolean enableFastTrading = true;
    public boolean enableAltKey = true;

    public AutoAdaptMode enableNoExperimentalWarning = AutoAdaptMode.AUTO;
    public boolean enableExperimentalDisplay = true;

    public boolean enableBundleUp = true;

    public boolean enableClickThrough = true;
    public boolean clickThroughOnlyContainers = true;
    public boolean clickThroughSneakToDye = false;
    public List<String> clickThroughContainers = new ArrayList<>(List.of(
            "minecraft:ender_chest",
            "minecraft:vault",
            "minecraft:composter",
            "minecraft:respawn_anchor",
            "minecraft:jukebox",
            "minecraft:decorated_pot",
            "minecraft:chiseled_bookshelf",
            "minecraft:beacon",
            "minecraft:stonecutter",
            "minecraft:grindstone",
            "minecraft:crafting_table"));

    public AutoAdaptMode enableDurabilityTooltip = AutoAdaptMode.AUTO;
    public DurabilityTooltipStyle durabilityTooltipStyle = DurabilityTooltipStyle.NUMBER;
    public boolean showDurabilityWhenNotDamaged = true;
    public boolean showDurabilityHint = true;

    public boolean enableTradingHud = true;
    public boolean enableTradingHudWhileSneaking = true;
    public int tradingHudXOffset = 0;
    public int tradingHudYOffset = 0;
    public TradingHudPos tradingHudPos = TradingHudPos.TOP_RIGHT;
    public boolean renderRealCostDirectly = true;

    public boolean enableAxolotlBucketFix = true;

    public boolean enableChatHistoryRetention = true;
    public boolean enablePersistentChatStorage = true;
    public boolean cleanRestoredChatSeparatorsOnSave = false;
    public String chatHistorySeparatorTemplate =
            """
                    &8&m--------------------------------------------------
                     &6[&eRestored Chat History&6]
                     &7Session: &f{session_name} &8| &7Disconnected: &f{disconnect_time}
                    &8&m--------------------------------------------------""";

    public boolean enableBookSaveConfirmation = true;

    public boolean enableDisplayRemainingSales = true;

    public boolean enableGlowingEnderEye = true;
    public boolean enableDroppedGlowingEnderEye = true;

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

    public boolean enableLowerShield = true;
    public double shieldOffset = -2;

    public boolean enableTotemBar = true;
    public TotemBarRenderMode totemBarRenderMode = TotemBarRenderMode.COMBINED;
    public int totemBarYOffset = 0;

    public boolean enablePotionBar = false;
    public boolean showVanillaEffectHud = false;
    public int potionBarMaxEffects = 3;
    public PotionBarPos potionBarPos = PotionBarPos.CENTER;
    public int potionBarXOffset = 0;
    public int potionBarYOffset = 0;
    public boolean autoAdaptPotionBarYOffset = true;

    public AutoAdaptMode enableFoodBarWhileMounted = AutoAdaptMode.AUTO;
    public AutoAdaptMode enableExperienceBarWhileMounted = AutoAdaptMode.AUTO;

    public boolean enableClientWeather = true;

    public boolean enableNewItemMarker = true;
    public boolean clearNewItemMarkerOnHover = true;
    public boolean clearNewItemMarkerOnSelect = true;
    public boolean clearNewItemMarkerOnInventoryClose = false;
    public boolean showNewItemMarkerOnHotbar = true;
    public NewItemMarkerPosition newItemMarkerPosition = NewItemMarkerPosition.UPPER_LEFT;

    public Config() {
    }

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
                LOGGER.error("Couldn't create config directory: ", e);
                return;
            }
        }
        if (Files.notExists(PATH)) {
            save();
        } else
            try {
                config = GSON.fromJson(Files.readString(PATH), config.getClass());
                config.normalizeAutoAdaptOptions();
            } catch (Exception e) {
                LOGGER.error("Couldn't load config file: ", e);
            }
    }

    private void normalizeAutoAdaptOptions() {
        if (enableDurabilityTooltip == null) enableDurabilityTooltip = AutoAdaptMode.AUTO;
        if (enableNoExperimentalWarning == null) enableNoExperimentalWarning = AutoAdaptMode.AUTO;
        if (enableFoodBarWhileMounted == null) enableFoodBarWhileMounted = AutoAdaptMode.AUTO;
        if (enableExperienceBarWhileMounted == null) enableExperienceBarWhileMounted = AutoAdaptMode.AUTO;
        if (enableBiomeTitle == null) enableBiomeTitle = AutoAdaptMode.AUTO;
    }

    public boolean isDurabilityTooltipEnabled() {
        return enableDurabilityTooltip.resolve(
                !Utils.isAnyModLoaded("durabilitytooltip", "rmes-durability-tooltips", "durabilityviewer"));
    }

    public boolean isNoExperimentalWarningEnabled() {
        return enableNoExperimentalWarning.resolve(
                !Utils.isAnyModLoaded("hideexperimentalwarning", "yeetusexperimentus", "experimentalist"));
    }

    public int getPotionBarYOffset() {
        return autoAdaptPotionBarYOffset && Utils.isAnyModLoaded("jade") ? 23 : potionBarYOffset;
    }

    public boolean isFoodBarWhileMountedEnabled() {
        return enableFoodBarWhileMounted.resolve(!Utils.isAnyModLoaded("bettermounthud", "leavemybarsalone"));
    }

    public boolean isExperienceBarWhileMountedEnabled() {
        return enableExperienceBarWhileMounted.resolve(!Utils.isAnyModLoaded("leavemybarsalone"));
    }

    public boolean isBiomeTitleEnabled() {
        return enableBiomeTitle.resolve(!Utils.isAnyModLoaded("travelerstitles"));
    }

    public static void save() {
        try {
            Files.write(PATH, Collections.singleton(GSON.toJson(config)));
        } catch (Exception e) {
            LOGGER.error("Couldn't save config file: ", e);
        }
    }

    public Screen makeScreen(Screen parent) {
        Minecraft mc = Minecraft.getInstance();
        String link = "https://modrinth.com/mod/yacl";

        return new ConfirmScreen(
                clicked -> {
                    if (clicked) ConfirmLinkScreen.confirmLinkNow(parent, link);
                    else mc.gui.setScreen(parent);
                },
                Component.translatable("text.better_client.help.missing"),
                Component.translatable("text.better_client.desc.help.missing"),
                Component.translatable("gui.continue"),
                Component.translatable("gui.back"));
    }
}
