package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IMultiLineEditBox;
import com.euphony.better_client.config.BetterClientConfig;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookEditScreen.class)
public class BookEditScreenMixin extends Screen {
    @Shadow private MultiLineEditBox page;
    @Unique
    private boolean better_client$isModified;

    protected BookEditScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "appendPageToBook", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;getNumPages()I",
            shift =  At.Shift.AFTER))
    private void appendPageToBookInject(CallbackInfo ci) {
        this.better_client$isModified = true;
    }

    @Override
    public void onClose() {
        if(!BetterClientConfig.HANDLER.instance().enableBookSaveConfirmation) {
            super.onClose();
            return;
        }

        if (this.better_client$isModified || ((IMultiLineEditBox) this.page).better_client$getIsModified()) {
            this.minecraft.setScreen(new ConfirmScreen((response) -> {
                if(response) {
                    this.better_client$isModified = false;
                    ((IMultiLineEditBox) this.page).better_client$setIsModified(false);
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
