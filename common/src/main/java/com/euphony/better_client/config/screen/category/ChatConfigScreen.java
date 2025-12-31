package com.euphony.better_client.config.screen.category;

import com.euphony.better_client.config.Config;
import com.euphony.better_client.config.ConfigUtils;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.List;

import static com.euphony.better_client.BetterClient.config;
import static com.euphony.better_client.config.Config.DEFAULTS;
import static com.euphony.better_client.config.YACLConfig.CLIENT_CATEGORY;

public class ChatConfigScreen {
    private static final String BETTER_CHAT_GROUP = "better_chat";
    private static final String TIMESTAMP_GROUP = "timestamp";
    private static final String CHAT_FORMATTER_GROUP = "chat_formatter";

    public static Screen generateScreen(Screen parent) {
        // Better Chat
        Option<Boolean> enableLongerChatHistoryOpt = ConfigUtils.buildBooleanOption(
                "enableLongerChatHistory",
                DEFAULTS.enableLongerChatHistory,
                () -> config.enableLongerChatHistory,
                newVal -> config.enableLongerChatHistory = newVal);

        Option<Integer> chatMaxMessagesOpt = ConfigUtils.<Integer>getGenericOption("chatMaxMessages")
                .binding(
                        DEFAULTS.chatMaxMessages,
                        () -> config.chatMaxMessages,
                        newVal -> config.chatMaxMessages = newVal)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(100, 32768))
                .build();

        Option<Boolean> enableTimeStampOpt = ConfigUtils.buildBooleanOption(
                "enableTimeStamp",
                "chat/timestamp",
                DEFAULTS.enableTimeStamp,
                () -> config.enableTimeStamp,
                newVal -> config.enableTimeStamp = newVal);

        Option<Color> timeStampColorOpt = ConfigUtils.<Color>getGenericOption("timeStampColor")
                .binding(
                        new Color(DEFAULTS.timeStampColor, false),
                        () -> new Color(config.timeStampColor, false),
                        newVal -> config.timeStampColor = newVal.getRGB())
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build();

        Option<Boolean> enableChatHistoryRetentionOpt = ConfigUtils.buildBooleanOption(
                "enableChatHistoryRetention",
                DEFAULTS.enableChatHistoryRetention,
                () -> config.enableChatHistoryRetention,
                newVal -> config.enableChatHistoryRetention = newVal);

        // Chat Formatter
        Option<Boolean> enableChatFormatterOpt = ConfigUtils.buildBooleanOption(
                "enableChatFormatter",
                DEFAULTS.enableChatFormatter,
                () -> config.enableChatFormatter,
                newVal -> config.enableChatFormatter = newVal);

        Option<String> posFormatOpt = ConfigUtils.<String>getGenericOption("posFormat")
                .binding(DEFAULTS.posFormat, () -> config.posFormat, newVal -> config.posFormat = newVal)
                .controller(StringControllerBuilder::create)
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.better_client:config"))
                .category(ConfigCategory.createBuilder()
                        .name(ConfigUtils.getCategoryName(CLIENT_CATEGORY))
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, BETTER_CHAT_GROUP))
                                .options(java.util.List.of(
                                        enableLongerChatHistoryOpt, chatMaxMessagesOpt, enableChatHistoryRetentionOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, TIMESTAMP_GROUP))
                                .options(List.of(enableTimeStampOpt, timeStampColorOpt))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(ConfigUtils.getGroupName(CLIENT_CATEGORY, CHAT_FORMATTER_GROUP))
                                .options(List.of(enableChatFormatterOpt, posFormatOpt))
                                .build())
                        .build())
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }
}
