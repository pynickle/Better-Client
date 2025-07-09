package com.euphony.better_client.config;

import com.euphony.better_client.utils.ConfigUtils;
import com.euphony.better_client.utils.enums.DescComponent;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.List;

import static com.euphony.better_client.BetterClient.config;

public class YACLConfig extends Config {
    public YACLConfig() {
        super();
    }

    private static final String CLIENT_CATEGORY = "client";
    private static final String FADING_NIGHT_VISION_GROUP = "fading_night_vision";
    private static final String BETTER_PING_DISPLAY_GROUP = "better_ping_display";
    private static final String BETTER_CHAT_GROUP = "better_chat";
    private static final String BIOME_TITLE_GROUP = "biome_title";
    private static final String FASTER_CLIMBING_GROUP = "faster_climbing";
    private static final String BOOK_SCROLL_GROUP = "book_scroll";
    private static final String MUSIC_PAUSE_GROUP = "music_pause";
    private static final String FAST_TRADING_GROUP = "fast_trading";
    private static final String NO_EXPERIMENTAL_WARNING_GROUP = "no_experimental_warning";
    private static final String BUNDLE_UP_GROUP = "bundle_up";
    private static final String DURABILITY_TOOLTIP_GROUP = "durability_tooltip";
    private static final String TRADING_HUD_GROUP = "trading_hud";
    private static final String OTHER_GROUP = "other";

