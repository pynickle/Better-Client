package com.euphony.better_client.mixin;

import com.euphony.better_client.client.events.ClickThroughEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    /** Allows dye application to signs while sneaking. */
    @Inject(method = "isSecondaryUseActive", at = @At("HEAD"), cancellable = true)
    private void better_client$allowDyeingSignWhileSneaking(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LocalPlayer && ClickThroughEvent.consumeDyeOnSign()) {
            cir.setReturnValue(false);
        }
    }
}
