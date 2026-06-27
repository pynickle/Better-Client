package com.euphony.better_client.mixin;

import com.euphony.better_client.service.NewItemMarker;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Final
    @Shadow
    public NonNullList<Slot> slots;

    @Unique
    private ItemStack[] better_client$inventoryBeforeClick;

    @Unique
    private ItemStack[] better_client$inventoryBeforeSync;

    @Inject(method = "doClick", at = @At("HEAD"))
    private void better_client$captureInventoryBeforeClick(
            int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (slotIndex >= 0 && slotIndex < this.slots.size()) {
            NewItemMarker.clearSlot(this.slots.get(slotIndex));
        }
        this.better_client$inventoryBeforeClick = NewItemMarker.snapshot(player.getInventory());
    }

    @Inject(method = "doClick", at = @At("RETURN"))
    private void better_client$markInventoryAfterClick(
            int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        NewItemMarker.markAddedSlots(player.getInventory(), this.better_client$inventoryBeforeClick);
        this.better_client$inventoryBeforeClick = null;
    }

    @Inject(method = "setItem", at = @At("HEAD"))
    private void better_client$captureInventoryBeforeSetItem(
            int slot, int stateId, ItemStack itemStack, CallbackInfo ci) {
        if (slot >= 0 && slot < this.slots.size() && this.slots.get(slot).container instanceof Inventory inventory) {
            this.better_client$inventoryBeforeSync = NewItemMarker.snapshot(inventory);
        }
    }

    @Inject(method = "setItem", at = @At("RETURN"))
    private void better_client$markInventoryAfterSetItem(
            int slot, int stateId, ItemStack itemStack, CallbackInfo ci) {
        if (slot >= 0 && slot < this.slots.size() && this.slots.get(slot).container instanceof Inventory inventory) {
            NewItemMarker.markAddedSlots(inventory, this.better_client$inventoryBeforeSync);
        }
        this.better_client$inventoryBeforeSync = null;
    }
}
