package com.euphony.better_client.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryBoundaryTest {
    private static final Instant DISCONNECT = Instant.parse("2024-01-02T03:04:05Z");
    private static final ZoneOffset UTC = ZoneOffset.UTC;
    private static final String PREVIOUS_DEFAULT = "&8&m--------------------------------------------------\n"
            + " &6[&eRestored Chat History&6]\n"
            + " &7Session: &f{session_name} &8| &7Disconnected: &f{disconnect_time}\n"
            + "&8&m--------------------------------------------------";

    @Test
    void restoreOfRealMessagesAnchorsNewestAndShowsDisconnectTime() {
        Line newest = new Line("newest");
        Line older = new Line("older");

        HistoryBoundary<Line> boundary = HistoryBoundary.restore(List.of(newest, older), DISCONNECT);

        assertEquals(List.of(newest, older), boundary.span());
        assertSame(newest, boundary.anchor());
        assertEquals(
                List.of("\u00A78[\u00A77Restored \u00A7f2024/01/02 03:04:05\u00A78]"),
                boundary.bannerLines(HistoryBoundary.DEFAULT_TEMPLATE, UTC));
    }

    @Test
    void restoreWithNoRealMessagesHasNoSpanAndNoBanner() {
        HistoryBoundary<Line> boundary = HistoryBoundary.restore(List.of(), DISCONNECT);

        assertTrue(boundary.span().isEmpty());
        assertNull(boundary.anchor());
        assertTrue(boundary.bannerLines(HistoryBoundary.DEFAULT_TEMPLATE, UTC).isEmpty());
        assertTrue(HistoryBoundary.<Line>restore(null, DISCONNECT).span().isEmpty());
    }

    @Test
    void messageReceivedAfterRestoreStaysOutsideSpan() {
        Line newest = new Line("newest");
        Line older = new Line("older");
        Line live = new Line("live");
        HistoryBoundary<Line> boundary = HistoryBoundary.restore(List.of(newest, older), DISCONNECT);

        HistoryBoundary<Line> after = boundary.retainPresent(List.of(live, newest, older));

        assertEquals(List.of(newest, older), after.span());
        assertSame(newest, after.anchor());
        assertFalse(after.contains(live));
    }

    @Test
    void removingAnchorMovesToNewestRemainingAndClearingSpanRemovesBanner() {
        Line newest = new Line("newest");
        Line middle = new Line("middle");
        Line older = new Line("older");
        HistoryBoundary<Line> boundary = HistoryBoundary.restore(List.of(newest, middle, older), DISCONNECT);

        HistoryBoundary<Line> moved = boundary.retainPresent(List.of(middle, older));
        assertSame(middle, moved.anchor());
        assertEquals(List.of(middle, older), moved.span());

        HistoryBoundary<Line> cleared = moved.retainPresent(List.of());
        assertNull(cleared.anchor());
        assertTrue(cleared.span().isEmpty());
        assertTrue(cleared.bannerLines(HistoryBoundary.DEFAULT_TEMPLATE, UTC).isEmpty());
    }

    @Test
    void removingMessageOutsideSpanKeepsAnchor() {
        Line newest = new Line("newest");
        Line older = new Line("older");
        Line outside = new Line("outside");
        HistoryBoundary<Line> boundary = HistoryBoundary.restore(List.of(newest, older), DISCONNECT);

        HistoryBoundary<Line> after = boundary.retainPresent(List.of(outside, newest, older).stream()
                .filter(line -> line != outside)
                .toList());

        assertSame(newest, after.anchor());
        assertEquals(List.of(newest, older), after.span());
    }

    @Test
    void sameTextIsNotTheSameMessage() {
        Line newest = new Line("same");
        Line older = new Line("older");
        HistoryBoundary<Line> boundary = HistoryBoundary.restore(List.of(newest, older), DISCONNECT);

        HistoryBoundary<Line> after = boundary.retainPresent(List.of(new Line("same"), older));

        assertSame(older, after.anchor());
        assertEquals(List.of(older), after.span());
    }

    @Test
    void blankTemplatesProduceNoBannerLines() {
        HistoryBoundary<Line> boundary = HistoryBoundary.restore(List.of(new Line("hello")), DISCONNECT);

        assertTrue(boundary.bannerLines("", UTC).isEmpty());
        assertTrue(boundary.bannerLines(" \n\t", UTC).isEmpty());
        assertTrue(boundary.bannerLines(null, UTC).isEmpty());
    }

    @Test
    void multiLineTemplateKeepsEmptyMiddleLine() {
        HistoryBoundary<Line> boundary = HistoryBoundary.restore(List.of(new Line("hello")), DISCONNECT);

        assertEquals(List.of("top", "", "bottom"), boundary.bannerLines("top\\n\\nbottom", UTC));
    }

    @Test
    void ampersandBecomesSectionSignAndDisconnectPlaceholdersFillIn() {
        HistoryBoundary<Line> boundary = HistoryBoundary.restore(List.of(new Line("hello")), DISCONNECT);

        assertEquals(
                List.of("\u00A7a03:04:05|2024/01/02|2024/01/02 03:04:05"),
                boundary.bannerLines("&a{disconnect_time}|{disconnect_date}|{disconnect_datetime}", UTC));
        assertEquals(
                List.of("03:04:05|2024/01/02|2024/01/02 03:04:05"),
                boundary.bannerLines(
                        "{last_disconnect_time}|{last_disconnect_date}|{last_disconnect_datetime}", UTC));
    }

    @Test
    void sessionTokensAndUnknownTokensStayLiteral() {
        HistoryBoundary<Line> boundary = HistoryBoundary.restore(List.of(new Line("hello")), DISCONNECT);

        assertEquals(
                List.of("{session_name}|{session_key}|{not_known}"),
                boundary.bannerLines("{session_name}|{session_key}|{not_known}", UTC));
    }

    @Test
    void placeholdersUseTheSuppliedZone() {
        HistoryBoundary<Line> boundary = HistoryBoundary.restore(List.of(new Line("hello")), DISCONNECT);

        assertEquals(
                List.of("2024/01/02 11:04:05"),
                boundary.bannerLines("{disconnect_datetime}", ZoneOffset.ofHours(8)));
    }

    @Test
    void previousFourLineDefaultMigratesAndOtherTemplatesDoNot() {
        assertEquals(PREVIOUS_DEFAULT, HistoryBoundary.PREVIOUS_DEFAULT_TEMPLATE);
        assertEquals(HistoryBoundary.DEFAULT_TEMPLATE, HistoryBoundary.migrateTemplate(PREVIOUS_DEFAULT));

        String oneCharacterOff = PREVIOUS_DEFAULT.substring(0, PREVIOUS_DEFAULT.length() - 1) + "=";
        assertEquals(oneCharacterOff, HistoryBoundary.migrateTemplate(oneCharacterOff));
        assertEquals(
                "hello {session_name}",
                HistoryBoundary.migrateTemplate("hello {session_name}"));
    }

    @Test
    void storedTranscriptDropsOnlyLegacyPrefixLinesAndKeepsOrder() {
        Line first = new Line("hello");
        Line legacy = new Line("\uE000old break");
        Line second = new Line("world");
        Line embedded = new Line("still \uE000 here");
        Line onlyPrefix = new Line("\uE000");

        List<Line> kept = HistoryBoundary.omitLegacyLines(
                List.of(legacy, first, onlyPrefix, second, embedded), Line::text);

        assertEquals(List.of(first, second, embedded), kept);

        HistoryBoundary<Line> boundary = HistoryBoundary.restore(kept, DISCONNECT);
        assertSame(first, boundary.anchor());
        assertFalse(boundary.contains(legacy));
        assertFalse(boundary.contains(onlyPrefix));
    }

    private static final class Line {
        private final String text;

        private Line(String text) {
            this.text = text;
        }

        private String text() {
            return text;
        }
    }
}
