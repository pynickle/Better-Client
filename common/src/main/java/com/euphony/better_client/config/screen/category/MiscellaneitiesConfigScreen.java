package com.euphony.better_client.config.screen.category;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.config.Config;
import com.euphony.better_client.config.ConfigUtils;
import com.euphony.better_client.service.BundlePanelSession;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.euphony.better_client.BetterClient.config;
import static com.euphony.better_client.config.Config.DEFAULTS;
import static com.euphony.better_client.config.YACLConfig.CLIENT_CATEGORY;

public class MiscellaneitiesConfigScreen {
    private static final String BUNDLE_UP_GROUP = "bundle_up";
    private static final String CLICK_THROUGH_GROUP = "click_through";

    public static Screen generateScreen(Screen parent) {
        Option<Boolean> enableBundleUpOpt = ConfigUtils.buildBooleanOption(
                "enableBundleUp",
                DEFAULTS.enableBundleUp,
                () -> config.enableBundleUp,
                newVal -> config.enableBundleUp = newVal);

        Option<Boolean> enableBundlePanelOpt = ConfigUtils.buildBooleanOption(
                "enableBundlePanel", DEFAULTS.enableBundlePanel,
                () -> config.enableBundlePanel, value -> config.enableBundlePanel = value);
        Option<Boolean> bundlePanelExpandedOpt = ConfigUtils.buildBooleanOption(
                "bundlePanelExpanded", DEFAULTS.bundlePanelExpanded,
                () -> config.bundlePanelExpanded, value -> config.bundlePanelExpanded = value);
        Option<BundlePanelSession.View> bundlePanelViewOpt = ConfigUtils.<BundlePanelSession.View>getGenericOption("bundlePanelView")
                .binding(DEFAULTS.bundlePanelView, () -> config.bundlePanelView, value -> config.bundlePanelView = value)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(BundlePanelSession.View.class)
                        .formatValue(value -> Component.translatable("text.better_client.bundle_panel.view." + value.name().toLowerCase(java.util.Locale.ROOT))))
                .build();

        Option<Boolean> enableClickThroughOpt = ConfigUtils.buildBooleanOption(
                "enableClickThrough",
                DEFAULTS.enableClickThrough,
                () -> config.enableClickThrough,
                newVal -> config.enableClickThrough = newVal);

        Option<Boolean> clickThroughOnlyContainersOpt = ConfigUtils.buildBooleanOption(
                "clickThroughOnlyContainers",
                DEFAULTS.clickThroughOnlyContainers,
                () -> config.clickThroughOnlyContainers,
                newVal -> config.clickThroughOnlyContainers = newVal);

        Option<Boolean> clickThroughSneakToDyeOpt = ConfigUtils.buildBooleanOption(
                "clickThroughSneakToDye",
                DEFAULTS.clickThroughSneakToDye,
                () -> config.clickThroughSneakToDye,
                newVal -> config.clickThroughSneakToDye = newVal);

        ListOption<String> clickThroughContainersOpt = ListOption.<String>createBuilder()
                .name(Component.translatable(String.format(
                        "yacl3.config.%s:config.clickThroughContainers", BetterClient.MOD_ID)))
                .description(OptionDescription.of(Component.translatable(String.format(
                        "yacl3.config.%s:config.clickThroughContainers.desc", BetterClient.MOD_ID))))
                .binding(
                        DEFAULTS.clickThroughContainers,
                        () -> config.clickThroughContainers,
                        newVal -> config.clickThroughContainers = newVal)
                .controller(StringControllerBuilder::create)
                .initial("minecraft:")
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.better_client:config"))
                .category(ConfigCategory.createBuilder()
                        .name(ConfigUtils.getCategoryName(CLIENT_CATEGORY))
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, "bundle_panel"))
                                .options(List.of(enableBundlePanelOpt, bundlePanelViewOpt, bundlePanelExpandedOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BUNDLE_UP_GROUP))
                                .options(List.of(enableBundleUpOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, CLICK_THROUGH_GROUP))
                                .options(List.of(
                                        enableClickThroughOpt,
                                        clickThroughOnlyContainersOpt,
                                        clickThroughSneakToDyeOpt))
                                .build())
                        .group(clickThroughContainersOpt)
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}
