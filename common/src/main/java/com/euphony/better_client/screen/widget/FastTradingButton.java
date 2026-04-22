package com.euphony.better_client.screen.widget;

import com.euphony.better_client.utils.Utils;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;

public class FastTradingButton extends ImageButton {
    public static final WidgetSprites FAST_TRADING_SPRITES = new WidgetSprites(
            Utils.prefix("fast_trading"),
            Utils.prefix("fast_trading_disabled"),
            Utils.prefix("fast_trading_highlighted"));

    public FastTradingButton(int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, FAST_TRADING_SPRITES, onPress);
    }
}
