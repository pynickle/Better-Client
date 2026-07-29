package com.euphony.better_client.utils;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.platform.Platform;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Locale;

public class Utils {
    public static String getModDisplayName(String modId) {
        return Platform.getMod(modId).getName();
    }

    public static Identifier prefix(String name) {
        return Identifier.fromNamespaceAndPath(BetterClient.MOD_ID, name.toLowerCase(Locale.ROOT));
    }

    public static boolean isAnyModLoaded(String... modIds) {
        Collection<String> loadedMods = Platform.getModIds();
        for (String modId : modIds) {
            if (loadedMods.contains(modId)) {
                return true;
            }
        }
        return false;
    }
}
