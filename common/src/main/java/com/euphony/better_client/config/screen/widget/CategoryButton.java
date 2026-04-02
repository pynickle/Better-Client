package com.euphony.better_client.config.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class CategoryButton extends Button {
    private final Item icon;
    private final Component text;

    private static final int MARGIN = 8;

    public CategoryButton(int x, int y, int w, int h, Component text, Item icon, OnPress onClick) {
        super(x, y, w, h, text, onClick, DEFAULT_NARRATION);
        this.icon = icon;
        this.text = text;
    }

    private static final WidgetSprites SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/button"),
            Identifier.withDefaultNamespace("widget/button_disabled"),
            Identifier.withDefaultNamespace("widget/button_highlighted"));

    @Override
    public void extractContents(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SPRITES.get(this.active, this.isHoveredOrFocused()),
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight());

        Minecraft mc = Minecraft.getInstance();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("textures/item/" + icon.toString().split(":")[1] + ".png"),
                this.getX() + 5, this.getY() + 2, 0f, 0f , 16, 16, 16, 16);
        // guiGraphics.fakeItem(icon, getX() + 5, getY() + 2);

        guiGraphics.centeredText(
                mc.font, text, getX() + width / 2 + MARGIN, getY() + (height - 8) / 2, 0xffffffff);
    }
}
