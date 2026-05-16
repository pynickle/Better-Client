package com.euphony.better_client.utils.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatMentionUtils {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@[A-Za-z0-9_]{1,16}");
    private static final Style DEFAULT_STYLE = Style.EMPTY.withColor(EditBox.DEFAULT_TEXT_COLOR);

    private ChatMentionUtils() {}

    public static boolean isAvailableInCurrentSession() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getConnection() != null && !minecraft.isLocalServer();
    }

    public static FormattedCharSequence formatMentions(String value, int mentionColor) {
        if (value == null || value.isEmpty() || value.startsWith("/")) {
            return null;
        }

        Matcher matcher = MENTION_PATTERN.matcher(value);
        List<FormattedCharSequence> parts = new ArrayList<>();
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                parts.add(FormattedCharSequence.forward(value.substring(lastEnd, matcher.start()), DEFAULT_STYLE));
            }
            parts.add(FormattedCharSequence.forward(
                    value.substring(matcher.start(), matcher.end()), Style.EMPTY.withColor(mentionColor)));
            lastEnd = matcher.end();
        }

        if (parts.isEmpty()) {
            return null;
        }

        if (lastEnd < value.length()) {
            parts.add(FormattedCharSequence.forward(value.substring(lastEnd), DEFAULT_STYLE));
        }

        return FormattedCharSequence.composite(parts);
    }

    public static Component colorMentions(Component message, int mentionColor) {
        MutableComponent output = Component.empty();
        boolean modified = false;

        for (Component part : message.toFlatList()) {
            String text = part.getString();
            Matcher matcher = MENTION_PATTERN.matcher(text);
            int lastEnd = 0;

            while (matcher.find()) {
                modified = true;
                if (matcher.start() > lastEnd) {
                    output.append(Component.literal(text.substring(lastEnd, matcher.start()))
                            .withStyle(part.getStyle()));
                }

                output.append(Component.literal(text.substring(matcher.start(), matcher.end()))
                        .withStyle(part.getStyle().withColor(mentionColor)));
                lastEnd = matcher.end();
            }

            if (lastEnd == 0) {
                output.append(part.copy());
                continue;
            }

            if (lastEnd < text.length()) {
                output.append(Component.literal(text.substring(lastEnd)).withStyle(part.getStyle()));
            }
        }

        return modified ? output : message;
    }
}
