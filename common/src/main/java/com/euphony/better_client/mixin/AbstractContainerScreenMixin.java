package com.euphony.better_client.mixin;

import com.euphony.better_client.service.NewItemMarker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
    @Shadow
    public Slot hoveredSlot;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(
            method = "extractTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"))
    private void better_client$clearHoveredMarker(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        NewItemMarker.clearOnHover(this.hoveredSlot);
    }

    @Inject(method = "extractSlot", at = @At("TAIL"))
    private void better_client$renderNewItemMarker(
            GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (NewItemMarker.isPlayerInventorySlot(slot)) {
            NewItemMarker.renderSlotMarker(graphics, slot);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void better_client$clearMarkersOnClose(CallbackInfo ci) {
        NewItemMarker.clearOnClose();
    }
}
