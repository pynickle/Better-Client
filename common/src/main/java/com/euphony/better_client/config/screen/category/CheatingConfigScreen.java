package com.euphony.better_client.config.screen.category;

import com.euphony.better_client.config.Config;
import com.euphony.better_client.config.ConfigUtils;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.euphony.better_client.BetterClient.config;
import static com.euphony.better_client.config.Config.DEFAULTS;
import static com.euphony.better_client.config.YACLConfig.CLIENT_CATEGORY;

public class CheatingConfigScreen {
    private static final String FASTER_CLIMBING_GROUP = "faster_climbing";

    public static Screen generateScreen(Screen parent) {
        // Faster Climbing
        Option<Boolean> enableFasterClimbingOpt = ConfigUtils.buildBooleanOption(
                "enableFasterClimbing",
                DEFAULTS.enableFasterClimbing,
                () -> config.enableFasterClimbing,
                newVal -> config.enableFasterClimbing = newVal);

        Option<Boolean> enableFasterUpwardOpt = ConfigUtils.buildBooleanOption(
                "enableFasterUpward",
                DEFAULTS.enableFasterUpward,
                () -> config.enableFasterUpward,
                newVal -> config.enableFasterUpward = newVal);

        Option<Boolean> enableFasterDownwardOpt = ConfigUtils.buildBooleanOption(
                "enableFasterDownward",
                DEFAULTS.enableFasterDownward,
                () -> config.enableFasterDownward,
                newVal -> config.enableFasterDownward = newVal);

        Option<Boolean> enableScaffoldingOpt = ConfigUtils.buildBooleanOption(
                "enableScaffolding",
                DEFAULTS.enableScaffolding,
                () -> config.enableScaffolding,
                newVal -> config.enableScaffolding = newVal);

        Option<Double> speedMultiplierOpt = ConfigUtils.<Double>getGenericOption("speedMultiplier")
                .binding(
                        DEFAULTS.speedMultiplier,
                        () -> config.speedMultiplier,
                        newVal -> config.speedMultiplier = newVal)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt)
                        .range(1.0, 10.0)
                        .step(0.5))
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.better_client:config"))
                .category(ConfigCategory.createBuilder()
                        .name(ConfigUtils.getCategoryName(CLIENT_CATEGORY))
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, FASTER_CLIMBING_GROUP))
                                .options(List.of(
                                        enableFasterClimbingOpt,
                                        enableFasterUpwardOpt,
                                        enableFasterDownwardOpt,
                                        enableScaffoldingOpt,
                                        speedMultiplierOpt))
                                .build())
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}
