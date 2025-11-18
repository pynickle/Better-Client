package com.euphony.better_client.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class ItemUtils {
    private ItemUtils() {}

    public static ResourceLocation getKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static MutableComponent createTooltip(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.GRAY);
    }

    public static MutableComponent createTooltip(String key, Object... args) {
        return Component.translatable(key, args).withStyle(ChatFormatting.GRAY);
    }
}
