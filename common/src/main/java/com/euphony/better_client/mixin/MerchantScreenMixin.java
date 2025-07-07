package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IMerchantMenu;
import com.euphony.better_client.screen.widget.FastTradingButton;
import com.euphony.better_client.utils.ItemUtils;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
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

import static com.euphony.better_client.BetterClient.config;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {
    @Shadow private int shopItem;

    @Unique
    private int better_client$tradeState = 0;

    @Unique
    private FastTradingButton better_client$fastTradingButton;

    @Unique
    private final Map<Integer, Component> better_client$tradeDescriptionCache = new HashMap<>();

    @Unique
    private int better_client$lastCachedShopItem = -1;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void addSpeedTradeButton(MerchantMenu merchantMenu, Inventory inventory, Component component, CallbackInfo ci) {
        if(!config.enableFastTrading) return;

        this.better_client$fastTradingButton = new FastTradingButton( this.leftPos + 350,this.topPos + 77, 18, 18, (button) -> {
            this.menu.setSelectionHint(this.shopItem);
            this.minecraft.getConnection().send(new ServerboundSelectTradePacket(this.shopItem));
            better_client$tradeState = 1;
        });
        this.addRenderableWidget(
                better_client$fastTradingButton
        );
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        this.better_client$fastTradingButton.active = false;

        if(!config.enableFastTrading) return;

        Inventory inventory = this.minecraft.player.getInventory();
        MerchantOffer merchantOffer = menu.getOffers().get(this.shopItem);

        if(merchantOffer.getUses() == merchantOffer.getMaxUses()) {
            this.better_client$fastTradingButton.active = false;
        } else {
            ItemStack costA = merchantOffer.getCostA();
            ItemStack costB = merchantOffer.getCostB();

            ItemStack sellItem = merchantOffer.getResult();

            ItemStack slotA = menu.slots.get(0).getItem();
            ItemStack slotB = menu.slots.get(1).getItem();

            int costACount = better_client$getItemTotalCountWithSlots(inventory, costA, slotA, slotB);
            boolean hasEnoughCostA = costACount >= costA.getCount() && costA.getCount() > 0;

            if (!better_client$isInactiveAlt(sellItem)) {
                if (!merchantOffer.getCostB().isEmpty()) {
                    int costBCount = better_client$getItemTotalCountWithSlots(inventory, costB, slotA, slotB);
                    boolean hasEnoughCostB = costBCount >= costB.getCount() && costB.getCount() > 0;

                    this.better_client$fastTradingButton.active = hasEnoughCostA && hasEnoughCostB;
                } else {
                    this.better_client$fastTradingButton.active = hasEnoughCostA;
                }
            }
        }

        Component tradeDescription;
        if (this.better_client$lastCachedShopItem == this.shopItem && this.better_client$tradeDescriptionCache.containsKey(this.shopItem)) {
            tradeDescription = this.better_client$tradeDescriptionCache.get(this.shopItem);
        } else {
            tradeDescription = better_client$generateTradeDescription(merchantOffer);
            this.better_client$tradeDescriptionCache.put(this.shopItem, tradeDescription);
            this.better_client$lastCachedShopItem = this.shopItem;
        }

        this.better_client$fastTradingButton.setTooltip(Tooltip.create(tradeDescription));

        if (better_client$tradeState > 0) {
            MerchantOffer offer = menu.getOffers().get(this.shopItem);

            switch (better_client$tradeState) {
                case 1:
                    ItemStack item = offer.getItemCostA().itemStack();
                    if (!item.isEmpty()){
                        better_client$fillSlots(item);
                    }
                    offer.getItemCostB().ifPresent(cost -> {
                        better_client$fillSlots(cost.itemStack());
                    });
                    better_client$tradeState = 2;
                    break;

                case 2:
                    if (!this.menu.getSlot(2).getItem().isEmpty()) {

                        slotClicked(this.menu.getSlot(2), 2, 0, ClickType.QUICK_MOVE);
                        better_client$tradeState = 3;
                    } else {
                        better_client$tradeState = 0;
                    }
                    break;

                case 3:
                    if (offer.getUses() < offer.getMaxUses() && inventory.getFreeSlot() != -1) {
                        better_client$tradeState = 1;
                    } else {
                        better_client$tradeState = 4;
                    }
                    break;
                case 4:
                    slotClicked(this.menu.getSlot(0), 0, 0, ClickType.QUICK_MOVE);
                    slotClicked(this.menu.getSlot(1), 1, 0, ClickType.QUICK_MOVE);
                    better_client$tradeState = 0;
            }
        }
    }

    @Unique
    private void better_client$fillSlots(ItemStack item) {
        int count = 0;
        for (int i = 3; i < 39; i++) {
            ItemStack invstack = this.menu.getSlot(i).getItem();
            if (!ItemStack.isSameItemSameComponents(item, invstack)) {
                continue;
            }

            count += invstack.getCount();

            slotClicked(this.menu.getSlot(i), i, i, ClickType.PICKUP);
            slotClicked(this.menu.getSlot(0), 0, 0, ClickType.PICKUP);

            if (count > this.menu.getSlot(i).getItem().getMaxStackSize()) { // items still on the cursor
                slotClicked(this.menu.getSlot(i), i, i, ClickType.PICKUP);
                break;
            } else if (count == this.menu.getSlot(i).getItem().getMaxStackSize()) {
                break;
            }
        }
    }

    @Unique
    private Component better_client$generateTradeDescription(MerchantOffer offer) {
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        ItemStack sellItem = offer.getResult();

        MutableComponent component = Component.empty();

        if(better_client$isInactiveAlt(sellItem)) {
            component.append(Component.translatable("message.better_client.fast_trading.alt").withStyle(ChatFormatting.RED));
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
    private int better_client$getItemTotalCountWithSlots(Inventory inventory, ItemStack itemStack, ItemStack... slots) {
        int count = ItemUtils.getItemTotalCount(inventory, itemStack);
        for (ItemStack slot : slots) {
            if (slot != null && !slot.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, slot)) {
                count += slot.getCount();
            }
        }
        return count;
    }

    @Unique
    private boolean better_client$isInactiveAlt(ItemStack sellItem) {
        return config.enableAltKey
                && !Screen.hasAltDown()
                && (sellItem.isDamageableItem() || !sellItem.isStackable());
    }

    @Shadow protected abstract void renderButtonArrows(GuiGraphics guiGraphics, MerchantOffer merchantOffer, int i, int j);

    @Shadow public abstract boolean mouseClicked(double d, double e, int i);

    @Shadow private int scrollOff;

    public MerchantScreenMixin(MerchantMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;renderButtonArrows(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/trading/MerchantOffer;II)V"))
    private void render(MerchantScreen instance, GuiGraphics guiGraphics, MerchantOffer merchantOffer, int k, int p) {
        if(!config.enableDisplayRemainingSales) {
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

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffers;size()I", ordinal = 1))
    private void disableOptionIfOutOfLevelRange(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci, @Local MerchantScreen.TradeOfferButton tradeOfferButton) {
        tradeOfferButton.active = ((IMerchantMenu) this.menu).better_client$shouldAllowTrade(tradeOfferButton.getIndex() + scrollOff);
    }
}
