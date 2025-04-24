package com.euphony.better_client.utils;

import com.euphony.better_client.BetterClient;
import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class Utils {
    public static String getModDisplayName(String modId) {
        return Platform.getMod(modId).getName();
    }

    public static ResourceLocation prefix(String name) {
        return ResourceLocation.fromNamespaceAndPath(BetterClient.MOD_ID, name.toLowerCase(Locale.ROOT));
    }
}
