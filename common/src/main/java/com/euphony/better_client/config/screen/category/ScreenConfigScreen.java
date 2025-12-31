package com.euphony.better_client.config.screen.category;

import com.euphony.better_client.config.Config;
import com.euphony.better_client.config.ConfigUtils;
import com.euphony.better_client.config.option.TotemBarRenderMode;
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
import static com.euphony.better_client.config.Config.DEFAULTS;
import static com.euphony.better_client.config.YACLConfig.CLIENT_CATEGORY;

public class ScreenConfigScreen {
    private static final String BETTER_PING_DISPLAY_GROUP = "better_ping_display";
    private static final String BIOME_TITLE_GROUP = "biome_title";
    private static final String BOOK_SCROLL_GROUP = "book_scroll";
    private static final String NO_EXPERIMENTAL_WARNING_GROUP = "no_experimental_warning";
    private static final String BOOK_SAVE_CONFIRMATION_GROUP = "book_save_confirmation";
    private static final String WORLD_PLAY_TIME_GROUP = "world_play_time";
    private static final String LOWER_SHIELD_GROUP = "lower_shield";
    private static final String TOTEM_BAR_GROUP = "totem_bar";

    public static Screen generateScreen(Screen parent) {
        // Better Ping Display
        Option<Boolean> enableBetterPingDisplayOpt = ConfigUtils.buildBooleanOption(
                "enableBetterPingDisplay",
                "screen/better_ping_display",
                DEFAULTS.enableBetterPingDisplay,
                () -> config.enableBetterPingDisplay,
                newVal -> config.enableBetterPingDisplay = newVal);

        Option<Boolean> enableDefaultPingBarsOpt = ConfigUtils.buildBooleanOption(
                "enableDefaultPingBars",
                DEFAULTS.enableDefaultPingBars,
                () -> config.enableDefaultPingBars,
                newVal -> config.enableDefaultPingBars = newVal);

        // Biome Title
        Option<Boolean> enableBiomeTitleOpt = ConfigUtils.buildBooleanOption(
                "enableBiomeTitle",
                DEFAULTS.enableBiomeTitle,
                () -> config.enableBiomeTitle,
                newVal -> config.enableBiomeTitle = newVal);

        Option<Boolean> hideInF1Opt = ConfigUtils.buildBooleanOption(
                "hideInF1", DEFAULTS.hideInF1, () -> config.hideInF1, newVal -> config.hideInF1 = newVal);

        Option<Boolean> hideInF3Opt = ConfigUtils.buildBooleanOption(
                "hideInF3", DEFAULTS.hideInF3, () -> config.hideInF3, newVal -> config.hideInF3 = newVal);

        Option<Double> displayDurationOpt = ConfigUtils.<Double>getGenericOption("displayDuration")
                .binding(
                        DEFAULTS.displayDuration,
                        () -> config.displayDuration,
                        newVal -> config.displayDuration = newVal)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt)
                        .range(1.0, 5.0)
                        .step(0.5)
                        .formatValue(value -> Component.literal(value + "s")))
                .build();

        Option<Integer> fadeInTimeOpt = ConfigUtils.<Integer>getGenericOption(
                        "fadeInTime", DescComponent.TICK_EXPLANATION)
                .binding(DEFAULTS.fadeInTime, () -> config.fadeInTime, newVal -> config.fadeInTime = newVal)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(0, 60)
                        .formatValue(value -> Component.literal(value + " ticks")))
                .build();

        Option<Integer> fadeOutTimeOpt = ConfigUtils.<Integer>getGenericOption(
                        "fadeOutTime", DescComponent.TICK_EXPLANATION)
                .binding(DEFAULTS.fadeOutTime, () -> config.fadeOutTime, newVal -> config.fadeOutTime = newVal)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(0, 60)
                        .formatValue(value -> Component.literal(value + " ticks")))
                .build();

        Option<Double> scaleOpt = ConfigUtils.<Double>getGenericOption("scale")
                .binding(DEFAULTS.scale, () -> config.scale, newVal -> config.scale = newVal)
                .controller(opt -> DoubleFieldControllerBuilder.create(opt).range(0.3, 3.0))
                .build();

        Option<Integer> biomeTitleYOffset = ConfigUtils.<Integer>getGenericOption("biomeTitleYOffset")
                .binding(
                        DEFAULTS.biomeTitleYOffset,
                        () -> config.biomeTitleYOffset,
                        newVal -> config.biomeTitleYOffset = newVal)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(-60, 60))
                .build();

        Option<Color> biomeTitleColorOpt = ConfigUtils.<Color>getGenericOption("biomeTitleColor")
                .binding(
                        new Color(DEFAULTS.biomeTitleColor, false),
                        () -> new Color(config.biomeTitleColor, false),
                        newVal -> config.biomeTitleColor = newVal.getRGB())
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build();

        Option<Double> cooldownTimeOpt = ConfigUtils.<Double>getGenericOption("cooldownTime")
                .binding(DEFAULTS.cooldownTime, () -> config.cooldownTime, newVal -> config.cooldownTime = newVal)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt)
                        .range(0.0, 5.0)
                        .step(0.5)
                        .formatValue(value -> Component.literal(value + "s")))
                .build();

        Option<Boolean> enableModNameOpt = ConfigUtils.buildBooleanOption(
                "enableModName",
                DEFAULTS.enableModName,
                () -> config.enableModName,
                newVal -> config.enableModName = newVal);

        Option<Boolean> enableUndergroundUpdateOpt = ConfigUtils.buildBooleanOption(
                "enableUndergroundUpdate",
                DEFAULTS.enableUndergroundUpdate,
                () -> config.enableUndergroundUpdate,
                newVal -> config.enableUndergroundUpdate = newVal);

        // Book Scroll
        Option<Boolean> enableBookScrollOpt = ConfigUtils.buildBooleanOption(
                "enableBookScroll",
                DEFAULTS.enableBookScroll,
                () -> config.enableBookScroll,
                newVal -> config.enableBookScroll = newVal);

        Option<Integer> ctrlSpeedMultiplierOpt = ConfigUtils.<Integer>getGenericOption("ctrlSpeedMultiplier")
                .binding(
                        DEFAULTS.ctrlSpeedMultiplier,
                        () -> config.ctrlSpeedMultiplier,
                        newVal -> config.ctrlSpeedMultiplier = newVal)
                .controller(opt ->
                        IntegerSliderControllerBuilder.create(opt).range(1, 10).step(1))
                .build();
        Option<Boolean> enablePageTurnSoundOpt = ConfigUtils.buildBooleanOption(
                "enablePageTurnSound",
                DEFAULTS.enablePageTurnSound,
                () -> config.enablePageTurnSound,
                newVal -> config.enablePageTurnSound = newVal);

        Option<Boolean> enableBookSaveConfirmationOpt = ConfigUtils.buildBooleanOption(
                "enableBookSaveConfirmation",
                DEFAULTS.enableBookSaveConfirmation,
                () -> config.enableBookSaveConfirmation,
                newVal -> config.enableBookSaveConfirmation = newVal);

        // No Experimental Warning
        Option<Boolean> enableNoExperimentalWarningOpt = ConfigUtils.buildBooleanOption(
                "enableNoExperimentalWarning",
                DEFAULTS.enableNoExperimentalWarning,
                () -> config.enableNoExperimentalWarning,
                newVal -> config.enableNoExperimentalWarning = newVal);
        Option<Boolean> enableExperimentalDisplayOpt = ConfigUtils.buildBooleanOption(
                "enableExperimentalDisplay",
                DEFAULTS.enableExperimentalDisplay,
                () -> config.enableExperimentalDisplay,
                newVal -> config.enableExperimentalDisplay = newVal);

        // World Play Time
        Option<Boolean> enableWorldPlayTimeOpt = ConfigUtils.buildBooleanOption(
                "enableWorldPlayTime",
                DEFAULTS.enableWorldPlayTime,
                () -> config.enableWorldPlayTime,
                newVal -> config.enableWorldPlayTime = newVal);
        Option<Color> worldPlayTimeColorOpt = ConfigUtils.<Color>getGenericOption("worldPlayTimeColor")
                .binding(
                        new Color(DEFAULTS.worldPlayTimeColor, false),
                        () -> new Color(config.worldPlayTimeColor, false),
                        newVal -> config.worldPlayTimeColor = newVal.getRGB())
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build();

        // Lower Shield
        Option<Boolean> enableLowerShieldOpt = ConfigUtils.buildBooleanOption(
                "enableLowerShield",
                DEFAULTS.enableLowerShield,
                () -> config.enableLowerShield,
                newVal -> config.enableLowerShield = newVal);
        Option<Double> shieldOffsetOpt = ConfigUtils.<Double>getGenericOption("shieldOffset")
                .binding(DEFAULTS.shieldOffset, () -> config.shieldOffset, newVal -> config.shieldOffset = newVal)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt)
                        .range(-5.0, 5.0)
                        .step(0.5))
                .build();

        // Totem Bar
        Option<Boolean> enableTotemBarOpt = ConfigUtils.buildBooleanOption(
                "enableTotemBar",
                DEFAULTS.enableTotemBar,
                () -> config.enableTotemBar,
                newVal -> config.enableTotemBar = newVal);
        Option<TotemBarRenderMode> totemBarRenderModeOpt = ConfigUtils.<TotemBarRenderMode>getGenericOption("totemBarRenderMode")
                .binding(DEFAULTS.totemBarRenderMode, () -> config.totemBarRenderMode, newVal -> config.totemBarRenderMode = newVal)
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(TotemBarRenderMode.class)
                        .formatValue(ConfigUtils.TOTEM_BAR_RENDER_MODE_VALUE_FORMATTER))
                .build();
        Option<Integer> totemBarYOffsetOpt = ConfigUtils.<Integer>getGenericOption("totemBarYOffset")
                .binding(DEFAULTS.totemBarYOffset, () -> config.totemBarYOffset, newVal -> config.totemBarYOffset = newVal)
                .controller(IntegerFieldControllerBuilder::create)
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.better_client:config"))
                .category(ConfigCategory.createBuilder()
                        .name(ConfigUtils.getCategoryName(CLIENT_CATEGORY))
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BETTER_PING_DISPLAY_GROUP))
                                .options(java.util.List.of(enableBetterPingDisplayOpt, enableDefaultPingBarsOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BIOME_TITLE_GROUP))
                                .options(java.util.List.of(
                                        enableBiomeTitleOpt,
                                        hideInF1Opt,
                                        hideInF3Opt,
                                        displayDurationOpt,
                                        fadeInTimeOpt,
                                        fadeOutTimeOpt,
                                        scaleOpt,
                                        biomeTitleYOffset,
                                        biomeTitleColorOpt,
                                        cooldownTimeOpt,
                                        enableModNameOpt,
                                        enableUndergroundUpdateOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BOOK_SCROLL_GROUP))
                                .options(java.util.List.of(
                                        enableBookScrollOpt, ctrlSpeedMultiplierOpt, enablePageTurnSoundOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, NO_EXPERIMENTAL_WARNING_GROUP))
                                .options(
                                        java.util.List.of(enableNoExperimentalWarningOpt, enableExperimentalDisplayOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BOOK_SAVE_CONFIRMATION_GROUP))
                                .options(List.of(enableBookSaveConfirmationOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, WORLD_PLAY_TIME_GROUP))
                                .options(List.of(enableWorldPlayTimeOpt, worldPlayTimeColorOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, LOWER_SHIELD_GROUP))
                                .options(List.of(enableLowerShieldOpt, shieldOffsetOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, TOTEM_BAR_GROUP))
                                .options(List.of(enableTotemBarOpt, totemBarRenderModeOpt, totemBarYOffsetOpt))
                                .build())
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}
