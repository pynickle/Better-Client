package com.euphony.better_client.service;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** The common boundary for the panel's projection and vanilla menu transactions. */
public final class BundlePanelSession {
    public enum View { MERGED, GROUPED }
    public enum Region { MAIN, HOTBAR, OFFHAND, OTHER }
    public enum Gesture { LEFT, RIGHT, WHEEL_UP, WHEEL_DOWN }
    public enum Failure { NONE, BUSY, CURSOR, NO_SPACE, TEMPORARY_SLOT, CHANGED, UNAVAILABLE, EXPLICIT_TARGET }
    public record Outcome(int moved, Failure failure) {}
    public enum Placement { LEFT, RIGHT, SWITCH }
    public record LayoutInput(int screenWidth, int screenHeight, int inventoryLeft, int inventoryTop,
                              int inventoryWidth, boolean recipeBook, int effectsWidth) {}
    public record Layout(Placement placement, int x, int y, int width, int height) {
        public boolean contains(double px, double py) { return px >= x && py >= y && px < x + width && py < y + height; }
    }
    public record Item(Object key, String name, int maxStack, boolean stackable, boolean bundle) {}
    public record Stack(Item item, int count) {
        public static final Stack EMPTY = new Stack(null, 0);
        public boolean empty() { return count == 0; }
    }
    public record Slot(int id, Region region, int position, Stack stack, List<Stack> contents,
                       int limit, int selected) {
        public Slot { contents = List.copyOf(contents); }
        public boolean bag() { return !stack.empty() && stack.item().bundle(); }
    }
    public record Snapshot(int menu, long revision, boolean valid, Stack cursor, List<Slot> slots) {
        public Snapshot { slots = List.copyOf(slots); }
    }
    public record EntryId(Object key, int bag, int occurrence) {}
    public record Entry(EntryId id, Item item, int count, int bag) {}
    public record State(Snapshot inventory, List<Slot> bags, List<Entry> entries, View view, boolean expanded) {}
    public interface MenuAdapter {
        Snapshot read();
        void select(int slot, int index);
        void click(int slot, int button);
        int capacity(int slot, Item item);
    }

    private final MenuAdapter menu;
    private View view;
    private boolean expanded;
    private String search = "";
    private boolean pointerInGrid;
    private List<Entry> visible = List.of();
    private final Deque<Runnable> steps = new ArrayDeque<>();
    private Snapshot expected;
    private Item transferItem;
    private EntryId transferEntry;
    private int remaining;
    private int moved;
    private int temporary = -1;
    private boolean active;
    private boolean intoMain;
    private enum Mode { EXTRACT, DEPOSIT_CURSOR, DEPOSIT_MAIN }
    private Mode mode;
    private int target = -1;
    private Outcome outcome = new Outcome(0, Failure.NONE);
    private record Intent(EntryId entry, int target, Gesture gesture, boolean control, boolean shift) {}
    private final Deque<Intent> pending = new ArrayDeque<>();
    private int sourceBag = -1;
    private int nestingTarget = -1;
    private int depositSource = -1;

    public BundlePanelSession(MenuAdapter menu, View view, boolean expanded) {
        this.menu = menu;
        this.view = view == null ? View.MERGED : view;
        this.expanded = expanded;
    }

    public void view(View value) { view = value; visible = List.of(); }
    public void expanded(boolean value) { expanded = value; }
    public void search(String value) { search = value.toLowerCase(Locale.ROOT); visible = List.of(); }
    public void pointerInGrid(boolean value) { pointerInGrid = value; }
    public boolean busy() { return active || !pending.isEmpty(); }
    public Outcome outcome() { return outcome; }
    public int inventoryLeft(LayoutInput input, boolean expanded) {
        int total = 148 + 6 + input.inventoryWidth() + (input.effectsWidth() > 0 ? 34 : 0);
        if (!expanded || input.recipeBook() || total + 8 > input.screenWidth()) return input.inventoryLeft();
        return (input.screenWidth() - total) / 2 + 154;
    }
    public Layout layout(LayoutInput input) {
        int width = Math.min(148, Math.max(1, input.screenWidth() - 8));
        int height = Math.min(166, Math.max(1, input.screenHeight() - 8));
        int y = Math.clamp(input.inventoryTop(), 4, Math.max(4, input.screenHeight() - height - 4));
        int left = input.inventoryLeft() - width - 6;
        if (!input.recipeBook() && left >= 4) return new Layout(Placement.LEFT, left, y, width, height);
        int right = input.inventoryLeft() + input.inventoryWidth() + 6 + input.effectsWidth();
        if (right + width <= input.screenWidth() - 4) return new Layout(Placement.RIGHT, right, y, width, height);
        width = Math.min(input.inventoryWidth(), Math.max(1, input.screenWidth() - 8));
        return new Layout(Placement.SWITCH, Math.clamp(input.inventoryLeft(), 0, Math.max(0, input.screenWidth() - width)), y, width, height);
    }

