package com.euphony.better_client.screen.widget;

import com.euphony.better_client.utils.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class FastTradingButton extends ImageButton {
    public static final WidgetSprites FAST_TRADING_SPRITES = new WidgetSprites(Utils.prefix("fast_trading"), Utils.prefix("fast_trading_disabled"), Utils.prefix("fast_trading_highlighted"));

    public FastTradingButton(int i, int j, int k, int l, OnPress onPress) {
        super(i, j, k, l, FAST_TRADING_SPRITES, onPress);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        ResourceLocation resourceLocation = this.sprites.get(this.isActive(), this.isHovered());
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, this.getX(), this.getY(), this.width, this.height);
    }
}
