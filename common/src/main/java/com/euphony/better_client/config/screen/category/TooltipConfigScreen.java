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

public class TooltipConfigScreen {
    private static final String DURABILITY_TOOLTIP_GROUP = "durability_tooltip";
    private static final String COMPASS_TOOLTIP_GROUP = "compass_tooltip";
    private static final String SUSPICIOUS_STEW_TOOLTIP_GROUP = "suspicious_stew_tooltip";

    public static Screen generateScreen(Screen parent) {
        // Durability Tooltip
        Option<Boolean> enableDurabilityTooltipOpt = ConfigUtils.buildBooleanOption(
                "enableDurabilityTooltip",
                DEFAULTS.enableDurabilityTooltip,
                () -> config.enableDurabilityTooltip,
                newVal -> config.enableDurabilityTooltip = newVal);
        Option<Boolean> showDurabilityWhenNotDamagedOpt = ConfigUtils.buildBooleanOption(
                "showDurabilityWhenNotDamaged",
                DEFAULTS.showDurabilityWhenNotDamaged,
                () -> config.showDurabilityWhenNotDamaged,
                newVal -> config.showDurabilityWhenNotDamaged = newVal);
        Option<Boolean> showDurabilityHintOpt = ConfigUtils.buildBooleanOption(
                "showDurabilityHint",
                DEFAULTS.showDurabilityHint,
                () -> config.showDurabilityHint,
                newVal -> config.showDurabilityHint = newVal);

        // Compass Tooltip
        Option<Boolean> enableCompassTooltipOpt = ConfigUtils.buildBooleanOption(
                "enableCompassTooltip",
                DEFAULTS.enableCompassTooltip,
                () -> config.enableCompassTooltip,
                newVal -> config.enableCompassTooltip = newVal);

        Option<Boolean> enableNormalCompassTooltipOpt = ConfigUtils.buildBooleanOption(
                "enableNormalCompassTooltip",
                DEFAULTS.enableNormalCompassTooltip,
                () -> config.enableNormalCompassTooltip,
                newVal -> config.enableNormalCompassTooltip = newVal);

        Option<Boolean> enableLodestoneCompassTooltipOpt = ConfigUtils.buildBooleanOption(
                "enableLodestoneCompassTooltip",
                DEFAULTS.enableLodestoneTooltip,
                () -> config.enableLodestoneTooltip,
                newVal -> config.enableLodestoneTooltip = newVal);

        Option<Boolean> enableRecoveryCompassTooltipOpt = ConfigUtils.buildBooleanOption(
                "enableRecoveryCompassTooltip",
                DEFAULTS.enableRecoveryCompassTooltip,
                () -> config.enableRecoveryCompassTooltip,
                newVal -> config.enableRecoveryCompassTooltip = newVal);

        // Suspicious Stew Tooltip
        Option<Boolean> enableSuspiciousStewTooltipOpt = ConfigUtils.buildBooleanOption(
                "enableSuspiciousStewTooltip",
                DEFAULTS.enableSuspiciousStewTooltip,
                () -> config.enableSuspiciousStewTooltip,
                newVal -> config.enableSuspiciousStewTooltip = newVal);

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.better_client:config"))
                .category(ConfigCategory.createBuilder()
                        .name(ConfigUtils.getCategoryName(CLIENT_CATEGORY))
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, DURABILITY_TOOLTIP_GROUP))
                                .options(List.of(
                                        enableDurabilityTooltipOpt,
                                        showDurabilityWhenNotDamagedOpt,
                                        showDurabilityHintOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, COMPASS_TOOLTIP_GROUP))
                                .options(List.of(
                                        enableCompassTooltipOpt,
                                        enableNormalCompassTooltipOpt,
                                        enableLodestoneCompassTooltipOpt,
                                        enableRecoveryCompassTooltipOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, SUSPICIOUS_STEW_TOOLTIP_GROUP))
                                .options(List.of(enableSuspiciousStewTooltipOpt))
                                .build())
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}
