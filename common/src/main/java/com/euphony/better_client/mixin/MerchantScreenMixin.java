package com.euphony.better_client.mixin;

import com.euphony.better_client.screen.widget.FastTradingButton;
import com.euphony.better_client.utils.ItemUtils;
import com.euphony.better_client.utils.KeyUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

import static com.euphony.better_client.BetterClient.config;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {
    @Shadow
    private int shopItem;

    @Unique
    private int better_client$tradeState = 0;

    @Unique
    private FastTradingButton better_client$fastTradingButton;

    @Unique
    private final Map<Integer, Component> better_client$tradeDescriptionCache = new HashMap<>();

    @Unique
    private int better_client$lastCachedShopItem = -1;

    @Inject(method = "init", at = @At("TAIL"))
    public void addSpeedTradeButton(CallbackInfo ci) {
        if (!config.enableFastTrading) return;

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        this.better_client$fastTradingButton = new FastTradingButton(i + 247, j + 37, 18, 18, (button) -> {
            this.menu.setSelectionHint(this.shopItem);
            if (this.minecraft != null) {
                this.minecraft.getConnection().send(new ServerboundSelectTradePacket(this.shopItem));
            }
            better_client$tradeState = 1;
        });
        this.addRenderableWidget(better_client$fastTradingButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (!config.enableFastTrading) return;

        this.better_client$fastTradingButton.active = false;

        Inventory inventory;
        if (this.minecraft != null) {
            if (this.minecraft.player != null) {
                inventory = this.minecraft.player.getInventory();
                MerchantOffer merchantOffer = menu.getOffers().get(this.shopItem);

                if (merchantOffer.getUses() == merchantOffer.getMaxUses()) {
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
                if (this.better_client$lastCachedShopItem == this.shopItem
                        && this.better_client$tradeDescriptionCache.containsKey(this.shopItem)) {
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
                            if (!item.isEmpty()) {
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
            slotClicked(this.menu.getSlot(0), 0, 0, ClickType.PICKUP_ALL);

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

        if (better_client$isInactiveAlt(sellItem)) {
            component.append(Component.translatable("message.better_client.fast_trading.alt")
                    .withStyle(ChatFormatting.RED));
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
                && !KeyUtils.hasAltDown() // Alt keys
                && (sellItem.isDamageableItem() || !sellItem.isStackable());
    }

    @Shadow
    protected abstract void renderButtonArrows(GuiGraphics guiGraphics, MerchantOffer merchantOffer, int i, int j);

    public MerchantScreenMixin(MerchantMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(
            method =
                    "renderButtonArrows(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/trading/MerchantOffer;II)V",
            at = @At("TAIL"))
    private void injectRenderButtonArrows(
            GuiGraphics guiGraphics, MerchantOffer merchantOffer, int i, int j, CallbackInfo ci) {
        if (config.enableDisplayRemainingSales) {
            // 计算剩余交易次数
            int remainingUses = merchantOffer.getMaxUses() - merchantOffer.getUses();

            // 保存当前的渲染状态
            guiGraphics.pose().pushMatrix();
            // 移动到指定位置（基于原代码的 k + 61, p + 11）
            guiGraphics.pose().translate(i + 61, j + 11);
            // 缩放文本（与原代码一致）
            guiGraphics.pose().scale(0.6F, 0.6F);
            // 绘制剩余次数文本，白色 (0xFFFFFFFF)，无阴影
            guiGraphics.drawString(this.font, String.valueOf(remainingUses), 0, 0, 0xFFFFFFFF, false);
            // 恢复渲染状态
            guiGraphics.pose().popMatrix();
        }
    }
}