    public void interact(EntryId entryId, int targetBag, Gesture gesture, boolean control, boolean shift) {
        if (active) {
            if (shift && (gesture == Gesture.WHEEL_UP || gesture == Gesture.WHEEL_DOWN)
                    && (mode == Mode.DEPOSIT_MAIN || mode == Mode.EXTRACT && intoMain)) {
                pending.addLast(new Intent(entryId, targetBag, gesture, control, shift));
            }
            return;
        }
        outcome = new Outcome(0, Failure.NONE);
        boolean wheel = gesture == Gesture.WHEEL_UP || gesture == Gesture.WHEEL_DOWN;
        if (wheel && !shift) return;
        if (gesture == Gesture.RIGHT && (control || shift)) return;
        Snapshot snapshot = menu.read();
        Entry entry = state().entries().stream().filter(e -> e.id().equals(entryId) && e.count() > 0).findFirst().orElse(null);
        target = view == View.GROUPED ? (targetBag >= 0 ? targetBag : entry == null ? -1 : entry.bag()) : -1;
        if (view == View.GROUPED && target < 0) { outcome = new Outcome(0, Failure.UNAVAILABLE); return; }
        if (!snapshot.cursor().empty() && !shift && !control && !wheel) {
            mode = Mode.DEPOSIT_CURSOR;
            transferItem = snapshot.cursor().item();
            if (transferItem.bundle() && target < 0) { outcome = new Outcome(0, Failure.EXPLICIT_TARGET); return; }
            begin(snapshot, gesture == Gesture.RIGHT ? 1 : snapshot.cursor().count());
            if (remaining < snapshot.cursor().count()) {
                temporary = emptyMain(snapshot);
                if (temporary < 0) complete(Failure.TEMPORARY_SLOT);
            }
            return;
        }
        if (entry == null) { outcome = new Outcome(0, Failure.UNAVAILABLE); return; }
        if (wheel && gesture == Gesture.WHEEL_UP) {
            if (!snapshot.cursor().empty()) { outcome = new Outcome(0, Failure.CURSOR); return; }
            if (entry.item().bundle()) { outcome = new Outcome(0, Failure.EXPLICIT_TARGET); return; }
            mode = Mode.DEPOSIT_MAIN;
            transferItem = entry.item();
            begin(snapshot, control ? transferItem.maxStack() : 1);
            return;
        }
        mode = Mode.EXTRACT;
        intoMain = shift;
        if (!snapshot.cursor().empty() && (shift || !control || gesture != Gesture.LEFT)) {
            outcome = new Outcome(0, Failure.CURSOR); return;
        }
        if (!snapshot.cursor().empty() && (!compatible(snapshot.cursor(), entry.item())
                || snapshot.cursor().count() >= entry.item().maxStack())) {
            outcome = new Outcome(0, Failure.CURSOR); return;
        }
        transferItem = entry.item();
        transferEntry = entry.id();
        remaining = Math.min(entry.count(), entry.item().maxStack());
        if (wheel) remaining = control ? remaining : 1;
        else if (gesture == Gesture.LEFT && control && shift) remaining = entry.count();
        else if (gesture == Gesture.LEFT && control) remaining = 1;
        else if (gesture == Gesture.RIGHT) remaining = (remaining + 1) / 2;
        begin(snapshot, remaining);
        Source source = source(snapshot);
        if (source == null) { complete(Failure.UNAVAILABLE); return; }
        if (intoMain) {
            remaining = Math.min(remaining, mainCapacity(snapshot, transferItem));
            if (remaining == 0) { complete(Failure.NO_SPACE); return; }
        } else if (!snapshot.cursor().empty() || source.stack().count() != remaining) {
            temporary = emptyMain(snapshot);
            if (temporary < 0) { complete(Failure.TEMPORARY_SLOT); return; }
            if (!snapshot.cursor().empty()) {
                int temp = temporary;
                steps.add(() -> click(temp, 0));
            }
        }
    }

