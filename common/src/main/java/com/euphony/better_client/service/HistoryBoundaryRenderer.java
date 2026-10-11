package com.euphony.better_client.service;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.euphony.better_client.BetterClient.config;

/**
 * Draws the current history boundary in the gap between the anchor message and chat received after that restore.
 * Newest chat lines sit at the bottom of the screen, so the banner occupies the rows just below the anchor.
 */
public final class HistoryBoundaryRenderer {
    public static final Layout NONE = new Layout(0, -1, List.of(), null);

    private HistoryBoundaryRenderer() {
    }

    public static Layout layout(
            Minecraft minecraft,
            List<GuiMessage> allMessages,
            List<GuiMessage.Line> trimmedMessages,
            int chatScrollbarPos,
            int linesPerPage,
            int chatWidth,
            double chatScale) {
        HistoryBoundary<GuiMessage> boundary = ChatHistoryManager.syncHistoryBoundary(allMessages);
        GuiMessage anchor = boundary.anchor();
        if (anchor == null || trimmedMessages == null || trimmedMessages.isEmpty() || linesPerPage <= 0) {
            return NONE;
        }

        int anchorTrimmedIndex = -1;
        for (int index = 0; index < trimmedMessages.size(); index++) {
            GuiMessage.Line line = trimmedMessages.get(index);
            if (line != null && line.parent() == anchor) {
                anchorTrimmedIndex = index;
                break;
            }
        }
        if (anchorTrimmedIndex < 0) {
            return NONE;
        }

        int visible = Math.min(trimmedMessages.size() - chatScrollbarPos, linesPerPage);
        if (visible <= 0
                || anchorTrimmedIndex < chatScrollbarPos
                || anchorTrimmedIndex >= chatScrollbarPos + visible) {
            return NONE;
        }

        List<String> bannerLines = boundary.bannerLines(config.chatHistorySeparatorTemplate, ZoneId.systemDefault());
        if (bannerLines.isEmpty()) {
            return NONE;
        }

        int wrapWidth = chatScale > 0.0 ? Mth.floor(chatWidth / chatScale) : Math.max(1, chatWidth);
        List<FormattedCharSequence> rows = new ArrayList<>();
        for (String bannerLine : bannerLines) {
            rows.addAll(ComponentRenderUtils.wrapComponents(legacyLine(bannerLine), Math.max(1, wrapWidth), minecraft.font));
        }
        if (rows.isEmpty()) {
            return NONE;
        }
        return new Layout(rows.size(), anchorTrimmedIndex, List.copyOf(rows), anchor);
    }

    public static int shiftFor(int lineIndex, int chatScrollbarPos, Layout layout) {
        if (layout.shift() <= 0 || layout.anchorTrimmedIndex() < 0) {
            return 0;
        }
        int messageIndex = lineIndex + chatScrollbarPos;
        return messageIndex >= layout.anchorTrimmedIndex() ? layout.shift() : 0;
    }

    public static void draw(
            ChatComponent.ChatGraphicsAccess graphics,
            ChatComponent.DisplayMode displayMode,
            int screenHeight,
            int ticks,
            int bottomOffset,
            int lineHeight,
            double chatScale,
            int chatWidth,
            int chatScrollbarPos,
            int linesPerPage,
            Layout layout) {
        if (graphics.getClass().getName().endsWith("ClickableTextOnlyGraphicsAccess")
                || layout.shift() <= 0
                || layout.anchor() == null) {
            return;
        }

        float alpha = anchorAlpha(displayMode.foreground, ticks, layout.anchor().addedTime());
        if (alpha <= 1.0E-5F) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        float scale = (float) chatScale;
        if (scale == 0.0F) {
            return;
        }
        int chatBottom = Mth.floor((screenHeight - 40 - bottomOffset) / scale);
        int backgroundWidth = Mth.ceil(chatWidth / scale);
        float textOpacity = minecraft.options.chatOpacity().get().floatValue() * 0.9F + 0.1F;
        float backgroundOpacity = minecraft.options.textBackgroundOpacity().get().floatValue();
        double chatLineSpacing = minecraft.options.chatLineSpacing().get();
        int entryBottomToMessageY = (int) Math.round(8.0 * (chatLineSpacing + 1.0) - 4.0 * chatLineSpacing);
        int anchorVisual = layout.anchorTrimmedIndex() - chatScrollbarPos;
        List<FormattedCharSequence> rows = layout.rows();
        for (int row = 0; row < rows.size(); row++) {
            int visualIndex = anchorVisual + layout.shift() - 1 - row;
            if (visualIndex < 0 || visualIndex >= linesPerPage) {
                continue;
            }
            int entryBottom = chatBottom - visualIndex * lineHeight;
            int entryTop = entryBottom - lineHeight;
            graphics.fill(-4, entryTop, backgroundWidth + 4 + 4, entryBottom, ARGB.black(alpha * backgroundOpacity));
            graphics.handleMessage(entryBottom - entryBottomToMessageY, alpha * textOpacity, rows.get(row));
        }
    }

    private static float anchorAlpha(boolean foreground, int ticks, int addedTime) {
        if (foreground) {
            return 1.0F;
        }
        double fade = 1.0 - (ticks - addedTime) / 200.0;
        fade *= 10.0;
        fade = Mth.clamp(fade, 0.0, 1.0);
        fade *= fade;
        return (float) fade;
    }

    private static Component legacyLine(String line) {
        if (line == null || line.isEmpty()) {
            return Component.empty();
        }
        MutableComponent text = Component.empty();
        StringDecomposer.iterateFormatted(line, Style.EMPTY, (index, style, codePoint) -> {
            text.append(Component.literal(Character.toString(codePoint)).setStyle(style));
            return true;
        });
        return text;
    }

    public record Layout(int shift, int anchorTrimmedIndex, List<FormattedCharSequence> rows, GuiMessage anchor) {
    }
}
