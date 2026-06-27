package com.euphony.better_client.mixin;

import com.euphony.better_client.service.NewItemMarker;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
    @Inject(method = "extractItemHotbar", at = @At("HEAD"))
    private void better_client$clearEmptyHotbarMarkers(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            NewItemMarker.clearEmptySlots(player.getInventory());
        }
    }

    @Inject(
            method = "extractSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
                    shift = Shift.AFTER))
    private void better_client$renderHotbarNewItemMarker(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            DeltaTracker deltaTracker,
            Player player,
            ItemStack itemStack,
            int seed,
            CallbackInfo ci) {
        Inventory inventory = player.getInventory();
        if (seed < 1 || seed > Inventory.getSelectionSize()) {
            return;
        }

        int slot = seed - 1;
        NewItemMarker.clearOnSelect(inventory);
        NewItemMarker.renderHotbarMarker(graphics, slot, itemStack, x, y);
    }
}
