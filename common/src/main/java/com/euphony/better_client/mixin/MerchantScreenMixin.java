package com.euphony.better_client.mixin;

import com.euphony.better_client.config.BetterClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {
    @Shadow protected abstract void renderButtonArrows(GuiGraphics guiGraphics, MerchantOffer merchantOffer, int i, int j);

    public MerchantScreenMixin(MerchantMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;renderButtonArrows(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/trading/MerchantOffer;II)V"))
    private void render(MerchantScreen instance, GuiGraphics guiGraphics, MerchantOffer merchantOffer, int k, int p) {
        if(!BetterClientConfig.HANDLER.instance().enableDisplayRemainingSales) {
            renderButtonArrows(guiGraphics, merchantOffer, k, p);
        } else {
            renderButtonArrows(guiGraphics, merchantOffer, k, p - 1);

            Matrix3x2fStack matrix3d = guiGraphics.pose();
            matrix3d.pushMatrix();
            matrix3d.translate(k + 61, p + 11);
            matrix3d.scale(0.6F, 0.6F);
            guiGraphics.drawString(this.font, String.valueOf(merchantOffer.getMaxUses() - merchantOffer.getUses()), 0, 0, 0xFFFFFFFF, false);
            matrix3d.popMatrix();
        }
    }
}