    private void begin(Snapshot snapshot, int amount) {
        moved = 0;
        remaining = amount;
        expected = snapshot;
        sourceBag = -1;
        nestingTarget = -1;
        depositSource = -1;
        active = snapshot.valid();
        outcome = new Outcome(0, active ? Failure.NONE : Failure.CHANGED);
    }

    /** Execute at most one ordinary menu click per tick; prediction becomes the next guard. */
    public void tick() {
        if (!active && !pending.isEmpty()) {
            Intent intent = pending.removeFirst();
            interact(intent.entry(), intent.target(), intent.gesture(), intent.control(), intent.shift());
        }
        if (!active) return;
        Snapshot current = menu.read();
        if (!current.valid() || !current.equals(expected)) { complete(Failure.CHANGED); return; }
        if (steps.isEmpty()) {
            if (remaining == 0) {
                if (temporary >= 0 && !slot(current, temporary).stack().empty()) {
                    int temp = temporary;
                    steps.add(() -> click(temp, 0));
                    steps.add(() -> complete(Failure.NONE));
                } else complete(Failure.NONE);
            } else if (mode == Mode.EXTRACT) planExtraction(current);
            else planDeposit(current);
        }
        if (active && !steps.isEmpty()) {
            steps.removeFirst().run();
            if (active) expected = menu.read();
        }
    }

    private void planDeposit(Snapshot snapshot) {
        Slot source = mode == Mode.DEPOSIT_MAIN ? snapshot.slots().stream()
                .filter(s -> s.region() == Region.MAIN && !s.stack().empty()
                        && s.stack().item().key().equals(transferItem.key()) && !s.stack().item().bundle())
                .min(Comparator.comparingInt(Slot::position)).orElse(null) : null;
        if (mode == Mode.DEPOSIT_MAIN && source == null) { complete(Failure.NONE); return; }
        if (source != null) depositSource = source.id();
        Slot bag = bags(snapshot).stream().filter(s -> target < 0 || s.id() == target)
                .filter(s -> menu.capacity(s.id(), transferItem) > 0)
                .sorted(Comparator.comparing((Slot s) -> s.contents().stream().noneMatch(c -> compatible(c, transferItem)))
                        .thenComparing(Slot::region).thenComparingInt(Slot::position))
                .findFirst().orElse(null);
        if (bag == null) { complete(Failure.NO_SPACE); return; }
        Stack carried = mode == Mode.DEPOSIT_MAIN ? source.stack() : snapshot.cursor();
        int amount = Math.min(remaining, Math.min(carried.count(), menu.capacity(bag.id(), transferItem)));
        if (amount <= 0) { complete(Failure.UNAVAILABLE); return; }
        if (transferItem.bundle()) {
            planNesting(snapshot, bag);
            return;
        }
        if (mode == Mode.DEPOSIT_MAIN) steps.add(() -> click(source.id(), 0));
        if (amount < carried.count() && (mode == Mode.DEPOSIT_MAIN || temporary >= 0)) {
            int staging = mode == Mode.DEPOSIT_MAIN ? source.id() : temporary;
            for (int i = 0; i < carried.count() - amount; i++) steps.add(() -> click(staging, 1));
        }
        steps.add(() -> {
            int before = menu.read().cursor().count();
            if (before == 0) { complete(Failure.CHANGED); return; }
            click(bag.id(), 0);
            int actual = before - menu.read().cursor().count();
            if (actual != amount) { complete(Failure.CHANGED); return; }
            moved += actual;
            remaining -= actual;
            outcome = new Outcome(moved, Failure.NONE);
        });
    }

    private void planNesting(Snapshot snapshot, Slot bag) {
        nestingTarget = bag.id();
        temporary = emptyMain(snapshot);
        if (temporary < 0) { complete(Failure.TEMPORARY_SLOT); return; }
        int temp = temporary;
        steps.add(() -> click(temp, 0));
        steps.add(() -> click(bag.id(), 0));
        // The TARGET must be carried: vanilla gives the carried bundle's insertion hook priority.
        steps.add(() -> click(temp, 0));
        steps.add(() -> {
            if (!slot(menu.read(), temp).stack().empty()) { complete(Failure.CHANGED); return; }
            click(bag.id(), 0);
            if (!menu.read().cursor().empty()) { complete(Failure.CHANGED); return; }
            moved++;
            remaining = 0;
            outcome = new Outcome(moved, Failure.NONE);
        });
    }

