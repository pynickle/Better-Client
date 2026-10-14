package com.euphony.better_client.screen;

import com.euphony.better_client.config.Config;
import com.euphony.better_client.mixin.accessor.BundlePanelContainerScreenAccessor;
import com.euphony.better_client.mixin.accessor.BundlePanelRecipeBookAccessor;
import com.euphony.better_client.service.BundlePanelSession;
import com.euphony.better_client.service.MinecraftBundlePanelMenu;
import com.euphony.better_client.utils.mc.KeyUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.euphony.better_client.BetterClient.config;
import static com.euphony.better_client.service.BundlePanelSession.*;

/** Native inventory textures and virtual cells beside the survival inventory. */
public final class BundlePanelScreen {
    private static final int ROW_HEIGHT = 18;
    private static final Identifier INVENTORY = Identifier.withDefaultNamespace("textures/gui/container/inventory.png");
    private static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    private static final Identifier BUTTON_HOVER = Identifier.withDefaultNamespace("widget/button_highlighted");
    private final Minecraft minecraft = Minecraft.getInstance();
    private final InventoryScreen screen;
    private final BundlePanelSession session;
    private final EditBox search;
    private List<Row> rows = List.of();
    private Layout layout;
    private boolean replacement;
    private boolean shieldRelease;
    private boolean inGrid;
    private int offset;
    private int columns;
    private double wheel;
    private State shown;

    private record Row(Object id, Entry entry, Slot bag, int owner, boolean header) {}
    private record Padding(int owner, int position) {}

    public BundlePanelScreen(InventoryScreen screen) {
        this.screen = screen;
        session = new BundlePanelSession(new MinecraftBundlePanelMenu(screen), config.bundlePanelView, config.bundlePanelExpanded);
        search = new EditBox(minecraft.font, 0, 0, 136, 16, text("search"));
        search.setHint(text("search"));
        search.setMaxLength(128);
        search.setResponder(value -> { session.search(value); rows = List.of(); offset = 0; });
        shown = session.state();
    }

    public void tick() { session.tick(); }
    public void close() { session.cancel(); }

    private BundlePanelContainerScreenAccessor geometry() { return (BundlePanelContainerScreenAccessor) screen; }
    private net.minecraft.client.gui.screens.recipebook.RecipeBookComponent<?> book() {
        return ((BundlePanelRecipeBookAccessor) screen).better_client$recipeBook();
    }

    private boolean available() { return session.busy() || !shown.bags().isEmpty(); }

