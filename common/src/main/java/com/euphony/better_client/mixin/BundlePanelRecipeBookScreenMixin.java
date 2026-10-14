package com.euphony.better_client.mixin;

import com.euphony.better_client.client.events.BundlePanelEvent;
import com.euphony.better_client.screen.BundlePanelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Catch panel input before the recipe book forwards it to vanilla slots. */
@Mixin(value = AbstractRecipeBookScreen.class, priority = 2000)
public abstract class BundlePanelRecipeBookScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void better_client$clickPanel(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        BundlePanelScreen panel = BundlePanelEvent.panel((Screen) (Object) this);
        if (panel != null && panel.mouseClicked(event, doubleClick)) cir.setReturnValue(true);
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void better_client$dragPanel(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        BundlePanelScreen panel = BundlePanelEvent.panel((Screen) (Object) this);
        if (panel != null && panel.mouseDragged(event)) cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void better_client$keyPanel(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        BundlePanelScreen panel = BundlePanelEvent.panel((Screen) (Object) this);
        if (panel != null && panel.keyPressed(event)) cir.setReturnValue(true);
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void better_client$typePanel(CharacterEvent event, CallbackInfoReturnable<Boolean> cir) {
        BundlePanelScreen panel = BundlePanelEvent.panel((Screen) (Object) this);
        if (panel != null && panel.charTyped(event)) cir.setReturnValue(true);
    }

    @Inject(method = "isInputCaptured", at = @At("RETURN"), cancellable = true)
    private void better_client$capturePanel(CallbackInfoReturnable<Boolean> cir) {
        BundlePanelScreen panel = BundlePanelEvent.panel((Screen) (Object) this);
        if (panel != null && panel.capturesInput()) cir.setReturnValue(true);
    }
}
