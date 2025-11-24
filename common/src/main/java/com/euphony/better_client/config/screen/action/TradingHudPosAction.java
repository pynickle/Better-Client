package com.euphony.better_client.config.screen.action;

import com.euphony.better_client.config.screen.option.TradingHudPos;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionEventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class TradingHudPosAction implements OptionEventListener<TradingHudPos> {
    private final Option<Integer> xOffsetOption;
    private final Option<Integer> yOffsetOption;

    public TradingHudPosAction(Option<Integer> xOffsetOption, Option<Integer> yOffsetOption) {
        this.xOffsetOption = xOffsetOption;
        this.yOffsetOption = yOffsetOption;
    }

    @Override
    public void onEvent(Option<TradingHudPos> option, Event event) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen != null) {
            int xOffset = screen.width - 90;

            if (event == Event.STATE_CHANGE) {
                if (option.pendingValue() == TradingHudPos.TOP_LEFT) {
                    xOffsetOption.requestSet(0);
                    yOffsetOption.requestSet(0);
                } else if (option.pendingValue() == TradingHudPos.TOP_RIGHT) {
                    xOffsetOption.requestSet(xOffset);
                    yOffsetOption.requestSet(0);
                }
            }
        }
    }
}
