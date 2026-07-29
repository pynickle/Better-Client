package com.euphony.better_client.mixin;

import com.euphony.better_client.service.NewItemMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Final
    @Shadow
    public NonNullList<Slot> slots;

    @Inject(method = "doClick", at = @At("HEAD"))
    private void better_client$clearClickedMarker(
            int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (player != Minecraft.getInstance().player || slotIndex < 0 || slotIndex >= this.slots.size()) {
            return;
        }

        NewItemMarker.clearSlot(this.slots.get(slotIndex));
    }
}
