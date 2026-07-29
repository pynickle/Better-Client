package com.euphony.better_client.utils.mc;

import com.euphony.better_client.utils.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.StringUtils;

import java.util.StringJoiner;

public class BiomeUtils {

    private BiomeUtils() {
    }

    public static String snakeCaseToTitle(String snakeCaseText) {
        String[] words = snakeCaseText.split("_");
        StringJoiner formatted = new StringJoiner(" ");

        for (String word : words) {
            formatted.add(StringUtils.capitalize(word));
        }

        return formatted.toString();
    }

    public static String getModDisplayName(Identifier location) {
        String modId = location.getNamespace();
        String displayName = Utils.getModDisplayName(modId);
        return displayName != null ? displayName : snakeCaseToTitle(modId);
    }

    public static Component createBiomeDisplayComponent(ResourceKey<Biome> key, boolean enableModName) {
        Identifier location = key.identifier();
        String translationKey = Util.makeDescriptionId("biome", location);
        MutableComponent biomeName = Component.translatable(translationKey);

        String displayedText = biomeName.getString();
        if (displayedText.equals(translationKey)) {
            String biomePath = location.getPath();
            biomeName = Component.literal(snakeCaseToTitle(biomePath));
        }

        if (enableModName) {
            String modName = getModDisplayName(location);
            biomeName = biomeName.append(Component.literal(String.format(" (%s)", modName)));
        }

        return biomeName;
    }
}
