package com.euphony.better_client.client.events;

import static com.euphony.better_client.BetterClient.config;

import dev.architectury.event.CompoundEventResult;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class BeautifiedChatEvent {
    public static final String VANILLA_FORMAT = "(?i)^<[a-z0-9_]{3,16}>\\s.+$";

    public static CompoundEventResult<Component> chatReceived(ChatType.Bound bound, Component component) {
        Component newMessage = processMessage(component);

        if (component != newMessage) {
            return CompoundEventResult.interruptTrue(newMessage);
        }
        return CompoundEventResult.pass();
    }

    public static Component processMessage(Component message) {
        if (message.getString().matches(VANILLA_FORMAT)) {
            MutableComponent output = Component.empty();
            if (config.enableTimeStamp) {
                Date now = new Date();
                String timestamp = new SimpleDateFormat("[dd:HH:mm] ").format(now);

                output.append(Component.literal(timestamp).withColor(config.timeStampColor));
            }
            output.append(message);
            return output;
        }
        return message.copy();
    }
}
