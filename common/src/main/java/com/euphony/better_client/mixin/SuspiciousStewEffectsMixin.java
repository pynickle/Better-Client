package com.euphony.better_client.mixin;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static com.euphony.better_client.BetterClient.config;

@Mixin(SuspiciousStewEffects.class)
public class SuspiciousStewEffectsMixin {
    @Inject(method = "addToTooltip", at = @At(value = "HEAD"), cancellable = true)
    private void isCreative(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter, CallbackInfo ci) {
        if (!config.enableSuspiciousStewTooltip) ci.cancel();
    }
}
