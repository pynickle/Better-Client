package com.euphony.better_client.config.screen.category;

import com.euphony.better_client.config.Config;
import com.euphony.better_client.utils.ConfigUtils;
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

public class VisualityConfigScreen {
    private static final String FADING_NIGHT_VISION_GROUP = "fading_night_vision";
    private static final String GLOWING_ENDER_EYE_GROUP = "glowing_ender_eye";
    private static final String FULL_BRIGHTNESS_TOGGLE_GROUP = "full_brightness_toggle";

    public static Screen generateScreen(Screen parent) {
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

        Option<Boolean> enableGlowingEnderEyeOpt = ConfigUtils.buildBooleanOption(
                "enableGlowingEnderEye",
                DEFAULTS.enableGlowingEnderEye,
                () -> config.enableGlowingEnderEye,
                newVal -> config.enableGlowingEnderEye = newVal
        );

        // Full Brightness Toggle
        Option<Boolean> enableFullBrightnessToggleOpt = ConfigUtils.buildBooleanOption(
                "enableFullBrightnessToggle",
                DEFAULTS.enableFullBrightnessToggle,
                () -> config.enableFullBrightnessToggle,
                newVal -> config.enableFullBrightnessToggle = newVal
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
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, GLOWING_ENDER_EYE_GROUP))
                                .options(List.of(
                                        enableGlowingEnderEyeOpt
                                ))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, FULL_BRIGHTNESS_TOGGLE_GROUP))
                                .options(List.of(
                                        enableFullBrightnessToggleOpt
                                ))
                                .build())
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}
