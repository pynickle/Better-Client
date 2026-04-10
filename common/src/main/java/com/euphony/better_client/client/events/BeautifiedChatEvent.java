package com.euphony.better_client.client.events;

import com.euphony.better_client.event.CompoundEventResult;
import com.euphony.better_client.utils.mc.ChatMentionUtils;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.euphony.better_client.BetterClient.config;

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
        Component output = message;

        if (config.enableTimeStamp && message.getString().matches(VANILLA_FORMAT)) {
            MutableComponent timestampedOutput = Component.empty();
            ZonedDateTime now = ZonedDateTime.now();
            String shortTimestamp = now.format(DateTimeFormatter.ofPattern("'['HH:mm:ss']' "));

            String fullDateTimeText =
                    now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) + "\nUTC" + now.getOffset();

            Component fullHoverText = Component.literal(fullDateTimeText);

            HoverEvent hoverEvent = new HoverEvent.ShowText(fullHoverText);

            timestampedOutput.append(Component.literal(shortTimestamp)
                    .withColor(config.timeStampColor)
                    .withStyle(style -> style.withHoverEvent(hoverEvent)));
            timestampedOutput.append(message);
            message = timestampedOutput;
        }

        if (config.enableChatMentionAutocomplete && ChatMentionUtils.isAvailableInCurrentSession()) {
            output = ChatMentionUtils.colorMentions(message, config.chatMentionColor);
        } else {
            output = message;
        }

        return output;
    }
}
