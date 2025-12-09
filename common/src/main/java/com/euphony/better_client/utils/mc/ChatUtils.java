package com.euphony.better_client.utils.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ChatUtils {
    public static void sendClientMsg(Component message) {
        Minecraft.getInstance().gui.getChat().addMessage(message);
    }

    public static void sendClientMsgTranslatable(String message) {
        sendClientMsg(Component.translatable(message));
    }
}
