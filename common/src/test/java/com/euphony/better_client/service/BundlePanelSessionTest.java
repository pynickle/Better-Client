package com.euphony.better_client.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.ArrayList;

import static com.euphony.better_client.service.BundlePanelSession.*;
import static org.junit.jupiter.api.Assertions.*;

class BundlePanelSessionTest {
    private static final Item STONE = new Item("stone", "Stone", 64, true, false);
    private static final Item BAG = new Item("bundle", "Bundle", 1, false, true);

    @Test
    void discoversOnlyDirectlyCarriedBagsAndShowsCompleteFirstLevelContents() {
        List<Stack> contents = java.util.stream.IntStream.range(0, 15)
                .mapToObj(i -> new Stack(new Item("item" + i, "Item " + i, 64, true, false), 1)).toList();
        Menu menu = new Menu(List.of(
                bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 10))),
                bag(9, Region.MAIN, 9, contents),
                bag(45, Region.OFFHAND, 40, List.of(new Stack(BAG, 1))),
                bag(5, Region.OTHER, 39, List.of(new Stack(STONE, 20)))));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);

        assertEquals(List.of(9, 36, 45), session.state().bags().stream().map(Slot::id).toList());
        assertEquals(17, session.state().entries().size());
        assertEquals(10, session.state().entries().stream().filter(e -> e.item().equals(STONE)).findFirst().orElseThrow().count());
        assertTrue(session.state().expanded());
    }

    private static Slot bag(int id, Region region, int position, List<Stack> contents) {
        return new Slot(id, region, position, bundleStack(BAG, contents), contents, 64, -1);
    }

    private record BagKey(Object appearance, List<Stack> contents) {}
    private static Stack bundleStack(Item item, List<Stack> contents) {
        Object appearance = item.key() instanceof BagKey key ? key.appearance() : item.key();
        return new Stack(new Item(new BagKey(appearance, List.copyOf(contents)), item.name(), 1, false, true), 1);
    }
    private static int weight(Stack stack) {
        if (stack.empty()) return 0;
        return stack.count() * (stack.item().bundle() ? 4 + contents(stack).stream().mapToInt(BundlePanelSessionTest::weight).sum()
                : 64 / stack.item().maxStack());
    }
    private static List<Stack> contents(Stack stack) {
        return !stack.empty() && stack.item().key() instanceof BagKey key ? key.contents() : List.of();
    }

    @Test
    void assemblesOneCursorStackAcrossBagsAndReleasesTemporarySpace() {
        Menu menu = new Menu(List.of(bag(9, Region.MAIN, 9, List.of(new Stack(STONE, 10))),
                bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 20))), empty(10)));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(session.state().entries().getFirst().id(), -1, Gesture.LEFT, false, false);
        finish(session);

        assertEquals(new Stack(STONE, 30), session.state().inventory().cursor());
        assertTrue(session.state().inventory().slots().stream().filter(s -> s.id() == 10).findFirst().orElseThrow().stack().empty());
        assertTrue(session.state().bags().stream().allMatch(s -> s.contents().isEmpty()));
        assertEquals(30, session.outcome().moved());
    }

    private static Slot empty(int id) { return new Slot(id, Region.MAIN, id, Stack.EMPTY, List.of(), 64, -1); }

    @ParameterizedTest
    @CsvSource({"64,100,LEFT,false,false,64,false", "64,100,RIGHT,false,false,32,false",
            "64,11,RIGHT,false,false,6,false", "16,11,RIGHT,false,false,6,false",
            "16,30,LEFT,false,false,16,false", "1,1,RIGHT,false,false,1,false",
            "64,30,LEFT,true,false,1,false", "64,100,LEFT,false,true,64,true",
            "64,100,LEFT,true,true,100,true", "64,30,WHEEL_DOWN,false,true,1,true",
            "16,30,WHEEL_DOWN,true,true,16,true", "64,30,WHEEL_DOWN,false,false,0,false"})
    void extractionGesturesRespectQuantitiesLimitsAndDestinations(int limit, int total, Gesture gesture,
            boolean control, boolean shift, int expected, boolean intoMain) {
        Item item = new Item("test", "Test", limit, limit > 1, false);
        int first = Math.min(total, limit);
        Menu menu = new Menu(new ArrayList<>(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(item, first))),
                bag(37, Region.HOTBAR, 1, total > first ? List.of(new Stack(item, total - first)) : List.of()),
                empty(9), empty(10), empty(11))));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(session.state().entries().getFirst().id(), -1, gesture, control, shift);
        finish(session);

        assertEquals(expected, session.outcome().moved());
        assertEquals(intoMain ? 0 : expected, session.state().inventory().cursor().count());
        assertEquals(intoMain ? expected : 0, session.state().inventory().slots().stream()
                .filter(s -> s.region() == Region.MAIN).mapToInt(s -> s.stack().count()).sum());
        assertEquals(total - expected, session.state().bags().stream()
                .flatMap(s -> s.contents().stream()).mapToInt(Stack::count).sum());
    }
    private static void finish(BundlePanelSession session) {
        for (int i = 0; i < 2000 && session.busy(); i++) session.tick();
        assertFalse(session.busy());
    }

    @ParameterizedTest
    @CsvSource({"LEFT,false,false,20", "RIGHT,false,false,1", "LEFT,true,false,-1",
            "LEFT,true,true,0", "LEFT,false,true,0", "WHEEL_UP,false,true,0"})
    void occupiedCursorUsesCarriedItemAndRecognizesExtractionBeforeDeposit(Gesture gesture,
            boolean control, boolean shift, int deposited) {
        Item dirt = new Item("dirt", "Dirt", 64, true, false);
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 10))), empty(9)));
        menu.cursor(new Stack(control && !shift ? STONE : dirt, 20));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(session.state().entries().getFirst().id(), -1, gesture, control, shift);
        finish(session);

        assertEquals(20 - deposited, session.state().inventory().cursor().count());
        assertEquals(10 + deposited, session.state().bags().getFirst().contents().stream().mapToInt(Stack::count).sum());
        assertTrue(session.state().inventory().slots().stream().filter(s -> s.id() == 9).findFirst().orElseThrow().stack().empty());
    }

    @Test
    void wheelDepositsUseOnlyMatchingMainInventoryItemsAndPreferExistingContents() {
        Item dirt = new Item("dirt", "Dirt", 64, true, false);
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of()),
                bag(37, Region.HOTBAR, 1, List.of(new Stack(STONE, 60))),
                new Slot(9, Region.MAIN, 9, new Stack(STONE, 10), List.of(), 64, -1),
                new Slot(10, Region.MAIN, 10, new Stack(dirt, 32), List.of(), 64, -1),
                new Slot(38, Region.HOTBAR, 2, new Stack(STONE, 30), List.of(), 64, -1)));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(session.state().entries().getFirst().id(), -1, Gesture.WHEEL_UP, true, true);
        finish(session);

        assertEquals(10, session.outcome().moved());
        assertEquals(64, session.state().bags().get(1).contents().getFirst().count());
        assertEquals(6, session.state().bags().getFirst().contents().getFirst().count());
        assertEquals(32, menu.slot(10).stack().count());
        assertEquals(30, menu.slot(38).stack().count());
        assertTrue(menu.read().cursor().empty());
    }

    @Test
    void explicitNestingKeepsTheChosenBagAsTheOuterBagAndRetrievalDiscoversTheInnerBag() {
        Item blue = new Item("blue", "Blue bundle", 1, false, true);
        Stack inner = bundleStack(blue, List.of(new Stack(STONE, 10)));
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of()), empty(9)));
        menu.cursor(inner);
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(null, -1, Gesture.LEFT, false, false);
        assertEquals(Failure.EXPLICIT_TARGET, session.outcome().failure());
        assertEquals(inner, menu.read().cursor());
        session.view(View.GROUPED);
        session.interact(null, 36, Gesture.LEFT, false, false);
        finish(session);
        assertEquals("Bundle", session.state().bags().getFirst().stack().item().name());
        assertEquals(inner, session.state().bags().getFirst().contents().getFirst());
        assertTrue(menu.read().cursor().empty());
        assertTrue(menu.slot(9).stack().empty());
        session.interact(session.state().entries().getFirst().id(), 36, Gesture.LEFT, false, true);
        finish(session);
        assertEquals(List.of(9, 36), session.state().bags().stream().map(Slot::id).toList());
        assertEquals(List.of(new Stack(STONE, 10)), menu.slot(9).contents());
    }

    @Test
    void partialMainStackAllowsPreciseRetrievalWithoutAnEmptyTemporarySlot() {
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 11))),
                new Slot(9, Region.MAIN, 9, new Stack(STONE, 63), List.of(), 64, -1)));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        EntryId entry = session.state().entries().getFirst().id();
        session.interact(entry, -1, Gesture.RIGHT, false, false);
        assertEquals(Failure.TEMPORARY_SLOT, session.outcome().failure());
        assertTrue(menu.read().cursor().empty());
        session.interact(entry, -1, Gesture.LEFT, false, true);
        finish(session);
        assertEquals(64, menu.slot(9).stack().count());
        assertEquals(10, session.state().bags().getFirst().contents().getFirst().count());
        assertEquals(1, session.outcome().moved());
    }

    @Test
    void unrelatedInventoryAndSelectionChangesDoNotInterruptTransfer() {
        Item dirt = new Item("dirt", "Dirt", 64, true, false);
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 11))), empty(9), empty(10)));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(session.state().entries().getFirst().id(), -1, Gesture.RIGHT, false, false);
        session.tick();
        menu.replace(new Slot(10, Region.MAIN, 10, new Stack(dirt, 5), List.of(), 64, -1), menu.read().cursor());
        menu.select(36, 0);
        finish(session);
        assertEquals(Failure.NONE, session.outcome().failure());
        assertEquals(new Stack(STONE, 6), menu.read().cursor());
        assertEquals(new Stack(dirt, 5), menu.slot(10).stack());
        assertEquals(List.of(new Stack(STONE, 5)), menu.slot(36).contents());
        assertTrue(menu.slot(9).stack().empty());
    }

    @Test
    void correctionsStopPendingClicksAndRetainOwnershipOfTransientItems() {
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 11))), empty(9)));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(session.state().entries().getFirst().id(), -1, Gesture.RIGHT, false, false);
        session.tick(); // Whole entry is on the cursor, before splitting.
        menu.snapshot = new Snapshot(1, 1, true, menu.snapshot.cursor(), menu.snapshot.slots());
        finish(session);
        assertEquals(Failure.CHANGED, session.outcome().failure());
        assertEquals(11, menu.read().cursor().count() + menu.read().slots().stream()
                .mapToInt(s -> s.contents().stream().mapToInt(Stack::count).sum() + (s.bag() ? 0 : s.stack().count())).sum());
        assertTrue(menu.slot(9).stack().empty());
    }

    @Test
    void projectionSeparatesComponentsAndNonStackablesAndKeepsDepletedEntriesStable() {
        Item variant = new Item("named stone", "Stone", 64, true, false);
        Item tool = new Item("tool", "Tool", 1, false, false);
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0,
                List.of(new Stack(STONE, 1), new Stack(variant, 1), new Stack(tool, 1), new Stack(tool, 1))), empty(9)));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        assertEquals(4, session.state().entries().size());
        session.inputHeld(true);
        Entry selected = session.state().entries().getFirst();
        session.interact(selected.id(), -1, Gesture.LEFT, false, false);
        finish(session);
        assertEquals(selected.id(), session.state().entries().getFirst().id());
        assertEquals(0, session.state().entries().getFirst().count());
        session.interact(selected.id(), -1, Gesture.LEFT, true, false);
        assertEquals(Failure.UNAVAILABLE, session.outcome().failure());
        session.inputHeld(false);
        assertEquals(3, session.state().entries().size());
        session.search("Tool");
        assertEquals(2, session.state().entries().size());
        session.view(View.GROUPED);
        assertEquals(View.GROUPED, session.state().view());
        assertTrue(session.state().entries().stream().allMatch(e -> e.bag() == 36));
        session.expanded(false);
        assertFalse(session.state().expanded());
    }

    @Test
    void compactionPreservesOrderAndNewItemsAppearWhileInputIsHeld() {
        Item apple = new Item("apple", "Apple", 64, true, false);
        Item wood = new Item("wood", "Wood", 64, true, false);
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0,
                List.of(new Stack(STONE, 1), new Stack(wood, 2)))));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        EntryId stone = session.state().entries().getFirst().id();
        session.inputHeld(true);
        session.interact(stone, -1, Gesture.LEFT, false, false);
        finish(session);
        menu.replace(bag(36, Region.HOTBAR, 0, List.of(new Stack(apple, 3), new Stack(wood, 2))), menu.read().cursor());
        assertEquals(List.of(0, 2, 3), session.state().entries().stream().map(Entry::count).toList());

        session.inputHeld(false);
        assertEquals(List.of(wood, apple), session.state().entries().stream().map(Entry::item).toList());
        assertEquals(new Stack(STONE, 1), session.state().inventory().cursor());
        session.search("");
        assertEquals(List.of(apple, wood), session.state().entries().stream().map(Entry::item).toList());
    }

    @Test
    void groupedCellsRefreshWhileHeldWithoutMovingOtherBagsAndCompactOnRelease() {
        Item apple = new Item("apple", "Apple", 64, true, false);
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 2))),
                bag(37, Region.HOTBAR, 1, List.of()), empty(9)));
        BundlePanelSession session = new BundlePanelSession(menu, View.GROUPED, true);
        assertEquals(8, session.cells(2).size());
        session.inputHeld(true);
        menu.replace(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 1), new Stack(apple, 3))), Stack.EMPTY);
        List<Cell> changed = session.cells(2);
        assertEquals(1, changed.get(2).entry().count());
        assertEquals(37, changed.get(4).bag().id());
        assertTrue(changed.stream().skip(8).anyMatch(c -> c.entry() != null && c.entry().item().equals(apple)));

        menu.replace(new Slot(36, Region.HOTBAR, 0, Stack.EMPTY, List.of(), 64, -1), Stack.EMPTY);
        menu.replace(bag(9, Region.MAIN, 9, List.of(new Stack(STONE, 1))), Stack.EMPTY);
        changed = session.cells(2);
        assertNull(changed.getFirst().bag());
        assertEquals(0, changed.get(2).entry().count());
        assertEquals(37, changed.get(4).bag().id());
        assertTrue(changed.stream().anyMatch(c -> c.bag() != null && c.bag().id() == 9));

        session.inputHeld(false);
        List<Cell> compact = session.cells(2);
        assertEquals(8, compact.size());
        assertEquals(37, compact.getFirst().bag().id());
        assertEquals(9, compact.get(4).bag().id());
        assertEquals(37, compact.get(2).owner()); // Empty bags still have a deposit row.
        assertFalse(compact.get(2).header());
    }

    @Test
    void groupedHeadingReturnsWhenABagIsPlacedBackBeforeRelease() {
        Slot bag = bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 2)));
        Menu menu = new Menu(List.of(bag));
        BundlePanelSession session = new BundlePanelSession(menu, View.GROUPED, true);
        session.inputHeld(true);
        assertEquals(bag, session.cells(2).getFirst().bag());
        menu.replace(new Slot(36, Region.HOTBAR, 0, Stack.EMPTY, List.of(), 64, -1), bag.stack());
        assertNull(session.cells(2).getFirst().bag());
        menu.replace(bag, Stack.EMPTY);
        assertEquals(bag, session.cells(2).getFirst().bag());
        assertEquals(4, session.cells(2).size());
    }

    @Test
    void repeatedClicksAreQueuedAndResolveAgainstTheUpdatedCursor() {
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 11))), empty(9)));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        EntryId entry = session.state().entries().getFirst().id();
        session.interact(entry, -1, Gesture.LEFT, true, false);
        session.interact(entry, -1, Gesture.LEFT, true, false);
        session.interact(entry, -1, Gesture.LEFT, true, false);
        finish(session);
        assertEquals(new Stack(STONE, 3), menu.read().cursor());
        assertEquals(List.of(new Stack(STONE, 8)), menu.slot(36).contents());
        assertTrue(menu.slot(9).stack().empty());
    }

    @Test
    void queuedWheelTransfersRefreshSelectedIndicesAfterReturningRemainders() {
        Item dirt = new Item("dirt", "Dirt", 64, true, false);
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(dirt, 5), new Stack(STONE, 11))), empty(9)));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        EntryId entry = session.state().entries().stream().filter(e -> e.item().equals(STONE)).findFirst().orElseThrow().id();
        session.inputHeld(true);
        session.interact(entry, -1, Gesture.WHEEL_DOWN, false, true);
        session.interact(entry, -1, Gesture.WHEEL_DOWN, false, true);
        session.interact(entry, -1, Gesture.WHEEL_DOWN, false, true);
        finish(session);
        assertEquals(new Stack(STONE, 3), menu.slot(9).stack());
        assertTrue(menu.read().cursor().empty());
        assertEquals(8, session.state().entries().stream().filter(e -> e.item().equals(STONE)).findFirst().orElseThrow().count());
        assertEquals(5, session.state().entries().stream().filter(e -> e.item().equals(dirt)).findFirst().orElseThrow().count());
    }

    @Test
    void groupedActionsStayInTheChosenBagAndPartialDepositsKeepCursorLeftovers() {
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 10))),
                bag(37, Region.HOTBAR, 1, List.of(new Stack(STONE, 60)))));
        BundlePanelSession session = new BundlePanelSession(menu, View.GROUPED, true);
        EntryId entry = session.state().entries().stream().filter(e -> e.bag() == 36).findFirst().orElseThrow().id();
        session.interact(entry, 36, Gesture.LEFT, false, false);
        finish(session);
        assertEquals(new Stack(STONE, 10), menu.read().cursor());
        session.interact(null, 37, Gesture.LEFT, false, false);
        finish(session);
        assertEquals(new Stack(STONE, 6), menu.read().cursor());
        assertEquals(64, menu.slot(37).contents().getFirst().count());
        assertTrue(menu.slot(36).contents().isEmpty());
    }

    @Test
    void closedMenuAndChangedCursorCancelAllQueuedActions() {
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 11))), empty(9)));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        EntryId entry = session.state().entries().getFirst().id();
        session.interact(entry, -1, Gesture.WHEEL_DOWN, false, true);
        session.interact(entry, -1, Gesture.WHEEL_DOWN, false, true);
        menu.snapshot = new Snapshot(2, 0, false, Stack.EMPTY, menu.snapshot.slots());
        finish(session);
        assertEquals(Failure.CHANGED, session.outcome().failure());
        assertEquals(11, menu.slot(36).contents().getFirst().count());
        assertTrue(menu.slot(9).stack().empty());
        assertTrue(session.state().bags().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"stone,63,64,NONE", "stone,64,64,CURSOR", "dirt,1,1,CURSOR"})
    void singleExtractionRequiresACompatibleCursorWithRoom(String carried, int before, int after, Failure failure) {
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 11))), empty(9)));
        menu.cursor(new Stack(carried.equals("stone") ? STONE : new Item("dirt", "Dirt", 64, true, false), before));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(session.state().entries().getFirst().id(), -1, Gesture.LEFT, true, false);
        finish(session);
        assertEquals(after, menu.read().cursor().count());
        assertEquals(failure, session.outcome().failure());
        assertTrue(menu.slot(9).stack().empty());
        assertEquals(failure == Failure.NONE ? 10 : 11, menu.slot(36).contents().getFirst().count());
    }

    @Test
    void singleCursorDepositWithoutMainSpaceIsUnavailableButWholeDepositStillWorks() {
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of()),
                new Slot(9, Region.HOTBAR, 1, Stack.EMPTY, List.of(), 64, -1)));
        menu.cursor(new Stack(STONE, 11));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(null, -1, Gesture.RIGHT, false, false);
        assertEquals(Failure.TEMPORARY_SLOT, session.outcome().failure());
        assertEquals(11, menu.read().cursor().count());
        session.interact(null, -1, Gesture.LEFT, false, false);
        finish(session);
        assertEquals(11, menu.slot(36).contents().getFirst().count());
        assertTrue(menu.read().cursor().empty());
        assertTrue(menu.slot(9).stack().empty());
    }

    @Test
    void vanillaWeightsLimitDepositsAndNestedContentsAreNotTraversed() {
        Item pearl = new Item("pearl", "Pearl", 16, true, false);
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 60)))));
        menu.cursor(new Stack(pearl, 3));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(null, -1, Gesture.LEFT, false, false);
        finish(session);
        assertEquals(new Stack(pearl, 2), menu.read().cursor());
        assertEquals(1, session.outcome().moved());
        assertEquals(60, menu.slot(36).contents().stream().filter(s -> s.item().equals(STONE)).findFirst().orElseThrow().count());
        Stack storedBag = bundleStack(BAG, List.of(new Stack(STONE, 10)));
        Menu nested = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(storedBag))));
        BundlePanelSession nestedSession = new BundlePanelSession(nested, View.MERGED, true);
        assertEquals(1, nestedSession.state().entries().size());
        assertTrue(nestedSession.state().entries().getFirst().item().bundle());
    }

    @Test
    void cancellationReleasesTheSingleTemporarySlotWithoutMovingUnrelatedSlots() {
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 11))), empty(9),
                new Slot(10, Region.MAIN, 10, new Stack(STONE, 30), List.of(), 64, -1)));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(session.state().entries().getFirst().id(), -1, Gesture.RIGHT, false, false);
        session.tick();
        session.tick(); // One requested item has been placed into the temporary slot.
        session.cancel();
        assertFalse(session.busy());
        assertTrue(menu.slot(9).stack().empty());
        assertEquals(new Stack(STONE, 30), menu.slot(10).stack());
        assertEquals(11, menu.read().cursor().count() + menu.slot(36).contents().stream().mapToInt(Stack::count).sum());
    }

    @ParameterizedTest
    @CsvSource({"WHEEL_DOWN,0", "WHEEL_UP,11"})
    void cancellationReturnsMainInventoryTransferItemsToTheirSourceWhenTheMenuIsStillValid(Gesture gesture, int mainCount) {
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 11))),
                new Slot(9, Region.MAIN, 9, mainCount == 0 ? Stack.EMPTY : new Stack(STONE, mainCount), List.of(), 64, -1)));
        Snapshot initial = menu.read();
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(session.state().entries().getFirst().id(), -1, gesture, false, true);
        session.tick();
        session.cancel();
        assertEquals(initial, menu.read());
        assertFalse(session.busy());
        assertEquals(Failure.CHANGED, session.outcome().failure());
    }

    @Test
    void correctionReplacingTheSourceBagDoesNotReceiveTransientItems() {
        Item dirt = new Item("dirt", "Dirt", 64, true, false);
        Menu menu = new Menu(List.of(bag(36, Region.HOTBAR, 0, List.of(new Stack(STONE, 11))), empty(9)));
        BundlePanelSession session = new BundlePanelSession(menu, View.MERGED, true);
        session.interact(session.state().entries().getFirst().id(), -1, Gesture.RIGHT, false, false);
        session.tick();
        session.tick();
        Slot replacement = bag(36, Region.HOTBAR, 0, List.of(new Stack(dirt, 5)));
        menu.replace(replacement, menu.read().cursor());
        finish(session);
        assertEquals(replacement, menu.slot(36));
        assertEquals(new Stack(STONE, 11), menu.read().cursor());
        assertTrue(menu.slot(9).stack().empty());
        assertEquals(Failure.CHANGED, session.outcome().failure());
    }

    @ParameterizedTest
    @CsvSource({"640,232,false,0,LEFT", "800,389,true,0,RIGHT", "800,389,true,120,SWITCH",
            "320,72,false,0,SWITCH", "640,232,false,120,LEFT"})
    void layoutUsesActualAvailableAreaAndAvoidsRecipeBookAndEffects(int width, int left,
            boolean recipeBook, int effectsWidth, Placement expected) {
        BundlePanelSession session = new BundlePanelSession(new Menu(List.of()), View.MERGED, true);
        Layout result = session.layout(new LayoutInput(width, 300, left, 60, 176, recipeBook, effectsWidth));
        assertEquals(expected, result.placement());
        assertTrue(result.x() >= 0 && result.x() + result.width() <= width);
        assertTrue(result.y() >= 0 && result.y() + result.height() <= 300);
    }

    @Test
    void centersInventoryWithItsLeftSidebarBeforeFallingBackToASwitch() {
        BundlePanelSession session = new BundlePanelSession(new Menu(List.of()), View.MERGED, true);
        LayoutInput original = new LayoutInput(427, 240, 125, 37, 176, false, 0);
        int left = session.inventoryLeft(original, true);
        assertEquals(202, left);
        Layout panel = session.layout(new LayoutInput(427, 240, left, 37, 176, false, 0));
        assertEquals(Placement.LEFT, panel.placement());
        assertEquals(48, panel.x());
        assertEquals(37, panel.y());
        assertEquals(166, panel.height());
        assertEquals(125, session.inventoryLeft(original, false));
        LayoutInput book = new LayoutInput(427, 240, 202, 37, 176, true, 0);
        assertEquals(202, session.inventoryLeft(book, true));
        Layout fallback = session.layout(book);
        assertEquals(Placement.SWITCH, fallback.placement());
        assertEquals(202, fallback.x());
        assertEquals(176, fallback.width());
    }

    private static final class Menu implements MenuAdapter {
        private Snapshot snapshot;
        Menu(List<Slot> slots) { snapshot = new Snapshot(1, 0, true, Stack.EMPTY, slots); }
        void cursor(Stack value) { snapshot = new Snapshot(snapshot.menu(), snapshot.revision(), snapshot.valid(), value, snapshot.slots()); }
        @Override public Snapshot read() { return snapshot; }
        @Override public void select(int slot, int index) {
            Slot s = slot(slot);
            replace(new Slot(s.id(), s.region(), s.position(), s.stack(), s.contents(), s.limit(), index), snapshot.cursor());
        }
        @Override public void click(int id, int button) {
            Slot s = slot(id);
            Stack cursor = snapshot.cursor();
            List<Stack> contents = new ArrayList<>(s.contents());
            Stack stored = s.stack();
            if (!cursor.empty() && cursor.item().bundle() && button == 0 && !stored.empty()) {
                List<Stack> carriedContents = new ArrayList<>(contents(cursor));
                int available = 64 - carriedContents.stream().mapToInt(BundlePanelSessionTest::weight).sum();
                int amount = Math.min(stored.count(), available / weight(new Stack(stored.item(), 1)));
                if (amount > 0) {
                    carriedContents.addFirst(new Stack(stored.item(), amount));
                    stored = stack(stored.item(), stored.count() - amount);
                    cursor = bundleStack(cursor.item(), carriedContents);
                }
            } else if (s.bag() && cursor.empty() && button == 1) {
                if (!contents.isEmpty()) cursor = contents.remove(s.selected() < 0 ? 0 : s.selected());
            } else if (s.bag() && !cursor.empty() && button == 0 && !cursor.item().bundle()) {
                int amount = Math.min(cursor.count(), capacity(id, cursor.item()));
                if (amount > 0) {
                    int old = 0;
                    for (int i = 0; i < contents.size(); i++) {
                        if (compatible(contents.get(i), cursor)) { old = contents.remove(i).count(); break; }
                    }
                    contents.addFirst(new Stack(cursor.item(), old + amount));
                    cursor = stack(cursor.item(), cursor.count() - amount);
                }
            } else if (cursor.empty()) {
                int amount = button == 0 ? stored.count() : (stored.count() + 1) / 2;
                cursor = stack(stored.item(), amount);
                stored = stack(stored.item(), stored.count() - amount);
            } else if (stored.empty() || compatible(stored, cursor)) {
                int amount = Math.min(button == 0 ? cursor.count() : 1,
                        Math.min(s.limit(), cursor.item().maxStack()) - stored.count());
                stored = new Stack(cursor.item(), stored.count() + amount);
                cursor = stack(cursor.item(), cursor.count() - amount);
            } else {
                Stack swap = stored; stored = cursor; cursor = swap;
            }
            if (!stored.empty() && stored.item().bundle()) {
                if (s.bag() && stored.item().key().equals(s.stack().item().key())) stored = bundleStack(stored.item(), contents);
                else contents = new ArrayList<>(contents(stored));
            } else contents.clear();
            replace(new Slot(s.id(), s.region(), s.position(), stored, contents, s.limit(), -1), cursor);
        }
        @Override public int capacity(int slot, Item item) {
            int weight = slot(slot).contents().stream().mapToInt(BundlePanelSessionTest::weight).sum();
            return (64 - weight) / weight(new Stack(item, 1));
        }
        private Slot slot(int id) { return snapshot.slots().stream().filter(s -> s.id() == id).findFirst().orElseThrow(); }
        private void replace(Slot slot, Stack cursor) {
            List<Slot> slots = new ArrayList<>(snapshot.slots());
            slots.replaceAll(s -> s.id() == slot.id() ? slot : s);
            snapshot = new Snapshot(snapshot.menu(), snapshot.revision(), snapshot.valid(), cursor, slots);
        }
        private static Stack stack(Item item, int count) { return count == 0 ? Stack.EMPTY : new Stack(item, count); }
        private static boolean compatible(Stack a, Stack b) {
            return !a.empty() && !b.empty() && a.item().stackable() && a.item().key().equals(b.item().key());
        }
    }
}
