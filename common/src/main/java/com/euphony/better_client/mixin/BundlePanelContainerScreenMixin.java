package com.euphony.better_client.mixin;

import com.euphony.better_client.client.events.BundlePanelEvent;
import com.euphony.better_client.screen.BundlePanelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractContainerScreen.class, priority = 2000)
public abstract class BundlePanelContainerScreenMixin {
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void better_client$scrollPanel(double x, double y, double sx, double sy, CallbackInfoReturnable<Boolean> cir) {
        BundlePanelScreen panel = BundlePanelEvent.panel((Screen) (Object) this);
        if (panel != null && panel.mouseScrolled(x, y, sy)) cir.setReturnValue(true);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void better_client$releasePanel(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        BundlePanelScreen panel = BundlePanelEvent.panel((Screen) (Object) this);
        if (panel != null && panel.mouseReleased(event)) cir.setReturnValue(true);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void better_client$closePanel(CallbackInfo ci) { BundlePanelEvent.close((Screen) (Object) this); }
}
