package com.euphony.better_client.utils.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;

import java.util.Objects;

public class TradingUtils {

    public static boolean isTradableMerchant(Entity entity) {
        if (Objects.isNull(entity) || !(entity instanceof Merchant)) {
            return false;
        }

        if (entity instanceof Villager villager) {
            return isValidVillagerForTrading(villager);
        }

        return true;
    }

    private static boolean isValidVillagerForTrading(Villager villager) {
        Holder<VillagerProfession> profession = villager.getVillagerData().profession();
        if (profession.is(VillagerProfession.NONE) || profession.is(VillagerProfession.NITWIT)) {
            return false;
        }

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack mainHandItem = player.getMainHandItem();
            return !mainHandItem.is(Items.VILLAGER_SPAWN_EGG) && !mainHandItem.is(Items.NAME_TAG);
        }

        return true;
    }

    public static Entity getCrosshairTradableEntity(Minecraft minecraft, boolean isWindowOpen) {
        if (isWindowOpen) {
            return null;
        }

        Entity crosshairTarget = minecraft.crosshairPickEntity;
        return isTradableMerchant(crosshairTarget) ? crosshairTarget : null;
    }
}
