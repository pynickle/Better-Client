package com.euphony.better_client.client.command;

import com.euphony.better_client.service.ClientWeatherHandler;
import com.euphony.better_client.utils.enums.ClientWeather;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import static com.euphony.better_client.BetterClient.config;
import static com.euphony.better_client.utils.mc.ChatUtils.sendClientMsgTranslatable;

public class ClientWeatherCommand {
    public static <S> void register(CommandDispatcher<S> dispatcher) {
        if (!config.enableClientWeather) return;

        LiteralArgumentBuilder<S> command = LiteralArgumentBuilder.literal("cweather");

        command.then(LiteralArgumentBuilder.<S>literal("off").executes(context -> {
            ClientWeatherHandler.setMode(ClientWeather.OFF);
            sendClientMsgTranslatable("command.cweather.off");
            return 1;
        }));

        command.then(LiteralArgumentBuilder.<S>literal("clear").executes(context -> {
            ClientWeatherHandler.setMode(ClientWeather.CLEAR);
            sendClientMsgTranslatable("command.cweather.clear");
            return 1;
        }));

        command.then(LiteralArgumentBuilder.<S>literal("rain").executes(context -> {
            ClientWeatherHandler.setMode(ClientWeather.RAIN);
            sendClientMsgTranslatable("command.cweather.rain");
            return 1;
        }));

        command.then(LiteralArgumentBuilder.<S>literal("thunder").executes(context -> {
            ClientWeatherHandler.setMode(ClientWeather.THUNDER);
            sendClientMsgTranslatable("command.cweather.thunder");
            return 1;
        }));

        dispatcher.register(command);
    }
}