    private void planExtraction(Snapshot snapshot) {
        Source source = source(snapshot);
        if (source == null) { remaining = 0; return; }
        int amount = Math.min(remaining, source.stack().count());
        sourceBag = source.bag();
        Slot destination = intoMain ? destination(snapshot, transferItem) : null;
        if (intoMain && destination == null) { remaining = 0; return; }
        if (intoMain) amount = Math.min(amount, room(destination, transferItem));
        int delivered = amount;
        steps.add(() -> {
            Source live = source(menu.read());
            if (live == null || live.bag() != source.bag()) { complete(Failure.CHANGED); return; }
            menu.select(live.bag(), live.index());
            click(live.bag(), 1);
            Stack cursor = menu.read().cursor();
            if (cursor.empty() || !cursor.item().key().equals(transferItem.key())
                    || cursor.count() != live.stack().count()) complete(Failure.CHANGED);
        });
        if (temporary >= 0 || intoMain) {
            int temp = intoMain ? destination.id() : temporary;
            if (delivered == source.stack().count()) steps.add(() -> click(temp, 0));
            else for (int i = 0; i < delivered; i++) steps.add(() -> click(temp, 1));
            steps.add(() -> { if (!menu.read().cursor().empty()) click(source.bag(), 0); });
            steps.add(() -> { if (!menu.read().cursor().empty()) complete(Failure.CHANGED); });
        }
        steps.add(() -> { moved += delivered; remaining -= delivered; outcome = new Outcome(moved, Failure.NONE); });
    }

    private record Source(int bag, int index, Stack stack) {}
    private Source source(Snapshot snapshot) {
        for (Slot bag : bags(snapshot)) {
            if (transferEntry.bag() >= 0 && transferEntry.bag() != bag.id()) continue;
            int occurrence = 0;
            for (int i = 0; i < bag.contents().size(); i++) {
                Stack stack = bag.contents().get(i);
                if (!stack.item().key().equals(transferItem.key())) continue;
                occurrence++;
                if (!transferItem.stackable() && occurrence != transferEntry.occurrence()) continue;
                return new Source(bag.id(), i, stack);
            }
        }
        return null;
    }

    private static Slot slot(Snapshot snapshot, int id) {
        return snapshot.slots().stream().filter(s -> s.id() == id).findFirst().orElseThrow();
    }
    private static int emptyMain(Snapshot snapshot) {
        return snapshot.slots().stream().filter(s -> s.region() == Region.MAIN && s.stack().empty())
                .min(Comparator.comparingInt(Slot::position)).map(Slot::id).orElse(-1);
    }
    private static boolean compatible(Stack stack, Item item) {
        return !stack.empty() && item.stackable() && stack.item().stackable() && stack.item().key().equals(item.key());
    }
    private static int room(Slot slot, Item item) {
        if (!slot.stack().empty() && (!compatible(slot.stack(), item) || item.bundle())) return 0;
        return Math.max(0, Math.min(slot.limit(), item.maxStack()) - slot.stack().count());
    }
    private static int mainCapacity(Snapshot snapshot, Item item) {
        return snapshot.slots().stream().filter(s -> s.region() == Region.MAIN).mapToInt(s -> room(s, item)).sum();
    }
    private static Slot destination(Snapshot snapshot, Item item) {
        return snapshot.slots().stream().filter(s -> s.region() == Region.MAIN && room(s, item) > 0)
                .sorted(Comparator.comparing((Slot s) -> s.stack().empty()).thenComparingInt(Slot::position))
                .findFirst().orElse(null);
    }
    private void click(int id, int button) { menu.click(id, button); }
    private void complete(Failure failure) {
        if (failure != Failure.NONE) {
            pending.clear();
            recoverTransient();
        }
        outcome = new Outcome(moved, failure);
        active = false;
        steps.clear();
        temporary = -1;
    }

    public void cancel() { pending.clear(); if (active) complete(Failure.CHANGED); }

