package com.euphony.better_client.mixin;

import com.euphony.better_client.service.NewItemMarker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin {
    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"))
    private void better_client$beginInventoryAddition(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        NewItemMarker.beginInventoryAddition();
    }

    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
    private void better_client$endInventoryAddition(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        NewItemMarker.endInventoryAddition();
    }

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"))
    private void better_client$beginSlottedInventoryAddition(
            int slot, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        NewItemMarker.beginInventoryAddition();
    }

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
    private void better_client$endSlottedInventoryAddition(
            int slot, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        NewItemMarker.endInventoryAddition();
    }

    @Inject(method = "addResource(ILnet/minecraft/world/item/ItemStack;)I", at = @At("TAIL"))
    private void better_client$markAddedStack(int slot, ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        NewItemMarker.markInventoryAddition(slot);
    }
}
