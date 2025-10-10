package com.euphony.better_client.client.events;

import com.euphony.better_client.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;

import java.util.List;

import static com.euphony.better_client.BetterClient.config;

public class CompassTooltipEvent {
    public static void tooltip(ItemStack itemStack, List<Component> components, Item.TooltipContext context, TooltipFlag tooltipFlag) {
        if (!config.enableCompassTooltip) return;

        if (itemStack.is(Items.COMPASS)) {
            getCompassTooltip(itemStack, components);
        } else if (itemStack.is(Items.RECOVERY_COMPASS) && config.enableRecoveryCompassTooltip) {
            getRecoveryCompassTooltip(components);
        }
    }

    public static void getCompassTooltip(ItemStack itemStack, List<Component> components) {
        LodestoneTracker lodestoneTracker = itemStack.get(DataComponents.LODESTONE_TRACKER);

        Level level = Minecraft.getInstance().level;
        if (lodestoneTracker != null && lodestoneTracker.target().isPresent() && config.enableLodestoneTooltip) {
            lodestoneTracker.target().ifPresent(pos -> {
                ResourceLocation location = pos.dimension().location();

                if (level != null && location.equals(level.dimension().location())) {
                    components.add(ItemUtils.createTooltip("info.better_client.tooltip.compass.lodestone_position",
                            getPositionComponent(pos.pos())
                    ));
                    return;
                }

                components.add(ItemUtils.createTooltip("info.better_client.tooltip.compass.lodestone_position.other_dimension",
                        getPositionComponentWithDimension(location, pos.pos())
                ));
            });
            return;
        }

        if (!config.enableNormalCompassTooltip) return;

        if (level != null && level.dimensionType().natural()) {
            GlobalPos spawnPosition = GlobalPos.of(level.dimension(), level.getRespawnData().pos());
            components.add(ItemUtils.createTooltip("info.better_client.tooltip.compass.spawn_position",
                    getPositionComponent(spawnPosition.pos())
            ));
        }
    }

    public static void getRecoveryCompassTooltip(List<Component> components) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        GlobalPos deathLocation = player.getLastDeathLocation().orElse(null);
        if (deathLocation == null) {
            return;
        }

        ResourceLocation location = deathLocation.dimension().location();

        Level level = Minecraft.getInstance().level;

        if (level != null && location.equals(level.dimension().location())) {
            components.add(ItemUtils.createTooltip("info.better_client.tooltip.recovery_compass.death_location",
                    getPositionComponent(deathLocation.pos())
            ));
            return;
        }

        components.add(ItemUtils.createTooltip("info.better_client.tooltip.recovery_compass.death_location.other_dimension",
                getPositionComponentWithDimension(location, deathLocation.pos())
        ));
    }

    public static Object[] getPositionComponent(BlockPos pos) {
        return new Object[]{
                Component.literal(String.valueOf(pos.getX())).withStyle(ChatFormatting.WHITE),
                Component.literal(String.valueOf(pos.getY())).withStyle(ChatFormatting.WHITE),
                Component.literal(String.valueOf(pos.getZ())).withStyle(ChatFormatting.WHITE)
        };
    }

    public static Object[] getPositionComponentWithDimension(ResourceLocation location, BlockPos pos) {
        MutableComponent dimensionName = Component.translatable(location.toString());

        if (location.getNamespace().equals("minecraft")) {
            dimensionName = Component.translatable(String.format("info.better_client.tooltip.dimension.%s", location.getPath()));

        }
        return new Object[]{
                dimensionName.withStyle(ChatFormatting.WHITE),
                Component.literal(String.valueOf(pos.getX())).withStyle(ChatFormatting.WHITE),
                Component.literal(String.valueOf(pos.getY())).withStyle(ChatFormatting.WHITE),
                Component.literal(String.valueOf(pos.getZ())).withStyle(ChatFormatting.WHITE)
        };
    }
}
