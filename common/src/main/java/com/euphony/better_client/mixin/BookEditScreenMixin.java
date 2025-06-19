package com.euphony.better_client.mixin;

import com.euphony.better_client.config.BetterClientConfig;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BookEditScreen.class)
public class BookEditScreenMixin extends Screen {
    @Shadow private boolean isModified;

    protected BookEditScreenMixin(Component component) {
        super(component);
    }

    @Override
    public void onClose() {
        if(!BetterClientConfig.HANDLER.instance().enableBookSaveConfirmation) {
            super.onClose();
            return;
        }

        if (this.isModified) {
            this.minecraft.setScreen(new ConfirmScreen((response) -> {
                if(response) {
                    this.minecraft.setScreen(null);
                } else {
                    this.minecraft.setScreen(this);
                }
            }, Component.translatable("message.better_client.book_save.title"), Component.translatable("message.better_client.book_save.question")));
        } else {
            super.onClose();
        }
    }
}
