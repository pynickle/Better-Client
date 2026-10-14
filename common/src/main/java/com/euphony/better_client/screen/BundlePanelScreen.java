package com.euphony.better_client.screen;

import com.euphony.better_client.config.Config;
import com.euphony.better_client.mixin.accessor.BundlePanelContainerScreenAccessor;
import com.euphony.better_client.mixin.accessor.BundlePanelRecipeBookAccessor;
import com.euphony.better_client.service.BundlePanelSession;
import com.euphony.better_client.service.MinecraftBundlePanelMenu;
import com.euphony.better_client.utils.mc.KeyUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLMouse;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Locale;

import static com.euphony.better_client.BetterClient.config;
import static com.euphony.better_client.service.BundlePanelSession.*;

/** Native inventory textures and virtual cells beside the survival inventory. */
public final class BundlePanelScreen {
    private static final int ROW_HEIGHT = 18;
    /** Side inset shared by the title, search field, grid, and footer so their edges line up. */
    private static final int PADDING = 7;
    /** How far a vanilla-aligned count and its drop shadow reach past the bottom of its cell. */
    private static final int COUNT_OVERHANG = 2;
    /** Vertical space reserved below the grid for the footer line and its padding. */
    private static final int FOOTER_BAND = 14;
    private static final Identifier INVENTORY = Identifier.withDefaultNamespace("textures/gui/container/inventory.png");
    private static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    private static final Identifier BUTTON_HOVER = Identifier.withDefaultNamespace("widget/button_highlighted");
    private final Minecraft minecraft = Minecraft.getInstance();
    private final InventoryScreen screen;
    private final BundlePanelSession session;
    private final EditBox search;
    private List<Cell> rows = List.of();
    private Layout layout;
    private boolean replacement;
    private boolean shieldRelease;
    private int offset;
    private int columns;
    private double wheel;
    private State shown;
    private Outcome lastOutcome;
    private long failureUntil;

    public BundlePanelScreen(InventoryScreen screen) {
        this.screen = screen;
        session = new BundlePanelSession(new MinecraftBundlePanelMenu(screen), config.bundlePanelView, config.bundlePanelExpanded);
        search = new EditBox(minecraft.font, 0, 0, 136, 16, text("search"));
        search.setHint(text("search"));
        search.setMaxLength(128);
        search.setResponder(value -> { session.search(value); rows = List.of(); offset = 0; });
        shown = session.state();
    }

    public EditBox searchField() { return search; }
    public void tick() { session.inputHeld(anyInputHeld()); session.tick(); }
    public void close() { focusSearch(false); session.cancel(); }

    private boolean anyInputHeld() {
        if (SDLMouse.SDL_GetMouseState((FloatBuffer) null, (FloatBuffer) null) != 0) return true;
        ByteBuffer keys = SDLKeyboard.SDL_GetKeyboardState();
        if (keys != null) for (int key = 0; key < keys.limit(); key++) if (keys.get(key) != 0) return true;
        return false;
    }

