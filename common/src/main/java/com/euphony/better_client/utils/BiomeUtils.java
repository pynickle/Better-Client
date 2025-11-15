package com.euphony.better_client.utils;

import java.util.StringJoiner;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.StringUtils;

/**
 * 生物群系相关的工具方法
 */
public class BiomeUtils {

    private BiomeUtils() {}

    /**
     * 将 snake_case 格式转换为英文标题格式
     * @param snakeCaseText snake_case 格式的文本
     * @return 转换后的英文标题
     */
    public static String snakeCaseToTitle(String snakeCaseText) {
        String[] words = snakeCaseText.split("_");
        StringJoiner formatted = new StringJoiner(" ");

        for (String word : words) {
            formatted.add(StringUtils.capitalize(word));
        }

        return formatted.toString();
    }

    /**
     * 获取模组显示名称
     * @param location 资源位置
     * @return 模组显示名称，如果获取不到则返回格式化的命名空间
     */
    public static String getModDisplayName(ResourceLocation location) {
        String modId = location.getNamespace();
        String displayName = Utils.getModDisplayName(modId);
        return displayName != null ? displayName : snakeCaseToTitle(modId);
    }

    /**
     * 创建生物群系显示组件
     * @param key 生物群系键
     * @param enableModName 是否启用模组名称显示
     * @return 生物群系显示组件
     */
    public static Component createBiomeDisplayComponent(ResourceKey<Biome> key, boolean enableModName) {
        ResourceLocation location = key.location();
        String translationKey = Util.makeDescriptionId("biome", location);
        MutableComponent biomeName = Component.translatable(translationKey);

        // 如果翻译键等于显示文本，说明没有翻译，使用格式化的路径
        String displayedText = biomeName.getString();
        if (displayedText.equals(translationKey)) {
            String biomePath = location.getPath();
            biomeName = Component.literal(snakeCaseToTitle(biomePath));
        }

        // 添加模组名称（如果启用）
        if (enableModName) {
            String modName = getModDisplayName(location);
            biomeName = biomeName.append(Component.literal(String.format(" (%s)", modName)));
        }

        return biomeName;
    }
}
