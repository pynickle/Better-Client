package com.euphony.better_client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;

import java.util.Objects;

/**
 * Helpers for detecting tradable merchants and villager interaction eligibility.
 */
public class TradingUtils {

    /**
     * Whether the given entity is a merchant we can trade with.
     */
    public static boolean isTradableMerchant(Entity entity) {
        if (Objects.isNull(entity) || !(entity instanceof Merchant)) {
            return false;
        }

        if (entity instanceof Villager villager) {
            return isValidVillagerForTrading(villager);
        }

        return true;
    }

    /**
     * Villagers need extra checks — they must have a non-NONE / non-NITWIT profession,
     * and the player must not be holding a villager spawn egg or a name tag.
     */
    private static boolean isValidVillagerForTrading(Villager villager) {
        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
            return false;
        }

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack mainHandItem = player.getMainHandItem();
            return !mainHandItem.is(Items.VILLAGER_SPAWN_EGG) && !mainHandItem.is(Items.NAME_TAG);
        }

        return true;
    }

    /**
     * Return the entity under the crosshair if it is a tradable merchant, otherwise null.
     *
     * @param minecraft    Minecraft instance
     * @param isWindowOpen whether the trading window is already open
     */
    public static Entity getCrosshairTradableEntity(Minecraft minecraft, boolean isWindowOpen) {
        if (isWindowOpen) {
            return null;
        }

        Entity crosshairTarget = minecraft.crosshairPickEntity;
        return isTradableMerchant(crosshairTarget) ? crosshairTarget : null;
    }
}
