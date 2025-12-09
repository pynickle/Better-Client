package com.euphony.better_client.config.screen.category;

import com.euphony.better_client.config.Config;
import com.euphony.better_client.config.ConfigUtils;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.euphony.better_client.BetterClient.config;
import static com.euphony.better_client.config.Config.DEFAULTS;
import static com.euphony.better_client.config.YACLConfig.CLIENT_CATEGORY;

public class OptimizationConfigScreen {
    private static final String AXOLOTL_BUCKET_FIX_GROUP = "axolotl_bucket_fix";
    private static final String WORLD_ICON_UPDATE_GROUP = "world_icon_update";
    private static final String MUSIC_PAUSE_GROUP = "music_pause";

    public static Screen generateScreen(Screen parent) {
        Option<Boolean> enableAxolotlBucketFixOpt = ConfigUtils.buildBooleanOption(
                "enableAxolotlBucketFix",
                DEFAULTS.enableAxolotlBucketFix,
                () -> config.enableAxolotlBucketFix,
                newVal -> config.enableAxolotlBucketFix = newVal);

        Option<Boolean> enableWorldIconUpdateOpt = ConfigUtils.buildBooleanOption(
                "enableWorldIconUpdate",
                DEFAULTS.enableWorldIconUpdate,
                () -> config.enableWorldIconUpdate,
                newVal -> config.enableWorldIconUpdate = newVal);

        // Music Pause
        Option<Boolean> enableMusicPauseOpt = ConfigUtils.buildBooleanOption(
                "enableMusicPause",
                DEFAULTS.enableMusicPause,
                () -> config.enableMusicPause,
                newVal -> config.enableMusicPause = newVal);

        Option<Boolean> pauseUiSoundOpt = ConfigUtils.buildBooleanOption(
                "pauseUiSound",
                DEFAULTS.pauseUiSound,
                () -> config.pauseUiSound,
                newVal -> config.pauseUiSound = newVal);

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.better_client:config"))
                .category(ConfigCategory.createBuilder()
                        .name(ConfigUtils.getCategoryName(CLIENT_CATEGORY))
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, MUSIC_PAUSE_GROUP))
                                .options(List.of(enableMusicPauseOpt, pauseUiSoundOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, WORLD_ICON_UPDATE_GROUP))
                                .options(List.of(enableWorldIconUpdateOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, AXOLOTL_BUCKET_FIX_GROUP))
                                .options(List.of(enableAxolotlBucketFixOpt))
                                .build())
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}
