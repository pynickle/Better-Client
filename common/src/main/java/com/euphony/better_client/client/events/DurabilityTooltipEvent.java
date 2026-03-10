package com.euphony.better_client.client.events;

import com.euphony.better_client.utils.mc.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import static com.euphony.better_client.BetterClient.config;

public class DurabilityTooltipEvent {
    public static void tooltip(
            ItemStack itemStack, List<Component> components, Item.TooltipContext context, TooltipFlag tooltipFlag) {
        if (!config.enableDurabilityTooltip) return;

        if ((config.showDurabilityWhenNotDamaged || itemStack.isDamaged()) && itemStack.isDamageableItem()) {
            int maxDurability = itemStack.getMaxDamage();
            int durability = maxDurability - itemStack.getDamageValue();
            int barColor = itemStack.getItem().getBarColor(itemStack);

            switch (config.durabilityTooltipStyle) {
                case NUMBER -> {
                    Component durabilityComponent =
                            Component.literal(Integer.toString(durability)).withColor(barColor);
                    Component maxDurabilityComponent =
                            Component.literal(Integer.toString(maxDurability)).withStyle(ChatFormatting.GRAY);
                    MutableComponent number;
                    if (durability == maxDurability)
                        number = ItemUtils.createTooltip(
                                "info.better_client.durability_tooltip.number.full_durability", maxDurabilityComponent);
                    else
                        number = ItemUtils.createTooltip(
                                "info.better_client.durability_tooltip.number.damaged",
                                durabilityComponent,
                                maxDurabilityComponent);
                    if (config.showDurabilityHint)
                        number =
                                Component.translatable("info.better_client.durability_tooltip.durability_hint", number);
                    components.add(number);
                }
                case BAR -> {
                    // 经典耐久条风格：  [█████░░░░░]  73/100
                    double percent = (double) durability / maxDurability;
                    int barLength = 10;
                    int filled = (int) Math.round(percent * barLength);

                    MutableComponent bar = Component.literal("[").withStyle(ChatFormatting.GRAY);

                    for (int i = 0; i < barLength; i++) {
                        if (i < filled) {
                            bar.append(Component.literal("█").withColor(barColor));
                        } else {
                            bar.append(Component.literal("░").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }

                    bar.append(Component.literal("] ").withStyle(ChatFormatting.GRAY));

                    Component num = Component.literal(durability + "/" + maxDurability)
                            .withStyle(Style.EMPTY.withColor(barColor));

                    components.add(bar.append(num));
                }

                case PERCENTAGE -> {
                    int percentInt = Math.round((float) durability / maxDurability * 100);
                    Component percentComp =
                            Component.literal(percentInt + "%").withStyle(Style.EMPTY.withColor(barColor));

                    Component line =
                            ItemUtils.createTooltip("info.better_client.durability_tooltip.percentage", percentComp);

                    if (config.showDurabilityHint) {
                        line = Component.translatable("info.better_client.durability_tooltip.durability_hint", line);
                    }

                    components.add(line);
                }
            }
        }
    }
}
