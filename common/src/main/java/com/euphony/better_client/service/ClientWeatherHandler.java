package com.euphony.better_client.service;

import com.euphony.better_client.utils.enums.ClientWeather;

public class ClientWeatherHandler {
    private static ClientWeather weatherMode = ClientWeather.OFF;

    public static void setMode(ClientWeather mode) {
        weatherMode = mode;
    }

    public static ClientWeather getMode() {
        return weatherMode;
    }
}