    private void focusSearch(boolean focused) {
        if (focused) screen.setFocused(search);
        else {
            if (screen.getFocused() == search) screen.setFocused(null);
            else if (search.isFocused()) search.setFocused(false);
            search.preeditUpdated(null);
        }
    }

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
        session.inputHeld(anyInputHeld());
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
        int nextColumns = Math.max(1, contentWidth() / ROW_HEIGHT);
        if (columns != nextColumns) { columns = nextColumns; rows = List.of(); offset = 0; }
        search.setX(layout.x() + PADDING);
        search.setY(layout.y() + 20);
        search.setWidth(contentWidth());
        search.visible = available() && shown.expanded() && (layout.placement() != Placement.SWITCH || replacement);
        if (!search.visible) focusSearch(false);
    }

    public boolean replacesInventory() {
        updateLayout();
        return available() && shown.expanded() && replacement;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        updateLayout();
        if (!available()) return;
        graphics.nextStratum();
        if (!shown.expanded() || layout.placement() == Placement.SWITCH && !replacement) {
            Layout toggle = toggleButton();
            button(graphics, toggle, Component.empty(), mouseX, mouseY);
            graphics.item(new ItemStack(Items.BUNDLE), toggle.x() + 1, toggle.y() + 1);
            if (toggle.contains(mouseX, mouseY)) graphics.setTooltipForNextFrame(text("open"), mouseX, mouseY);
            return;
        }
        inventoryFrame(graphics);
        graphics.text(minecraft.font, text("title"), layout.x() + PADDING, layout.y() + 6, 0xFF404040, false);
        button(graphics, collapseButton(), Component.literal("×"), mouseX, mouseY);
        if (collapseButton().contains(mouseX, mouseY)) graphics.setTooltipForNextFrame(text(replacement ? "back" : "collapse_hint"), mouseX, mouseY);
        search.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
        Layout grid = grid();
        rows = session.cells(columns);
        int visibleRows = Math.max(1, grid.height() / ROW_HEIGHT);
        int rowCount = (rows.size() + columns - 1) / columns;
        offset = Math.clamp(offset, 0, Math.max(0, rowCount - visibleRows));
        // A count matches vanilla's baseline, so its shadow can hang COUNT_OVERHANG below the cell.
        // Extend the scissor to keep the last visible row from clipping it.
        graphics.enableScissor(grid.x(), grid.y(), grid.x() + grid.width(), grid.y() + grid.height() + COUNT_OVERHANG);
        for (int r = 0; r < visibleRows; r++) for (int c = 0; c < columns; c++) {
            int index = (offset + r) * columns + c;
            Cell row = index < rows.size() ? rows.get(index) : null;
            int x = grid.x() + c * ROW_HEIGHT, y = grid.y() + r * ROW_HEIGHT;
            if (row != null && row.header()) {
                if (row.bag() != null) renderBag(graphics, row.bag(), grid.x(), y, mouseX, mouseY);
                continue;
            }
            slotBackground(graphics, x, y);
            if (row != null && row.entry() != null) renderItem(graphics, row.entry(), x, y, mouseX, mouseY);
        }
        graphics.disableScissor();
        // Centre the footer in the band between the grid and the panel's bottom edge.
        int footer = grid.y() + grid.height()
                + Math.max(0, (layout.y() + layout.height() - grid.y() - grid.height() - minecraft.font.lineHeight) / 2);
        Outcome outcome = session.outcome();
        if (outcome != lastOutcome) {
            lastOutcome = outcome;
            failureUntil = outcome.failure() == Failure.NONE ? 0 : System.nanoTime() + 4_000_000_000L;
        }
        if (session.busy()) graphics.text(minecraft.font, text("working"), layout.x() + PADDING, footer, 0xFF404040, false);
        else if (System.nanoTime() < failureUntil) graphics.text(minecraft.font,
                clip(failure(outcome.failure()).getString(), contentWidth()), layout.x() + PADDING, footer, 0xFF8B2020, false);
        else if (shown.entries().isEmpty()) graphics.text(minecraft.font, text(search.getValue().isEmpty() ? "empty" : "no_results"), layout.x() + PADDING, footer, 0xFF404040, false);
        else if (rowCount > visibleRows) graphics.text(minecraft.font, text("scroll", offset + 1, rowCount), layout.x() + PADDING, footer, 0xFF404040, false);
    }

    private void renderItem(GuiGraphicsExtractor graphics, Entry entry, int x, int y, int mx, int my) {
        if (entry.count() == 0) return;
        ItemStack item = MinecraftBundlePanelMenu.sample(entry.item());
        graphics.item(item, x + 1, y + 1);
        graphics.itemDecorations(minecraft.font, item, x + 1, y + 1, "");
        renderCount(graphics, entry.count(), x, y);
        if (new Layout(layout.placement(), x, y, ROW_HEIGHT, ROW_HEIGHT).contains(mx, my)) {
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0x80FFFFFF);
            itemTooltip(graphics, item, mx, my);
        }
    }

    private void renderCount(GuiGraphicsExtractor graphics, int count, int x, int y) {
        String label = Integer.toString(count);
        Font font = minecraft.font;
        int width = font.width(label);
        // Pin the bottom-right corner to the box vanilla itemCount produces, which draws at
        // (iconX + 19 - 2 - width, iconY + 6 + 3) with the cell's icon origin at (x + 1, y + 1).
        // At full scale that is the right edge x + 18 and the bottom edge y + 19, so one- and
        // two-digit counts stay pixel-identical to the survival inventory. Longer totals shrink
        // away from that same corner instead of drifting to the top.
        float scale = Math.min(1.0F, (float) (ROW_HEIGHT - 4) / (width + 1));
        float right = x + 1 + 17;
        float bottom = y + 1 + 6 + 3 + font.lineHeight;
        graphics.pose().pushMatrix();
        graphics.pose().translate(right - (float) width * scale, bottom - font.lineHeight * scale);
        graphics.pose().scale(scale, scale);
        graphics.text(font, label, 0, 0, 0xFFFFFFFF, true);
        graphics.pose().popMatrix();
    }

    private void renderBag(GuiGraphicsExtractor graphics, Slot bag, int x, int y, int mx, int my) {
        ItemStack item = MinecraftBundlePanelMenu.sample(bag.stack().item());
        graphics.item(item, x + 1, y + 1);
        graphics.itemDecorations(minecraft.font, item, x + 1, y + 1);
        float fullness = BundleItem.getFullnessDisplay(item);
        String capacity = Math.round(fullness * 100) + "%";
        int capacityX = x + grid().width() - minecraft.font.width(capacity) - 2;
        int capacityColor = fullness >= 1 ? 0xFFAA2020 : fullness >= 0.75f ? 0xFF985000 : 0xFF606060;
        graphics.text(minecraft.font, Language.getInstance().getVisualOrder(minecraft.font.substrByWidth(
                item.getHoverName(), Math.max(1, capacityX - x - ROW_HEIGHT - 5))), x + ROW_HEIGHT + 2, y + 5, 0xFF303030, false);
        graphics.text(minecraft.font, capacity, capacityX, y + 5, capacityColor, false);
        if (new Layout(layout.placement(), x, y, grid().width(), ROW_HEIGHT).contains(mx, my))
            itemTooltip(graphics, item, mx, my);
    }

    private void itemTooltip(GuiGraphicsExtractor graphics, ItemStack item, int x, int y) {
        graphics.setTooltipForNextFrame(minecraft.font, Screen.getTooltipFromItem(minecraft, item),
                item.getTooltipImage(), x, y, item.get(DataComponents.TOOLTIP_STYLE), true);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        updateLayout();
        if (!available()) return false;
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
        if (!replacement && !layout.contains(event.x(), event.y())) {
            session.cancel(); focusSearch(false); return false;
        }
        if (replacement && !layout.contains(event.x(), event.y())) return true;
        shieldRelease = true;
        if (collapseButton().contains(event.x(), event.y()) && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            session.cancel();
            replacement = false;
            session.expanded(false); config.bundlePanelExpanded = false; Config.save();
            focusSearch(false);
        } else if (search.isMouseOver(event.x(), event.y())) {
            focusSearch(true); search.mouseClicked(event, doubleClick);
        } else {
            focusSearch(false);
            Gesture gesture = event.button() == InputConstants.MOUSE_BUTTON_LEFT ? Gesture.LEFT
                    : event.button() == InputConstants.MOUSE_BUTTON_RIGHT ? Gesture.RIGHT : null;
            if (gesture != null) {
                Cell row = rowAt(event.x(), event.y());
                if (row != null && !row.header() && (state.view() == View.MERGED
                        || state.bags().stream().anyMatch(b -> b.id() == row.owner())))
                    session.interact(row.entry() == null || row.entry().count() == 0 ? null : row.entry().id(),
                        row.owner(), gesture, event.hasControlDown(), event.hasShiftDown());
                else if (row == null && state.view() == View.MERGED && grid().contains(event.x(), event.y()))
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
        Cell row = rowAt(x, y);
        while (Math.abs(wheel) >= 1) {
            int direction = wheel > 0 ? 1 : -1;
            wheel -= direction;
            if (row != null && row.entry() != null && row.entry().count() > 0) session.interact(row.entry().id(), row.owner(),
                    direction > 0 ? Gesture.WHEEL_UP : Gesture.WHEEL_DOWN, KeyUtils.hasControlDown(), true);
        }
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            if (search.isFocused()) { focusSearch(false); return true; }
            if (replacement) { session.cancel(); replacement = false; return true; }
            session.cancel(); return false;
        }
        if (search.isFocused() && event.shortcutKey() == 9) return false;
        if (search.isFocused()) { search.keyPressed(event); return true; }
        return replacement;
    }
    public boolean charTyped(CharacterEvent event) { return search.isFocused() && search.charTyped(event) || replacement; }
    public boolean capturesInput() { return search.isFocused() || replacement; }

    private Cell rowAt(double x, double y) {
        Layout grid = grid();
        if (!grid.contains(x, y)) return null;
        int r = offset + (int) (y - grid.y()) / ROW_HEIGHT;
        int index = r * columns + (int) (x - grid.x()) / ROW_HEIGHT;
        if (index >= rows.size()) return null;
        Cell row = rows.get(index);
        return row.header() ? rows.get(r * columns) : row;
    }
    private Layout grid() {
        // Header holds the title and search field; the footer band holds one line plus its padding.
        int header = 40;
        int height = Math.max(ROW_HEIGHT, (layout.height() - header - FOOTER_BAND) / ROW_HEIGHT * ROW_HEIGHT);
        // Left-aligned inside the same padded content width the search field uses, so the grid's
        // outer columns line up with the field and the whole columns absorb the remainder.
        return new Layout(layout.placement(), layout.x() + PADDING,
                layout.y() + header, columns * ROW_HEIGHT, height);
    }
    /** Width shared by the search field and the width the grid's columns fill. */
    private int contentWidth() { return Math.max(ROW_HEIGHT, layout.width() - PADDING * 2); }
    private Layout toggleButton() { return new Layout(layout.placement(), geometry().better_client$leftPos() + geometry().better_client$imageWidth() - 23, geometry().better_client$topPos() + 5, 18, 18); }
    private Layout collapseButton() { return new Layout(layout.placement(), layout.x() + layout.width() - PADDING - 14, layout.y() + 4, 14, 14); }
    private void button(GuiGraphicsExtractor graphics, Layout rect, Component label, int mx, int my) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, rect.contains(mx, my) ? BUTTON_HOVER : BUTTON,
                rect.x(), rect.y(), rect.width(), rect.height());
        // Buttons are sized so this centre is an exact integer: centeredText divides the label's
        // width by two, so an odd-sized button would bias the label half a pixel off the sprite.
        graphics.centeredText(minecraft.font, label,
                rect.x() + rect.width() / 2,
                rect.y() + (rect.height() - minecraft.font.lineHeight) / 2, 0xFFFFFFFF);
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
    private static Component failure(Failure failure) { return text("failure." + failure.name().toLowerCase(Locale.ROOT)); }
}
