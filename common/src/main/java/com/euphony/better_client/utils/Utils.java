package com.euphony.better_client.utils;

import com.euphony.better_client.BetterClient;
import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Locale;

public class Utils {
    public static String getModDisplayName(String modId) {
        return Platform.getMod(modId).getName();
    }

    public static ResourceLocation prefix(String name) {
        return ResourceLocation.fromNamespaceAndPath(BetterClient.MOD_ID, name.toLowerCase(Locale.ROOT));
    }

    /**
     * 检查是否有任何一个指定的模组已加载
     * @param modIds 要检查的模组ID列表
     * @return 如果至少有一个模组已加载则返回true，否则返回false
     */
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
