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
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MerchantConfigScreen {
    private static final String FAST_TRADING_GROUP = "fast_trading";
    private static final String TRADING_HUD_GROUP = "trading_hud";
    private static final String REMAINING_SALES_GROUP = "remaining_sales";

    public static Screen generateScreen(Screen parent) {
        // Fast Trading
        Option<Boolean> enableFastTradingOpt = ConfigUtils.buildBooleanOption(
                "enableFastTrading",
                DEFAULTS.enableFastTrading,
                () -> config.enableFastTrading,
                newVal -> config.enableFastTrading = newVal);
        Option<Boolean> enableAltKeyOpt = ConfigUtils.buildBooleanOption(
                "enableAltKey",
                DEFAULTS.enableAltKey,
                () -> config.enableAltKey,
                newVal -> config.enableAltKey = newVal);

        // Trading Hud
        Option<Boolean> enableTradingHudOpt = ConfigUtils.buildBooleanOption(
                "enableTradingHud",
                DEFAULTS.enableTradingHud,
                () -> config.enableTradingHud,
                newVal -> config.enableTradingHud = newVal);
        Option<Integer> tradingHudXOffset = ConfigUtils.<Integer>getGenericOption("tradingHudXOffset")
                .binding(
                        DEFAULTS.tradingHudXOffset,
                        () -> config.tradingHudXOffset,
                        newVal -> config.tradingHudXOffset = newVal)
                .controller(IntegerFieldControllerBuilder::create)
                .build();
        Option<Integer> tradingHudYOffset = ConfigUtils.<Integer>getGenericOption("tradingHudYOffset")
                .binding(
                        DEFAULTS.tradingHudYOffset,
                        () -> config.tradingHudYOffset,
                        newVal -> config.tradingHudYOffset = newVal)
                .controller(IntegerFieldControllerBuilder::create)
                .build();
        Option<Boolean> renderRealCostDirectlyOpt = ConfigUtils.buildBooleanOption(
                "renderRealCostDirectly",
                DEFAULTS.renderRealCostDirectly,
                () -> config.renderRealCostDirectly,
                newVal -> config.renderRealCostDirectly = newVal);

        Option<Boolean> enableDisplayRemainingSalesOpt = ConfigUtils.buildBooleanOption(
                "enableDisplayRemainingSales",
                DEFAULTS.enableDisplayRemainingSales,
                () -> config.enableDisplayRemainingSales,
                newVal -> config.enableDisplayRemainingSales = newVal);

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.better_client:config"))
                .category(ConfigCategory.createBuilder()
                        .name(ConfigUtils.getCategoryName(CLIENT_CATEGORY))
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, FAST_TRADING_GROUP))
                                .options(List.of(enableFastTradingOpt, enableAltKeyOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, TRADING_HUD_GROUP))
                                .options(List.of(
                                        enableTradingHudOpt,
                                        tradingHudXOffset,
                                        tradingHudYOffset,
                                        renderRealCostDirectlyOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, REMAINING_SALES_GROUP))
                                .options(List.of(enableDisplayRemainingSalesOpt))
                                .build())
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}
