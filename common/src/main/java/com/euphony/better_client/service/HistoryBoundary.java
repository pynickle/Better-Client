package com.euphony.better_client.service;

import com.euphony.better_client.utils.FormatUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Render state for the current restore: which real messages belong to the restored span,
 * which of them is the anchor, and which banner lines to draw. The boundary is not a chat message.
 * Span membership follows the message object captured at restore, not equal text.
 */
public final class HistoryBoundary<T> {
    public static final char LEGACY_LINE_PREFIX = '\uE000';
    public static final String DEFAULT_TEMPLATE = "&8[&7Restored &f{disconnect_datetime}&8]";
    public static final String PREVIOUS_DEFAULT_TEMPLATE = "&8&m" + "-".repeat(50) + "\n"
            + " &6[&eRestored Chat History&6]\n"
            + " &7Session: &f{session_name} &8| &7Disconnected: &f{disconnect_time}\n"
            + "&8&m" + "-".repeat(50);

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ROOT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.ROOT);
    private static final HistoryBoundary<?> EMPTY = new HistoryBoundary<>(List.of(), null);

    private final List<T> spanNewestFirst;
    private final Instant disconnectTime;

    private HistoryBoundary(List<T> spanNewestFirst, Instant disconnectTime) {
        this.spanNewestFirst = spanNewestFirst;
        this.disconnectTime = disconnectTime;
    }

    @SuppressWarnings("unchecked")
    public static <T> HistoryBoundary<T> none() {
        return (HistoryBoundary<T>) EMPTY;
    }

    public static <T> HistoryBoundary<T> restore(List<T> realMessagesNewestFirst, Instant disconnectTime) {
        if (realMessagesNewestFirst == null || realMessagesNewestFirst.isEmpty()) {
            return none();
        }

        List<T> span = new ArrayList<>();
        for (T message : realMessagesNewestFirst) {
            if (message != null) {
                span.add(message);
            }
        }
        if (span.isEmpty()) {
            return none();
        }
        return new HistoryBoundary<>(List.copyOf(span), disconnectTime);
    }

    public List<T> span() {
        return spanNewestFirst;
    }

    public T anchor() {
        return spanNewestFirst.isEmpty() ? null : spanNewestFirst.get(0);
    }

    public boolean contains(T message) {
        return containsReference(spanNewestFirst, message);
    }

    public HistoryBoundary<T> retainPresent(Iterable<T> present) {
        if (spanNewestFirst.isEmpty()) {
            return this;
        }

        Set<T> presentMessages = Collections.newSetFromMap(new IdentityHashMap<>());
        if (present != null) {
            for (T message : present) {
                if (message != null) {
                    presentMessages.add(message);
                }
            }
        }

        List<T> kept = new ArrayList<>();
        for (T message : spanNewestFirst) {
            if (presentMessages.contains(message)) {
                kept.add(message);
            }
        }
        if (kept.size() == spanNewestFirst.size()) {
            return this;
        }
        if (kept.isEmpty()) {
            return none();
        }
        return new HistoryBoundary<>(List.copyOf(kept), disconnectTime);
    }

    public List<String> bannerLines(String template, ZoneId zone) {
        if (spanNewestFirst.isEmpty() || disconnectTime == null) {
            return List.of();
        }
        return formatBanner(template, disconnectTime, zone);
    }

    public static List<String> formatBanner(String template, Instant disconnectTime, ZoneId zone) {
        if (template == null || disconnectTime == null || zone == null) {
            return List.of();
        }

        String normalized = template.replace("\\n", "\n").replace('&', '\u00A7');
        ZonedDateTime zoned = disconnectTime.atZone(zone);
        String formatted = FormatUtils.format(
                normalized,
                Map.of(
                        "disconnect_time", TIME_FORMATTER.format(zoned),
                        "disconnect_date", DATE_FORMATTER.format(zoned),
                        "disconnect_datetime", DATE_TIME_FORMATTER.format(zoned),
                        "last_disconnect_time", TIME_FORMATTER.format(zoned),
                        "last_disconnect_date", DATE_FORMATTER.format(zoned),
                        "last_disconnect_datetime", DATE_TIME_FORMATTER.format(zoned)));
        if (formatted.isBlank()) {
            return List.of();
        }
        return List.copyOf(Arrays.asList(formatted.split("\n", -1)));
    }

    public static String migrateTemplate(String stored) {
        if (PREVIOUS_DEFAULT_TEMPLATE.equals(stored)) {
            return DEFAULT_TEMPLATE;
        }
        return stored;
    }

    public static boolean isLegacyLine(String plainText) {
        return plainText != null && !plainText.isEmpty() && plainText.charAt(0) == LEGACY_LINE_PREFIX;
    }

    public static <T> List<T> omitLegacyLines(List<T> messages, Function<T, String> plainText) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        List<T> kept = new ArrayList<>();
        for (T message : messages) {
            String text = message == null || plainText == null ? null : plainText.apply(message);
            if (isLegacyLine(text)) {
                continue;
            }
            kept.add(message);
        }
        return Collections.unmodifiableList(kept);
    }

    private static <T> boolean containsReference(List<T> messages, T target) {
        for (T message : messages) {
            if (message == target) {
                return true;
            }
        }
        return false;
    }
}
