package com.euphony.better_client.config.screen.category;

import static com.euphony.better_client.BetterClient.config;
import static com.euphony.better_client.config.Config.DEFAULTS;
import static com.euphony.better_client.config.YACLConfig.CLIENT_CATEGORY;

import com.euphony.better_client.config.Config;
import com.euphony.better_client.utils.ConfigUtils;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MiscellaneitiesConfigScreen {
    private static final String BUNDLE_UP_GROUP = "bundle_up";

    public static Screen generateScreen(Screen parent) {
        // Bundle Up
        Option<Boolean> enableBundleUpOpt = ConfigUtils.buildBooleanOption(
                "enableBundleUp",
                DEFAULTS.enableBundleUp,
                () -> config.enableBundleUp,
                newVal -> config.enableBundleUp = newVal);

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.better_client:config"))
                .category(ConfigCategory.createBuilder()
                        .name(ConfigUtils.getCategoryName(CLIENT_CATEGORY))
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BUNDLE_UP_GROUP))
                                .options(List.of(enableBundleUpOpt))
                                .build())
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}
