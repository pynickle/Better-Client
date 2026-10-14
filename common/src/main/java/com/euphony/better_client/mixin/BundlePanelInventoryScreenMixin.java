package com.euphony.better_client.mixin;

import com.euphony.better_client.client.events.BundlePanelEvent;
import com.euphony.better_client.screen.BundlePanelScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class BundlePanelInventoryScreenMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void better_client$renderReplacement(GuiGraphicsExtractor graphics, int x, int y, float delta, CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen) (Object) this;
        BundlePanelScreen panel = BundlePanelEvent.panel(screen);
        if (panel != null && panel.replacesInventory()) {
            screen.extractBackground(graphics, x, y, delta);
            panel.render(graphics, x, y, delta);
            graphics.nextStratum();
            screen.extractCarriedItem(graphics, x, y);
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void better_client$renderSidebar(GuiGraphicsExtractor graphics, int x, int y, float delta, CallbackInfo ci) {
        BundlePanelScreen panel = BundlePanelEvent.panel((InventoryScreen) (Object) this);
        if (panel != null) {
            panel.render(graphics, x, y, delta);
            graphics.nextStratum();
            ((InventoryScreen) (Object) this).extractCarriedItem(graphics, x, y);
        }
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void better_client$tickPanel(CallbackInfo ci) {
        BundlePanelScreen panel = BundlePanelEvent.panel((InventoryScreen) (Object) this);
        if (panel != null) panel.tick();
    }
}
