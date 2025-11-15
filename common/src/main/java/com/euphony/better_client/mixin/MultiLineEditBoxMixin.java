package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IMultiLineEditBox;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiLineEditBox.class)
public abstract class MultiLineEditBoxMixin extends AbstractTextAreaWidget implements IMultiLineEditBox {
    @Unique
    private static boolean better_client$isModified;

    public MultiLineEditBoxMixin(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    @ModifyReturnValue(method = "charTyped", at = @At("RETURN"))
    private boolean charTypedInject(boolean original) {
        if (original) {
            better_client$isModified = true;
        }
        return original;
    }

    @Override
    public void better_client$setIsModified(boolean isModified) {
        better_client$isModified = isModified;
    }

    @Override
    public boolean better_client$getIsModified() {
        return better_client$isModified;
    }
}
