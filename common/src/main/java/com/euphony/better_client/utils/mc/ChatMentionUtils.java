package com.euphony.better_client.utils.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatMentionUtils {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@[A-Za-z0-9_]{1,16}");
    private static final Style DEFAULT_STYLE = Style.EMPTY.withColor(EditBox.DEFAULT_TEXT_COLOR);

    private ChatMentionUtils() {
    }

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
        Component colored = colorComponent(message, mentionColor);
        return colored == null ? message : colored;
    }

    /**
     * @return a rewritten copy when a plain-text mention was recolored, otherwise null
     */
    private static Component colorComponent(Component component, int mentionColor) {
        MutableComponent rebuilt = colorContents(component, mentionColor);
        boolean changed = rebuilt != null;

        List<Component> siblings = component.getSiblings();
        List<Component> rewrittenSiblings = null;
        for (int index = 0; index < siblings.size(); index++) {
            Component rewrittenSibling = colorComponent(siblings.get(index), mentionColor);
            if (rewrittenSibling == null) {
                continue;
            }
            if (rewrittenSiblings == null) {
                rewrittenSiblings = new ArrayList<>(siblings);
            }
            rewrittenSiblings.set(index, rewrittenSibling);
            changed = true;
        }

        if (!changed) {
            return null;
        }

        if (rebuilt == null) {
            rebuilt = component.plainCopy().withStyle(component.getStyle());
        }
        for (Component sibling : rewrittenSiblings != null ? rewrittenSiblings : siblings) {
            rebuilt.append(sibling);
        }
        return rebuilt;
    }

    private static MutableComponent colorContents(Component component, int mentionColor) {
        ComponentContents contents = component.getContents();
        Style style = component.getStyle();
        if (contents instanceof PlainTextContents plain) {
            return colorPlainText(plain.text(), style, mentionColor);
        }
        if (contents instanceof TranslatableContents translatable) {
            Object[] rewrittenArgs = colorTranslatableArgs(translatable.getArgs(), mentionColor);
            if (rewrittenArgs == null) {
                return null;
            }
            return Component.translatableWithFallback(translatable.getKey(), translatable.getFallback(), rewrittenArgs)
                    .withStyle(style);
        }
        if (contents instanceof ObjectContents object && object.fallback().isPresent()) {
            Component rewrittenFallback = colorComponent(object.fallback().get(), mentionColor);
            if (rewrittenFallback == null) {
                return null;
            }
            return Component.object(object.contents(), rewrittenFallback).withStyle(style);
        }
        if (contents instanceof SelectorContents selector && selector.separator().isPresent()) {
            Component rewrittenSeparator = colorComponent(selector.separator().get(), mentionColor);
            if (rewrittenSeparator == null) {
                return null;
            }
            return Component.selector(selector.selector(), Optional.of(rewrittenSeparator)).withStyle(style);
        }
        if (contents instanceof NbtContents nbt && nbt.separator().isPresent()) {
            Component rewrittenSeparator = colorComponent(nbt.separator().get(), mentionColor);
            if (rewrittenSeparator == null) {
                return null;
            }
            return Component.nbt(
                            nbt.nbtPath(),
                            nbt.interpreting(),
                            nbt.plain(),
                            Optional.of(rewrittenSeparator),
                            nbt.dataSource())
                    .withStyle(style);
        }
        return null;
    }

    private static Object[] colorTranslatableArgs(Object[] args, int mentionColor) {
        Object[] rewritten = null;
        for (int index = 0; index < args.length; index++) {
            if (!(args[index] instanceof Component arg)) {
                continue;
            }
            Component colored = colorComponent(arg, mentionColor);
            if (colored == null) {
                continue;
            }
            if (rewritten == null) {
                rewritten = args.clone();
            }
            rewritten[index] = colored;
        }
        return rewritten;
    }

    private static MutableComponent colorPlainText(String text, Style style, int mentionColor) {
        Matcher matcher = MENTION_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        matcher.reset();
        MutableComponent root = null;
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                root = appendLiteral(root, text.substring(lastEnd, matcher.start()), style);
            }
            root = appendLiteral(
                    root, text.substring(matcher.start(), matcher.end()), style.withColor(mentionColor));
            lastEnd = matcher.end();
        }
        if (lastEnd < text.length()) {
            root = appendLiteral(root, text.substring(lastEnd), style);
        }
        return root;
    }

    private static MutableComponent appendLiteral(MutableComponent root, String text, Style style) {
        MutableComponent part = Component.literal(text).withStyle(style);
        if (root == null) {
            return part;
        }
        root.append(part);
        return root;
    }
}