    private void recoverTransient() {
        Snapshot current = menu.read();
        if (expected == null || !current.valid() || current.menu() != expected.menu()) return;
        if (mode == Mode.EXTRACT && intoMain && sourceBag >= 0 && !current.cursor().empty()
                && !current.cursor().item().bundle() && current.cursor().equals(expected.cursor())
                && slot(current, sourceBag).equals(slot(expected, sourceBag)) && slot(current, sourceBag).bag()
                && menu.capacity(sourceBag, current.cursor().item()) >= current.cursor().count()) {
            click(sourceBag, 0);
        } else if (mode == Mode.DEPOSIT_MAIN && depositSource >= 0 && !current.cursor().empty()
                && current.cursor().equals(expected.cursor()) && slot(current, depositSource).stack().equals(slot(expected, depositSource).stack())
                && room(slot(current, depositSource), current.cursor().item()) >= current.cursor().count()) {
            click(depositSource, 0);
        }
        if (temporary < 0) return;
        current = menu.read();
        Slot temp = slot(current, temporary);
        if (!temp.stack().equals(slot(expected, temporary).stack())) return;
        if (nestingTarget >= 0 && !current.cursor().empty() && current.cursor().item().bundle()
                && current.cursor().equals(expected.cursor())
                && slot(current, nestingTarget).stack().empty() && slot(expected, nestingTarget).stack().empty()) {
            click(nestingTarget, 0);
        } else if (sourceBag >= 0 && !current.cursor().empty() && !current.cursor().item().bundle()
                && current.cursor().equals(expected.cursor()) && slot(current, sourceBag).bag()
                && slot(current, sourceBag).equals(slot(expected, sourceBag))
                && menu.capacity(sourceBag, current.cursor().item()) >= current.cursor().count() && !temp.stack().empty()) {
            click(sourceBag, 0);
        }
        current = menu.read();
        if (current.valid() && !current.cursor().empty() && current.cursor().equals(expected.cursor()) && compatible(slot(current, temporary).stack(), current.cursor().item())
                && room(slot(current, temporary), current.cursor().item()) >= current.cursor().count()) {
            click(temporary, 0);
            current = menu.read();
        }
        if (current.valid() && current.cursor().empty() && !slot(current, temporary).stack().empty()) click(temporary, 0);
    }

    public State state() {
        Snapshot snapshot = menu.read();
        List<Slot> bags = bags(snapshot);
        Map<EntryId, Entry> entries = new LinkedHashMap<>();
        for (Slot bag : bags) {
            Map<Object, Integer> occurrences = new LinkedHashMap<>();
            for (Stack stack : bag.contents()) {
                Item item = stack.item();
                if (!item.name().toLowerCase(Locale.ROOT).contains(search)) continue;
                int source = view == View.MERGED && item.stackable() ? -1 : bag.id();
                int occurrence = item.stackable() ? 0 : occurrences.merge(item.key(), 1, Integer::sum);
                EntryId id = new EntryId(item.key(), source, occurrence);
                Entry previous = entries.get(id);
                entries.put(id, new Entry(id, item, stack.count() + (previous == null ? 0 : previous.count()), source));
            }
        }
        List<Entry> fresh = new ArrayList<>(entries.values());
        if (view == View.MERGED) fresh.sort(Comparator.comparing((Entry e) -> e.item().name())
                .thenComparing(e -> e.item().key().toString()).thenComparingInt(e -> e.id().bag())
                .thenComparingInt(e -> e.id().occurrence()));
        if (pointerInGrid && !visible.isEmpty()) {
            List<Entry> stable = new ArrayList<>();
            for (Entry old : visible) {
                Entry current = entries.remove(old.id());
                stable.add(current == null ? new Entry(old.id(), old.item(), 0, old.bag()) : current);
            }
            for (Entry entry : fresh) if (entries.containsKey(entry.id())) stable.add(entry);
            visible = List.copyOf(stable);
        } else visible = List.copyOf(fresh);
        return new State(snapshot, bags, visible, view, expanded);
    }

    private static List<Slot> bags(Snapshot snapshot) {
        if (!snapshot.valid()) return List.of();
        return snapshot.slots().stream().filter(s -> s.region() != Region.OTHER && s.bag())
                .sorted(Comparator.comparing(Slot::region).thenComparingInt(Slot::position)).toList();
    }
}
