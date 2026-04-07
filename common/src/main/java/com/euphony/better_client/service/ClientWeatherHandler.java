package com.euphony.better_client.service;

import static com.euphony.better_client.BetterClient.config;

import com.euphony.better_client.utils.enums.ClientWeather;

public class ClientWeatherHandler {
    private static ClientWeather weatherMode = ClientWeather.OFF;

    public static void setMode(ClientWeather mode) {
        weatherMode = mode;
    }

    public static ClientWeather getMode() {
        if (!config.enableClientWeather && weatherMode != ClientWeather.OFF) {
            weatherMode = ClientWeather.OFF;
        }
        return weatherMode;
    }
}