    private int effectsWidth(int left) {
        if (minecraft.player == null || minecraft.level == null) return 0;
        int available = screen.width - left - geometry().better_client$imageWidth() - 2;
        if (available < 32) return 0;
        int maxWidth = available >= 120 ? available - 7 : 32;
        int result = 0;
        for (var effect : minecraft.player.getActiveEffects()) {
            Component name = effect.getEffect().value().getDisplayName();
            if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9)
                name = name.copy().append(" ").append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
            Component duration = MobEffectUtil.formatDuration(effect, 1, minecraft.level.tickRateManager().tickrate());
            result = Math.max(result, Math.min(maxWidth, 39 + Math.max(minecraft.font.width(name), minecraft.font.width(duration))) + 7);
        }
        return result;
    }

    private void updateLayout() {
        shown = session.state();
        int base = book().updateScreenPosition(screen.width, geometry().better_client$imageWidth());
        int left = session.inventoryLeft(new LayoutInput(screen.width, screen.height, base, geometry().better_client$topPos(),
                geometry().better_client$imageWidth(), book().isVisible(), effectsWidth(base)), available() && shown.expanded());
        int old = geometry().better_client$leftPos();
        if (old != left) {
            geometry().better_client$leftPos(left);
            for (var child : screen.children())
                if (child instanceof ImageButton button && button.getX() == old + 104 && button.getY() == screen.height / 2 - 22)
                    button.setX(left + 104);
        }
        layout = session.layout(new LayoutInput(screen.width, screen.height, left, geometry().better_client$topPos(),
                geometry().better_client$imageWidth(), book().isVisible(), effectsWidth(left)));
        if (layout.placement() != Placement.SWITCH || book().isVisible()) replacement = false;
        int nextColumns = Math.max(1, (layout.width() - 14) / ROW_HEIGHT);
        if (columns != nextColumns) { columns = nextColumns; rows = List.of(); offset = 0; }
        search.setX(layout.x() + 7);
        search.setY(layout.y() + 39);
        search.setWidth(Math.max(1, layout.width() - 14));
    }

    public boolean replacesInventory() {
        updateLayout();
        return available() && shown.expanded() && replacement;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        updateLayout();
        if (!available()) { search.setFocused(false); return; }
        graphics.nextStratum();
        if (!shown.expanded() || layout.placement() == Placement.SWITCH && !replacement) {
            Layout toggle = toggleButton();
            button(graphics, toggle, Component.empty(), mouseX, mouseY);
            graphics.item(new ItemStack(Items.BUNDLE), toggle.x() + 1, toggle.y() + 1);
            if (toggle.contains(mouseX, mouseY)) graphics.setTooltipForNextFrame(text("open"), mouseX, mouseY);
            return;
        }
        inventoryFrame(graphics);
        graphics.text(minecraft.font, text("title"), layout.x() + 7, layout.y() + 6, 0xFF404040, false);
        button(graphics, collapseButton(), Component.literal("×"), mouseX, mouseY);
        if (collapseButton().contains(mouseX, mouseY)) graphics.setTooltipForNextFrame(text(replacement ? "back" : "collapse_hint"), mouseX, mouseY);
        button(graphics, viewButton(), viewLabel(shown.view()), mouseX, mouseY);
        search.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
        if (shown.view() == View.MERGED) button(graphics, depositButton(), text("deposit"), mouseX, mouseY);
        Layout grid = grid();
        inGrid = grid.contains(mouseX, mouseY);
        session.pointerInGrid(inGrid);
        shown = session.state();
        updateRows(shown);
        int visibleRows = Math.max(1, grid.height() / ROW_HEIGHT);
        int rowCount = (rows.size() + columns - 1) / columns;
        offset = Math.clamp(offset, 0, Math.max(0, rowCount - visibleRows));
        graphics.enableScissor(grid.x(), grid.y(), grid.x() + grid.width(), grid.y() + grid.height());
        for (int r = 0; r < visibleRows; r++) for (int c = 0; c < columns; c++) {
            int index = (offset + r) * columns + c;
            Row row = index < rows.size() ? rows.get(index) : null;
            int x = grid.x() + c * ROW_HEIGHT, y = grid.y() + r * ROW_HEIGHT;
            if (row != null && row.header()) {
                if (row.bag() != null) renderBag(graphics, row.bag(), grid.x(), y, mouseX, mouseY);
                continue;
            }
            slotBackground(graphics, x, y);
            if (row != null && row.entry() != null) renderItem(graphics, row.entry(), x, y, mouseX, mouseY);
        }
        graphics.disableScissor();
        int footer = layout.y() + layout.height() - 12;
        if (session.busy()) graphics.text(minecraft.font, text("working"), layout.x() + 7, footer, 0xFF404040, false);
        else if (session.outcome().failure() != Failure.NONE) graphics.text(minecraft.font,
                clip(failure(session.outcome().failure()).getString(), layout.width() - 14), layout.x() + 7, footer, 0xFF8B2020, false);
        else if (shown.entries().isEmpty()) graphics.text(minecraft.font, text(search.getValue().isEmpty() ? "empty" : "no_results"), layout.x() + 7, footer, 0xFF404040, false);
        else if (rowCount > visibleRows) graphics.text(minecraft.font, text("scroll", offset + 1, rowCount), layout.x() + 7, footer, 0xFF404040, false);
        if (layout.contains(mouseX, mouseY) && mouseY >= footer && session.outcome().failure() != Failure.NONE)
            graphics.setTooltipForNextFrame(failure(session.outcome().failure()), mouseX, mouseY);
    }

    private void renderItem(GuiGraphicsExtractor graphics, Entry entry, int x, int y, int mx, int my) {
        ItemStack item = MinecraftBundlePanelMenu.sample(entry.item());
        graphics.item(item, x + 1, y + 1);
        if (entry.count() == 0) {
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xA0808080);
            graphics.text(minecraft.font, "0", x + 17 - minecraft.font.width("0"), y + 9, 0xFFA0A0A0);
        } else graphics.itemDecorations(minecraft.font, item, x + 1, y + 1, Integer.toString(entry.count()));
        if (new Layout(layout.placement(), x, y, ROW_HEIGHT, ROW_HEIGHT).contains(mx, my)) {
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0x80FFFFFF);
            List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(minecraft, item));
            tooltip.add(text("extract_hint")); tooltip.add(text("main_hint"));
            tooltip.add(text("wheel_hint")); tooltip.add(text("deposit_hint"));
            if (session.outcome().failure() != Failure.NONE) tooltip.add(failure(session.outcome().failure()));
            graphics.setComponentTooltipForNextFrame(minecraft.font, tooltip, mx, my);
        }
    }

    private void renderBag(GuiGraphicsExtractor graphics, Slot bag, int x, int y, int mx, int my) {
        ItemStack item = MinecraftBundlePanelMenu.sample(bag.stack().item());
        slotBackground(graphics, x, y);
        graphics.item(item, x + 1, y + 1);
        graphics.itemDecorations(minecraft.font, item, x + 1, y + 1);
        String capacity = Math.round(BundleItem.getFullnessDisplay(item) * 100) + "%";
        Layout target = new Layout(layout.placement(), x + ROW_HEIGHT, y, grid().width() - ROW_HEIGHT, ROW_HEIGHT);
        button(graphics, target, Component.literal(clip(bag.stack().item().name(), target.width() - 34) + " " + capacity), mx, my);
        if (new Layout(layout.placement(), x, y, grid().width(), ROW_HEIGHT).contains(mx, my))
            graphics.setComponentTooltipForNextFrame(minecraft.font,
                    List.of(item.getHoverName(), text("capacity", capacity), text("deposit_hint"), text("nesting_hint")), mx, my);
    }

    private void updateRows(State state) {
        if (inGrid && !rows.isEmpty()) {
            List<Row> stable = new ArrayList<>();
            for (Row old : rows) {
                Entry entry = old.entry() == null ? null : state.entries().stream().filter(e -> e.id().equals(old.entry().id()))
                        .findFirst().orElse(new Entry(old.entry().id(), old.entry().item(), 0, old.owner()));
                Slot bag = old.bag() == null ? null : state.bags().stream().filter(s -> s.id() == old.owner()).findFirst().orElse(old.bag());
                stable.add(new Row(old.id(), entry, bag, old.owner(), old.header()));
            }
            rows = List.copyOf(stable);
            return;
        }
        List<Row> fresh = new ArrayList<>();
        if (state.view() == View.MERGED) {
            for (Entry entry : state.entries()) fresh.add(new Row(entry.id(), entry, null, -1, false));
        } else for (Slot bag : state.bags()) {
            fresh.add(new Row(bag.id(), null, bag, bag.id(), true));
            for (int i = 1; i < columns; i++) fresh.add(new Row(new Padding(bag.id(), fresh.size()), null, null, bag.id(), true));
            int start = fresh.size();
            for (Entry entry : state.entries()) if (entry.bag() == bag.id()) fresh.add(new Row(entry.id(), entry, null, bag.id(), false));
            if (fresh.size() == start) for (int i = 0; i < columns; i++) fresh.add(new Row(new Padding(bag.id(), fresh.size()), null, null, bag.id(), false));
            while (fresh.size() % columns != 0) fresh.add(new Row(new Padding(bag.id(), fresh.size()), null, null, bag.id(), false));
        }
        rows = List.copyOf(fresh);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        updateLayout();
        if (!available()) return false;
        if (session.busy()) {
            if (replacement || shown.expanded() && layout.placement() != Placement.SWITCH && layout.contains(event.x(), event.y())) {
                shieldRelease = true; return true;
            }
            session.cancel();
            search.setFocused(false);
            return false;
        }
        State state = shown;
        if (!state.expanded() || layout.placement() == Placement.SWITCH && !replacement) {
            if (!toggleButton().contains(event.x(), event.y())) return false;
            shieldRelease = true;
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                session.expanded(true);
                config.bundlePanelExpanded = true;
                Config.save();
                if (book().isVisible()) { book().toggleVisibility(); screen.resize(screen.width, screen.height); }
                updateLayout();
                replacement = layout.placement() == Placement.SWITCH;
            }
            return true;
        }
        if (!replacement && !layout.contains(event.x(), event.y())) { search.setFocused(false); return false; }
        if (replacement && !layout.contains(event.x(), event.y())) return true;
        shieldRelease = true;
        if (collapseButton().contains(event.x(), event.y()) && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            replacement = false;
            session.expanded(false); config.bundlePanelExpanded = false; Config.save();
            search.setFocused(false);
        } else if (viewButton().contains(event.x(), event.y()) && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            View view = state.view() == View.MERGED ? View.GROUPED : View.MERGED;
            session.view(view); config.bundlePanelView = view; Config.save(); rows = List.of(); offset = 0;
        } else if (search.isMouseOver(event.x(), event.y())) {
            search.setFocused(true); search.mouseClicked(event, doubleClick);
        } else {
            search.setFocused(false);
            Gesture gesture = event.button() == InputConstants.MOUSE_BUTTON_LEFT ? Gesture.LEFT
                    : event.button() == InputConstants.MOUSE_BUTTON_RIGHT ? Gesture.RIGHT : null;
            if (gesture != null) {
                Row row = rowAt(event.x(), event.y());
                if (row != null) session.interact(row.entry() == null ? null : row.entry().id(),
                        row.owner(), gesture, event.hasControlDown(), event.hasShiftDown());
                else if (state.view() == View.MERGED && (grid().contains(event.x(), event.y()) || depositButton().contains(event.x(), event.y())))
                    session.interact(null, -1, gesture, event.hasControlDown(), event.hasShiftDown());
            }
        }
        return true;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = shieldRelease || replacement || available() && layout != null
                && layout.placement() != Placement.SWITCH && layout.contains(event.x(), event.y()) && shown.expanded();
        shieldRelease = false;
        return handled;
    }
    public boolean mouseDragged(MouseButtonEvent event) { return shieldRelease || replacement; }

    public boolean mouseScrolled(double x, double y, double amount) {
        updateLayout();
        if (!available()) return false;
        if (!shown.expanded() || layout.placement() == Placement.SWITCH && !replacement) return toggleButton().contains(x, y);
        if (!replacement && !layout.contains(x, y)) return false;
        if (!grid().contains(x, y)) return true;
        if (!KeyUtils.hasShiftDown()) { offset -= (int) Math.signum(amount); wheel = 0; return true; }
        if (wheel * amount < 0) wheel = 0;
        wheel += amount;
        Row row = rowAt(x, y);
        while (Math.abs(wheel) >= 1) {
            int direction = wheel > 0 ? 1 : -1;
            wheel -= direction;
            if (row != null && row.entry() != null) session.interact(row.entry().id(), row.owner(),
                    direction > 0 ? Gesture.WHEEL_UP : Gesture.WHEEL_DOWN, KeyUtils.hasControlDown(), true);
        }
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            if (search.isFocused()) { search.setFocused(false); return true; }
            if (replacement) { session.cancel(); replacement = false; return true; }
            session.cancel(); return false;
        }
        if (session.busy() && !replacement) session.cancel();
        if (search.isFocused()) { search.keyPressed(event); return true; }
        return replacement;
    }
    public boolean charTyped(CharacterEvent event) { return search.isFocused() && search.charTyped(event) || replacement; }
    public boolean capturesInput() { return search.isFocused() || replacement || session.busy(); }

    private Row rowAt(double x, double y) {
        Layout grid = grid();
        if (!grid.contains(x, y)) return null;
        int r = offset + (int) (y - grid.y()) / ROW_HEIGHT;
        int index = r * columns + (int) (x - grid.x()) / ROW_HEIGHT;
        if (index >= rows.size()) return null;
        Row row = rows.get(index);
        return row.header() ? rows.get(r * columns) : row;
    }
    private Layout grid() {
        int header = shown.view() == View.MERGED ? 80 : 58;
        int height = Math.max(ROW_HEIGHT, (layout.height() - header - 14) / ROW_HEIGHT * ROW_HEIGHT);
        return new Layout(layout.placement(), layout.x() + (layout.width() - columns * ROW_HEIGHT) / 2,
                layout.y() + header, columns * ROW_HEIGHT, height);
    }
    private Layout toggleButton() { return new Layout(layout.placement(), geometry().better_client$leftPos() + geometry().better_client$imageWidth() - 23, geometry().better_client$topPos() + 5, 18, 18); }
    private Layout collapseButton() { return new Layout(layout.placement(), layout.x() + layout.width() - 20, layout.y() + 4, 13, 13); }
    private Layout viewButton() { return new Layout(layout.placement(), layout.x() + 7, layout.y() + 20, layout.width() - 14, 16); }
    private Layout depositButton() { return new Layout(layout.placement(), layout.x() + 7, layout.y() + 58, layout.width() - 14, 18); }
    private void button(GuiGraphicsExtractor graphics, Layout rect, Component label, int mx, int my) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, rect.contains(mx, my) ? BUTTON_HOVER : BUTTON,
                rect.x(), rect.y(), rect.width(), rect.height());
        graphics.centeredText(minecraft.font, label, rect.x() + rect.width() / 2, rect.y() + (rect.height() - 8) / 2, 0xFFFFFFFF);
        if (rect.equals(depositButton()) && rect.contains(mx, my)) graphics.setTooltipForNextFrame(text("deposit_hint"), mx, my);
    }
    private void slotBackground(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x, y, 7, 83, ROW_HEIGHT, ROW_HEIGHT, 256, 256);
    }
    private void inventoryFrame(GuiGraphicsExtractor graphics) {
        int x = layout.x(), y = layout.y(), w = layout.width(), h = layout.height();
        graphics.fill(x + 4, y + 4, x + w - 4, y + h - 4, 0xFFC6C6C6);
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x, y, 0, 0, 4, 4, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x + w - 4, y, 172, 0, 4, 4, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x, y + h - 4, 0, 162, 4, 4, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x + w - 4, y + h - 4, 172, 162, 4, 4, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x + 4, y, 4, 0, w - 8, 4, 1, 4, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x + 4, y + h - 4, 4, 162, w - 8, 4, 1, 4, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x, y + 4, 0, 4, 4, h - 8, 4, 1, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x + w - 4, y + 4, 172, 4, 4, h - 8, 4, 1, 256, 256);
    }

    private String clip(String value, int width) { return minecraft.font.plainSubstrByWidth(value, Math.max(1, width)); }
    private static Component text(String key, Object... args) { return Component.translatable("text.better_client.bundle_panel." + key, args); }
    private static Component viewLabel(View view) { return text("view." + view.name().toLowerCase(Locale.ROOT)); }
    private static Component failure(Failure failure) { return text("failure." + failure.name().toLowerCase(Locale.ROOT)); }
}
