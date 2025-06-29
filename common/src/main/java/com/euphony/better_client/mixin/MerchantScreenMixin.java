package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IMerchantMenu;
import com.euphony.better_client.config.BetterClientConfig;
import com.euphony.better_client.screen.widget.FastTradingButton;
import com.euphony.better_client.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {
    @Shadow private int shopItem;

    @Unique
    private FastTradingButton fastTradingButton;

    @Unique
    private final Map<Integer, Component> enc_vanilla$tradeDescriptionCache = new HashMap<>();

    @Unique
    private int enc_vanilla$lastCachedShopItem = -1;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void addSpeedTradeButton(MerchantMenu merchantMenu, Inventory inventory, Component component, CallbackInfo ci) {
        if(!BetterClientConfig.HANDLER.instance().enableFastTrading) return;

        this.fastTradingButton = new FastTradingButton( this.leftPos + 350,this.topPos + 77, 18, 18, (button) -> {
            this.minecraft.gameMode.handleInventoryButtonClick(
                    this.menu.containerId, this.shopItem
            );
            ((IMerchantMenu) menu).enc_vanilla$buttonClick(inventory.player, this.shopItem);
        });
        this.addRenderableWidget(
                fastTradingButton
        );
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        this.fastTradingButton.active = false;

        if(!BetterClientConfig.HANDLER.instance().enableFastTrading) return;

        Inventory inventory = this.minecraft.player.getInventory();
        MerchantOffer merchantOffer = menu.getOffers().get(this.shopItem);

        if(merchantOffer.getUses() == merchantOffer.getMaxUses()) {
            this.fastTradingButton.active = false;
        } else {
            ItemStack costA = merchantOffer.getCostA();
            ItemStack costB = merchantOffer.getCostB();

            ItemStack sellItem = merchantOffer.getResult();

            ItemStack slotA = menu.slots.get(0).getItem();
            ItemStack slotB = menu.slots.get(1).getItem();

            int costACount = enc_vanilla$getItemTotalCountWithSlots(inventory, costA, slotA, slotB);
            boolean hasEnoughCostA = costACount >= costA.getCount() && costA.getCount() > 0;

            if (!enc_vanilla$isInactiveAlt(sellItem)) {
                if (!merchantOffer.getCostB().isEmpty()) {
                    int costBCount = enc_vanilla$getItemTotalCountWithSlots(inventory, costB, slotA, slotB);
                    boolean hasEnoughCostB = costBCount >= costB.getCount() && costB.getCount() > 0;

                    this.fastTradingButton.active = hasEnoughCostA && hasEnoughCostB;
                } else {
                    this.fastTradingButton.active = hasEnoughCostA;
                }
            }
        }

        Component tradeDescription;
        if (this.enc_vanilla$lastCachedShopItem == this.shopItem && this.enc_vanilla$tradeDescriptionCache.containsKey(this.shopItem)) {
            tradeDescription = this.enc_vanilla$tradeDescriptionCache.get(this.shopItem);
        } else {
            tradeDescription = enc_vanilla$generateTradeDescription(inventory.player, merchantOffer);
            this.enc_vanilla$tradeDescriptionCache.put(this.shopItem, tradeDescription);
            this.enc_vanilla$lastCachedShopItem = this.shopItem;
        }

        this.fastTradingButton.setTooltip(Tooltip.create(tradeDescription));
    }

    @Unique
    private Component enc_vanilla$generateTradeDescription(Player player, MerchantOffer offer) {
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        ItemStack sellItem = offer.getResult();

        MutableComponent component = Component.empty();

        if(enc_vanilla$isInactiveAlt(sellItem)) {
            component.append(Component.translatable("message.enc_vanilla.fast_trading.alt").withStyle(ChatFormatting.RED));
        }

        component.append(ItemUtils.getWrappedItemName(costA));
        if (!costB.isEmpty()) {
            component.append(ItemUtils.createTooltip(" + "));
            component.append(ItemUtils.getWrappedItemName(costB));
        }

        component.append(ItemUtils.createTooltip(" -> "));
        component.append(ItemUtils.getWrappedItemName(sellItem));

        return component;
    }

    @Unique
    private int enc_vanilla$getItemTotalCountWithSlots(Inventory inventory, ItemStack itemStack, ItemStack... slots) {
        int count = ItemUtils.getItemTotalCount(inventory, itemStack);
        for (ItemStack slot : slots) {
            if (slot != null && !slot.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, slot)) {
                count += slot.getCount();
            }
        }
        return count;
    }

    @Unique
    private boolean enc_vanilla$isInactiveAlt(ItemStack sellItem) {
        return BetterClientConfig.HANDLER.instance().enableAltKey
                && !Screen.hasAltDown()
                && (sellItem.isDamageableItem() || !sellItem.isStackable());
    }

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
