package com.euphony.better_client.client.events;

import com.euphony.better_client.utils.ItemUtils;
import com.euphony.better_client.utils.enums.TooltipCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import static com.euphony.better_client.BetterClient.config;

public class DurabilityTooltipEvent {
    public static void tooltip(ItemStack itemStack, List<Component> components, Item.TooltipContext context, TooltipFlag tooltipFlag) {
        if(!config.enableDurabilityTooltip) return;

        if((config.showDurabilityWhenNotDamaged || itemStack.isDamaged())
                && itemStack.isDamageableItem()) {
            int maxDurability = itemStack.getMaxDamage();
            int durability = maxDurability - itemStack.getDamageValue();

            switch(TooltipCategory.NUMBER) {
                case TooltipCategory.NUMBER:
                    Component durabilityComponent = Component.literal(Integer.toString(durability)).withColor(itemStack.getItem().getBarColor(itemStack));
                    Component maxDurabilityComponent = Component.literal(Integer.toString(maxDurability)).withStyle(ChatFormatting.GRAY);
                    MutableComponent number;
                    if(durability == maxDurability)
                        number = ItemUtils.createTooltip("info.better_client.durability_tooltip.number.full_durability", maxDurabilityComponent);
                    else
                        number = ItemUtils.createTooltip("info.better_client.durability_tooltip.number.damaged", durabilityComponent, maxDurabilityComponent);
                    if(config.showDurabilityHint)
                        number = Component.translatable("info.better_client.durability_tooltip.number.durability_hint", number);
                    components.add(number);
                    break;
            }
        }
    }
}
