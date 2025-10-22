package com.euphony.better_client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;

import java.util.Objects;

/**
 * 交易相关的工具类
 */
public class TradingUtils {

    /**
     * 检查实体是否为可交易的商人
     * @param entity 要检查的实体
     * @return 如果实体可以交易则返回 true
     */
    public static boolean isTradableMerchant(Entity entity) {
        if (Objects.isNull(entity) || !(entity instanceof Merchant)) {
            return false;
        }

        // 如果是村民，需要额外检查职业和玩家手持物品
        if (entity instanceof Villager villager) {
            return isValidVillagerForTrading(villager);
        }

        return true;
    }

    /**
     * 检查村民是否可以交易
     * @param villager 村民实体
     * @return 如果村民可以交易则返回 true
     */
    private static boolean isValidVillagerForTrading(Villager villager) {
        // 检查村民职业
        Holder<VillagerProfession> profession = villager.getVillagerData().profession();
        if (profession.is(VillagerProfession.NONE) || profession.is(VillagerProfession.NITWIT)) {
            return false;
        }

        // 检查玩家手持物品
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack mainHandItem = player.getMainHandItem();
            return !mainHandItem.is(Items.VILLAGER_SPAWN_EGG) && !mainHandItem.is(Items.NAME_TAG);
        }

        return true;
    }

    /**
     * 获取当前准星指向的可交易实体
     * @param minecraft Minecraft 实例
     * @param isWindowOpen 交易窗口是否已打开
     * @return 可交易的实体，如果没有则返回 null
     */
    public static Entity getCrosshairTradableEntity(Minecraft minecraft, boolean isWindowOpen) {
        if (isWindowOpen) {
            return null;
        }

        Entity crosshairTarget = minecraft.crosshairPickEntity;
        return isTradableMerchant(crosshairTarget) ? crosshairTarget : null;
    }
}
