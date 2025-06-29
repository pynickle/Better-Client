package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IMerchantMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin extends AbstractContainerMenu implements IMerchantMenu {
    @Shadow public abstract void setSelectionHint(int i);

    @Shadow public abstract void tryMoveItems(int i);

    @Shadow public abstract MerchantOffers getOffers();

    @Shadow public abstract void removed(Player arg);

    @Shadow @Final private Merchant trader;

    @Shadow @Final private MerchantContainer tradeContainer;

    protected MerchantMenuMixin(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Override
    public void enc_vanilla$buttonClick(Player player, int i) {
        setSelectionHint(i);
        while(true) {
            tryMoveItems(i);
            ItemStack stack = slots.get(2).safeTake(getOffers().get(i).getResult().getCount(), getOffers().get(i).getResult().getCount(), player);
            if(stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(this.tradeContainer.removeItem(0, 64));
                player.getInventory().placeItemBackInInventory(this.tradeContainer.removeItem(1, 64));
                break;
            }
            while (!stack.isEmpty()) {
                stack = slots.get(2).safeTake(getOffers().get(i).getResult().getCount(), getOffers().get(i).getResult().getCount(), player);
                player.getInventory().add(stack);
            }
        }

        super.clickMenuButton(player, i);
    }

    @Override
    public boolean clickMenuButton(Player player, int i) {
        enc_vanilla$buttonClick(player, i);
        return true;
    }

}
