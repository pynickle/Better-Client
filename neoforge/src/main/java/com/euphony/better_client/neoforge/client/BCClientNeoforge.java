package com.euphony.better_client.neoforge.client;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.client.events.BundleUpEvent;
import com.euphony.better_client.client.events.TradingHudEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = BetterClient.MOD_ID, value = Dist.CLIENT)
public class BCClientNeoforge {
    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            TradingHudEvent.clientLevelPost(level);
        }
    }

    @SubscribeEvent
    public static void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        BundleUpEvent.bundleUp(
                Minecraft.getInstance(), event.getScreen(), event.getKeyCode(), event.getScanCode());
    }
}