    @Override
    public Screen makeScreen(Screen parent) {
        // Fading Night Vision
        Option<Boolean> enableFadingNightVisionOpt = ConfigUtils.buildBooleanOption(
                "enableFadingNightVision",
                DEFAULTS.enableFadingNightVision,
                () -> config.enableFadingNightVision,
                newVal -> config.enableFadingNightVision = newVal
        );

        Option<Double> fadingOutDurationOpt = ConfigUtils.<Double>getGenericOption("fadingOutDuration")
                .binding(DEFAULTS.fadingOutDuration,
                        () -> config.fadingOutDuration,
                        newVal -> config.fadingOutDuration = newVal)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt)
                        .range(1.0, 5.0).step(0.5).formatValue(value -> Component.literal(value + "s")))
                .build();

        // Better Ping Display
        Option<Boolean> enableBetterPingDisplayOpt = ConfigUtils.buildBooleanOption(
                "enableBetterPingDisplay",
                DEFAULTS.enableBetterPingDisplay,
                () -> config.enableBetterPingDisplay,
                newVal -> config.enableBetterPingDisplay = newVal
        );

        Option<Boolean> enableDefaultPingBarsOpt = ConfigUtils.buildBooleanOption(
                "enableDefaultPingBars",
                DEFAULTS.enableDefaultPingBars,
                () -> config.enableDefaultPingBars,
                newVal -> config.enableDefaultPingBars = newVal
        );

        // Better Chat
        Option<Boolean> enableLongerChatHistoryOpt = ConfigUtils.buildBooleanOption(
                "enableLongerChatHistory",
                DEFAULTS.enableLongerChatHistory,
                () -> config.enableLongerChatHistory,
                newVal -> config.enableLongerChatHistory = newVal
        );

        Option<Integer> chatMaxMessagesOpt = ConfigUtils.<Integer>getGenericOption("chatMaxMessages")
                .binding(DEFAULTS.chatMaxMessages,
                        () -> config.chatMaxMessages,
                        newVal -> config.chatMaxMessages = newVal)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(100, 32768))
                .build();

        Option<Boolean> enableTimeStampOpt = ConfigUtils.buildBooleanOption(
                "enableTimeStamp",
                DEFAULTS.enableTimeStamp,
                () -> config.enableTimeStamp,
                newVal -> config.enableTimeStamp = newVal
        );

        Option<Color> timeStampColorOpt = ConfigUtils.<Color>getGenericOption("timeStampColor")
                .binding(new Color(DEFAULTS.timeStampColor, false),
                        () -> new Color(config.timeStampColor, false),
                        newVal -> config.timeStampColor = newVal.getRGB())
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build();

        // Biome Title
        Option<Boolean> enableBiomeTitleOpt = ConfigUtils.buildBooleanOption(
                "enableBiomeTitle",
                DEFAULTS.enableBiomeTitle,
                () -> config.enableBiomeTitle,
                newVal -> config.enableBiomeTitle = newVal
        );

        Option<Boolean> hideInF1Opt = ConfigUtils.buildBooleanOption(
                "hideInF1",
                DEFAULTS.hideInF1,
                () -> config.hideInF1,
                newVal -> config.hideInF1 = newVal
        );

        Option<Boolean> hideInF3Opt = ConfigUtils.buildBooleanOption(
                "hideInF3",
                DEFAULTS.hideInF3,
                () -> config.hideInF3,
                newVal -> config.hideInF3 = newVal
        );

        Option<Double> displayDurationOpt = ConfigUtils.<Double>getGenericOption("displayDuration")
                .binding(DEFAULTS.displayDuration,
                        () -> config.displayDuration,
                        newVal -> config.displayDuration = newVal)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt)
                        .range(1.0, 5.0).step(0.5).formatValue(value -> Component.literal(value + "s")))
                .build();

        Option<Integer> fadeInTimeOpt = ConfigUtils.<Integer>getGenericOption("fadeInTime", DescComponent.TICK_EXPLANATION)
                .binding(DEFAULTS.fadeInTime,
                        () -> config.fadeInTime,
                        newVal -> config.fadeInTime = newVal)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(0, 60).formatValue(value -> Component.literal(value + " ticks")))
                .build();

        Option<Integer> fadeOutTimeOpt = ConfigUtils.<Integer>getGenericOption("fadeOutTime", DescComponent.TICK_EXPLANATION)
                .binding(DEFAULTS.fadeOutTime,
                        () -> config.fadeOutTime,
                        newVal -> config.fadeOutTime = newVal)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(0, 60).formatValue(value -> Component.literal(value + " ticks")))
                .build();

        Option<Double> scaleOpt = ConfigUtils.<Double>getGenericOption("scale")
                .binding(DEFAULTS.scale,
                        () -> config.scale,
                        newVal -> config.scale = newVal)
                .controller(opt -> DoubleFieldControllerBuilder.create(opt)
                        .range(0.3, 3.0))
                .build();

        Option<Integer> yOffsetOpt = ConfigUtils.<Integer>getGenericOption("yOffset")
                .binding(DEFAULTS.yOffset,
                        () -> config.yOffset,
                        newVal -> config.yOffset = newVal)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(-60, 60))
                .build();

        Option<Color> colorOpt = ConfigUtils.<Color>getGenericOption("color")
                .binding(new Color(DEFAULTS.color, false),
                        () -> new Color(config.color, false),
                        newVal -> config.color = newVal.getRGB())
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build();

        Option<Double>  cooldownTimeOpt = ConfigUtils.<Double>getGenericOption("cooldownTime")
                .binding(DEFAULTS.cooldownTime,
                        () -> config.cooldownTime,
                        newVal -> config.cooldownTime = newVal)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt)
                        .range(0.0, 5.0).step(0.5).formatValue(value -> Component.literal(value + "s")))
                .build();

        Option<Boolean> enableModNameOpt = ConfigUtils.buildBooleanOption(
                "enableModName",
                DEFAULTS.enableModName,
                () -> config.enableModName,
                newVal -> config.enableModName = newVal
        );

        Option<Boolean> enableUndergroundUpdateOpt = ConfigUtils.buildBooleanOption(
                "enableUndergroundUpdate",
                DEFAULTS.enableUndergroundUpdate,
                () -> config.enableUndergroundUpdate,
                newVal -> config.enableUndergroundUpdate = newVal
        );

        // Faster Climbing
        Option<Boolean> enableFasterClimbingOpt = ConfigUtils.buildBooleanOption(
                "enableFasterClimbing",
                DEFAULTS.enableFasterClimbing,
                () -> config.enableFasterClimbing,
                newVal -> config.enableFasterClimbing = newVal
        );

        Option<Boolean> enableFasterUpwardOpt = ConfigUtils.buildBooleanOption(
                "enableFasterUpward",
                DEFAULTS.enableFasterUpward,
                () -> config.enableFasterUpward,
                newVal -> config.enableFasterUpward = newVal
        );

        Option<Boolean> enableFasterDownwardOpt = ConfigUtils.buildBooleanOption(
                "enableFasterDownward",
                DEFAULTS.enableFasterDownward,
                () -> config.enableFasterDownward,
                newVal -> config.enableFasterDownward = newVal
        );

        Option<Double> speedMultiplierOpt = ConfigUtils.<Double>getGenericOption("speedMultiplier")
                .binding(DEFAULTS.speedMultiplier,
                        () -> config.speedMultiplier,
                        newVal -> config.speedMultiplier = newVal)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt)
                        .range(1.0, 10.0).step(0.5))
                .build();

        // Book Scroll
        Option<Boolean> enableBookScrollOpt = ConfigUtils.buildBooleanOption(
                "enableBookScroll",
                DEFAULTS.enableBookScroll,
                () -> config.enableBookScroll,
                newVal -> config.enableBookScroll = newVal
        );

        Option<Integer> ctrlSpeedMultiplierOpt = ConfigUtils.<Integer>getGenericOption("ctrlSpeedMultiplier")
                .binding(DEFAULTS.ctrlSpeedMultiplier,
                        () -> config.ctrlSpeedMultiplier,
                        newVal -> config.ctrlSpeedMultiplier = newVal)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(1, 10).step(1))
                .build();
        Option<Boolean> enablePageTurnSoundOpt = ConfigUtils.buildBooleanOption(
                "enablePageTurnSound",
                DEFAULTS.enablePageTurnSound,
                () -> config.enablePageTurnSound,
                newVal -> config.enablePageTurnSound = newVal
        );

        // Music Pause
        Option<Boolean> enableMusicPauseOpt = ConfigUtils.buildBooleanOption(
                "enableMusicPause",
                DEFAULTS.enableMusicPause,
                () -> config.enableMusicPause,
                newVal -> config.enableMusicPause = newVal
        );

        Option<Boolean> pauseUiSoundOpt = ConfigUtils.buildBooleanOption(
                "pauseUiSound",
                DEFAULTS.pauseUiSound,
                () -> config.pauseUiSound,
                newVal -> config.pauseUiSound = newVal
        );

        // Fast Trading
        Option<Boolean> enableFastTradingOpt = ConfigUtils.buildBooleanOption(
                "enableFastTrading",
                DEFAULTS.enableFastTrading,
                () -> config.enableFastTrading,
                newVal -> config.enableFastTrading = newVal
        );
        Option<Boolean> enableAltKeyOpt = ConfigUtils.buildBooleanOption(
                "enableAltKey",
                DEFAULTS.enableAltKey,
                () -> config.enableAltKey,
                newVal -> config.enableAltKey = newVal
        );

        // No Experimental Warning
        Option<Boolean> enableNoExperimentalWarningOpt = ConfigUtils.buildBooleanOption(
                "enableNoExperimentalWarning",
                DEFAULTS.enableNoExperimentalWarning,
                () -> config.enableNoExperimentalWarning,
                newVal -> config.enableNoExperimentalWarning = newVal
        );
        Option<Boolean> enableExperimentalDisplayOpt = ConfigUtils.buildBooleanOption(
                "enableExperimentalDisplay",
                DEFAULTS.enableExperimentalDisplay,
                () -> config.enableExperimentalDisplay,
                newVal -> config.enableExperimentalDisplay = newVal
        );

        // Bundle Up
        Option<Boolean> enableBundleUpOpt = ConfigUtils.buildBooleanOption(
                "enableBundleUp",
                DEFAULTS.enableBundleUp,
                () -> config.enableBundleUp,
                newVal -> config.enableBundleUp = newVal
        );

        // Durability Tooltip
        Option<Boolean> enableDurabilityTooltipOpt = ConfigUtils.buildBooleanOption(
                "enableDurabilityTooltip",
                DEFAULTS.enableDurabilityTooltip,
                () -> config.enableDurabilityTooltip,
                newVal -> config.enableDurabilityTooltip = newVal
        );
        Option<Boolean> showDurabilityWhenNotDamagedOpt = ConfigUtils.buildBooleanOption(
                "showDurabilityWhenNotDamaged",
                DEFAULTS.showDurabilityWhenNotDamaged,
                () -> config.showDurabilityWhenNotDamaged,
                newVal -> config.showDurabilityWhenNotDamaged = newVal
        );
        Option<Boolean> showDurabilityHintOpt = ConfigUtils.buildBooleanOption(
                "showDurabilityHint",
                DEFAULTS.showDurabilityHint,
                () -> config.showDurabilityHint,
                newVal -> config.showDurabilityHint = newVal
        );

        // Trading Hud
        Option<Boolean> enableTradingHudOpt = ConfigUtils.buildBooleanOption(
                "enableTradingHud",
                DEFAULTS.enableTradingHud,
                () -> config.enableTradingHud,
                newVal -> config.enableTradingHud = newVal
        );
        Option<Boolean> renderRealCostDirectlyOpt = ConfigUtils.buildBooleanOption(
                "renderRealCostDirectly",
                DEFAULTS.renderRealCostDirectly,
                () -> config.renderRealCostDirectly,
                newVal -> config.renderRealCostDirectly = newVal
        );

        // Other
        Option<Boolean> enableAxolotlBucketFixOpt = ConfigUtils.buildBooleanOption(
                "enableAxolotlBucketFix",
                DEFAULTS.enableAxolotlBucketFix,
                () -> config.enableAxolotlBucketFix,
                newVal -> config.enableAxolotlBucketFix = newVal
        );

        Option<Boolean> enableBookSaveConfirmationOpt = ConfigUtils.buildBooleanOption(
                "enableBookSaveConfirmation",
                DEFAULTS.enableBookSaveConfirmation,
                () -> config.enableBookSaveConfirmation,
                newVal -> config.enableBookSaveConfirmation = newVal
        );

        Option<Boolean> enableChatHistoryRetentionOpt = ConfigUtils.buildBooleanOption(
                "enableChatHistoryRetention",
                DEFAULTS.enableChatHistoryRetention,
                () -> config.enableChatHistoryRetention,
                newVal -> config.enableChatHistoryRetention = newVal
        );

        Option<Boolean> enableDisplayRemainingSalesOpt = ConfigUtils.buildBooleanOption(
                "enableDisplayRemainingSales",
                DEFAULTS.enableDisplayRemainingSales,
                () -> config.enableDisplayRemainingSales,
                newVal -> config.enableDisplayRemainingSales = newVal
        );

        Option<Boolean> enableVisibleTradeOpt = ConfigUtils.buildBooleanOption(
                "enableVisibleTrade",
                DEFAULTS.enableVisibleTrade,
                () -> config.enableVisibleTrade,
                newVal -> config.enableVisibleTrade = newVal
        );

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.better_client:config"))
                .category(ConfigCategory.createBuilder()
                        .name(ConfigUtils.getCategoryName(CLIENT_CATEGORY))
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, FADING_NIGHT_VISION_GROUP))
                                .options(List.of(
                                        enableFadingNightVisionOpt,
                                        fadingOutDurationOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BETTER_PING_DISPLAY_GROUP))
                                .options(List.of(
                                        enableBetterPingDisplayOpt,
                                        enableDefaultPingBarsOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BETTER_CHAT_GROUP))
                                .options(List.of(
                                        enableLongerChatHistoryOpt,
                                        chatMaxMessagesOpt,
                                        enableTimeStampOpt,
                                        timeStampColorOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BIOME_TITLE_GROUP))
                                .options(List.of(
                                        enableBiomeTitleOpt,
                                        hideInF1Opt,
                                        hideInF3Opt,
                                        displayDurationOpt,
                                        fadeInTimeOpt,
                                        fadeOutTimeOpt,
                                        scaleOpt,
                                        yOffsetOpt,
                                        colorOpt,
                                        cooldownTimeOpt,
                                        enableModNameOpt,
                                        enableUndergroundUpdateOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, FASTER_CLIMBING_GROUP))
                                .options(List.of(
                                        enableFasterClimbingOpt,
                                        enableFasterUpwardOpt,
                                        enableFasterDownwardOpt,
                                        speedMultiplierOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BOOK_SCROLL_GROUP))
                                .options(List.of(
                                        enableBookScrollOpt,
                                        ctrlSpeedMultiplierOpt,
                                        enablePageTurnSoundOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, MUSIC_PAUSE_GROUP))
                                .options(List.of(
                                        enableMusicPauseOpt,
                                        pauseUiSoundOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, FAST_TRADING_GROUP))
                                .options(List.of(
                                        enableFastTradingOpt,
                                        enableAltKeyOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, NO_EXPERIMENTAL_WARNING_GROUP))
                                .options(List.of(
                                        enableNoExperimentalWarningOpt,
                                        enableExperimentalDisplayOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BUNDLE_UP_GROUP))
                                .options(List.of(
                                        enableBundleUpOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, DURABILITY_TOOLTIP_GROUP))
                                .options(List.of(
                                        enableDurabilityTooltipOpt,
                                        showDurabilityWhenNotDamagedOpt,
                                        showDurabilityHintOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, TRADING_HUD_GROUP))
                                .options(List.of(
                                        enableTradingHudOpt,
                                        renderRealCostDirectlyOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, OTHER_GROUP))
                                .options(List.of(
                                        enableAxolotlBucketFixOpt,
                                        enableChatHistoryRetentionOpt,
                                        enableBookSaveConfirmationOpt,
                                        enableDisplayRemainingSalesOpt,
                                        enableVisibleTradeOpt
                                ))
                                .build())
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}

