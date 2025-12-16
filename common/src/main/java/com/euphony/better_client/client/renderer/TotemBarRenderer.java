package com.euphony.better_client.client.renderer;

import com.euphony.better_client.config.option.TotemBarRenderMode;
import com.euphony.better_client.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

import static com.euphony.better_client.BetterClient.config;

public class TotemBarRenderer {
    private static final Identifier TEX_FULL  = Utils.prefix("textures/gui/sprites/totem.png");
    private static final Identifier TEX_EMPTY = Utils.prefix("textures/gui/sprites/totem_empty.png");

    public static void render(GuiGraphics ctx, DeltaTracker tickCounter) {
        if (!config.enableTotemBar) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;
        if (client.player.isCreative() || client.player.isSpectator()) return;

        int totInventory = client.player.getInventory().countItem(Items.TOTEM_OF_UNDYING);
        int handTotems = 0;
        if (client.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) handTotems++;
        if (client.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING))  handTotems++;

        drawIcons(ctx, totInventory, handTotems);
    }

    private static void drawIcons(GuiGraphics ctx, int totInventory, int hand) {
        int iconSize = 9;
        int xSpace = -1;
        int ySpace = 1;
        int yOffset = 30;
        int xOffset = 91;

        int step = iconSize + xSpace;
        int total = Math.min(totInventory, 10);
        if (config.totemBarRenderMode == TotemBarRenderMode.INVENTORY_ONLY) {
            total = total - hand;
        }
        if (total <= 0) return;

        Minecraft client = Minecraft.getInstance();
        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();

        int rightEdge = sw / 2 + xOffset;
        int totalWidth = iconSize + (total - 1) * step;
        int x0 = rightEdge - totalWidth;

        int y = sh - yOffset - ySpace - (iconSize * 2);
        y -= getTotemBarYOffset(client, iconSize + ySpace);

        for (int i = 0; i < total; i++) {
            boolean isFull = switch (config.totemBarRenderMode) {
                case INVENTORY_ONLY -> true;
                case COMBINED -> i >= (total - hand);
            };
            Identifier tex = isFull ? TEX_FULL : TEX_EMPTY;

            ctx.blit(
                    tex,
                    x0 + i * step,
                    y,
                    x0 + i * step + iconSize,
                    y + iconSize,
                    0f, 1f, 0f, 1f
            );
        }
    }

    private static int getTotemBarYOffset(Minecraft client, int spacing) {
        if (client.player == null) return 0;
        int offset = config.totemBarYOffset;

        boolean airBar = client.player.isUnderWater() || client.player.getAirSupply() < client.player.getMaxAirSupply();
        if (airBar) offset += spacing;

        var vehicle = client.player.getVehicle();
        if (vehicle instanceof LivingEntity mount) {
            float hp = mount.getMaxHealth();
            if (hp > 0f) {
                if (hp > 20f) {
                    int extraLines = (int) Math.ceil((hp - 20f) / 20f);
                    offset += extraLines * spacing;
                }
                if (FabricLoader.getInstance().isModLoaded("bettermounthud")) {
                    offset += spacing;
                }
            }
        }
        return offset;
    }
}