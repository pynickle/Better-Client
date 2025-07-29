package com.euphony.better_client.config.screen.category;

import com.euphony.better_client.config.Config;
import com.euphony.better_client.utils.ConfigUtils;
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

public class TooltipConfigScreen{
    private static final String DURABILITY_TOOLTIP_GROUP = "durability_tooltip";

    public static Screen generateScreen(Screen parent) {
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

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.better_client:config"))
                .category(ConfigCategory.createBuilder()
                        .name(ConfigUtils.getCategoryName(CLIENT_CATEGORY))
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, DURABILITY_TOOLTIP_GROUP))
                                .options(List.of(
                                        enableDurabilityTooltipOpt,
                                        showDurabilityWhenNotDamagedOpt,
                                        showDurabilityHintOpt
                                ))
                                .build())
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}
