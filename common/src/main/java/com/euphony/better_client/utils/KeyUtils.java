package com.euphony.better_client.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

public class KeyUtils {
    public static boolean hasControlDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 341)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 345);
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 344);
    }

    public static boolean hasAltDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 342)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 346);
    }
}
