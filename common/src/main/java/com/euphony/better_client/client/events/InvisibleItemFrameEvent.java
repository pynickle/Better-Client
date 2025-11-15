package com.euphony.better_client.client.events;

import static com.euphony.better_client.BetterClient.config;

import com.euphony.better_client.keymapping.BCKeyMappings;
import com.euphony.better_client.service.ItemFrameVisibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;

public class InvisibleItemFrameEvent {
    private static boolean wasKeyPressed = false;

    public static void clientLevelPre(ClientLevel clientLevel) {
        boolean isKeyPressed = BCKeyMappings.ITEM_FRAME_INVISIBILITY_TOGGLE.isDown();

        if (!config.enableInvisibleItemFrame) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (minecraft.screen != null
                || minecraft.player == null
                || player.isSpectator()
                || player.isSpectator()
                || !player.getItemInHand(minecraft.player.getUsedItemHand()).isEmpty()
                || !(minecraft.crosshairPickEntity instanceof ItemFrame)) {
            wasKeyPressed = isKeyPressed;
            return;
        }

        if (isKeyPressed && !wasKeyPressed) {
            fullBrightnessToggle(minecraft);
        }

        wasKeyPressed = isKeyPressed;
    }

    public static void fullBrightnessToggle(Minecraft minecraft) {
        ItemFrame frame = (ItemFrame) minecraft.crosshairPickEntity;
        ItemFrameVisibilityManager.getInstance().toggleFrameVisibility(frame.getPos());
    }
}
