package com.euphony.better_client.client;

import com.euphony.better_client.client.command.ClientWeatherCommand;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;

public class BCClientCommands {
    public static void init() {
        ClientCommandRegistrationEvent.EVENT.register(ClientWeatherCommand::register);
    }
}
