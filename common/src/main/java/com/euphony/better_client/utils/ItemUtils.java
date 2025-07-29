package com.euphony.better_client.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class ItemUtils {
    private ItemUtils() {}

    public static MutableComponent createTooltip(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.GRAY);
    }

    public static int getItemTotalCount(Inventory inventory, ItemStack itemStack) {
        ItemStack itemStack1;
        int count = 0;

        for(int i = 0; i < inventory.getNonEquipmentItems().size(); ++i) {
            itemStack1 = inventory.getNonEquipmentItems().get(i);
            if (!itemStack1.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, itemStack1)) {
                count += itemStack1.getCount();
            }
        }

        return count;
    }

    public static Component getWrappedItemName(ItemStack stack) {
        MutableComponent mutableComponent = Component.empty().append(stack.getHoverName());
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            mutableComponent.withStyle(ChatFormatting.ITALIC);
        }

        mutableComponent.withStyle(stack.getRarity().color());

        return createTooltip("[")
                .append(mutableComponent)
                .append(createTooltip("] x"))
                .append(createTooltip(String.valueOf(stack.getCount())));
    }
}