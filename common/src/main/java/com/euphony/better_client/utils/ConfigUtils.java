package com.euphony.better_client.utils;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.utils.enums.DescComponent;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigUtils {
    public static final int IMG_WIDTH = 1920;
    public static final int IMG_HEIGHT = 991;

    private static final Map<ResourceLocation, int[]> IMAGE_DIMENSIONS_CACHE = new HashMap<>();

    public static final OptionFlag RESOURCE_RELOAD = (client) -> {
        if (client.hasSingleplayerServer()) {
            MinecraftServer server = client.getSingleplayerServer();
            if (server != null) {
                PackRepository packrepository = server.getPackRepository();
                Collection<String> collection = packrepository.getSelectedIds();
                server.reloadResources(collection);
            }
        }
    };

    public static ListOption.Builder<String> getListGroupOption(String name) {
        return ListOption.<String>createBuilder()
                .name(getOptionName(name))
                .description(OptionDescription.createBuilder()
                        .text(getDesc(name, null))
                        .build());
    }

    public static ButtonOption.Builder getButtonOption(String name) {
        return ButtonOption.createBuilder()
                .name(getButtonOptionName(name))
                .description(OptionDescription.createBuilder()
                        .text(getDesc(name, null))
                        .build());
    }

    public static Option<Boolean> buildBooleanOption(
            String name, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return getBooleanOption(name)
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    public static Option.Builder<Boolean> getBooleanOption(String name) {
        return Option.<Boolean>createBuilder()
                .name(getOptionName(name))
                .description(OptionDescription.createBuilder()
                        .text(getDesc(name, null))
                        .build());
    }

    public static <T> Option.Builder<T> getGenericOption(String name) {
        return getGenericOption(name, (DescComponent) null);
    }

    public static <T> Option.Builder<T> getGenericOption(String name, DescComponent descComponent) {
        return Option.<T>createBuilder()
                .name(getOptionName(name))
                .description(OptionDescription.createBuilder()
                        .text(getDesc(name, descComponent))
                        .build());
    }

    public static <T> Option.Builder<T> getGenericOption(String name, String image) {
        return getGenericOption(name, image, null);
    }

    public static <T> Option.Builder<T> getGenericOption(String name, String image, DescComponent descComponent) {
        int[] dimensions = getImageDimensions(Utils.prefix(String.format("config/%s.png", image)));
        return Option.<T>createBuilder()
                .name(getOptionName(name))
                .description(OptionDescription.createBuilder()
                        .text(getDesc(name, descComponent))
                        .image(getImage(image), dimensions[0], dimensions[1])
                        .build());
    }

    public static int[] getImageDimensions(ResourceLocation location) {
        if (IMAGE_DIMENSIONS_CACHE.containsKey(location)) {
            return IMAGE_DIMENSIONS_CACHE.get(location);
        }

        Minecraft mc = Minecraft.getInstance();

        Optional<Resource> resource = mc.getResourceManager().getResource(location);
        if (resource.isPresent()) {
            try (InputStream inputStream = resource.get().open()) {
                BufferedImage image = ImageIO.read(inputStream);
                if (image != null) {
                    int[] dimensions = new int[] {image.getWidth(), image.getHeight()};
                    IMAGE_DIMENSIONS_CACHE.put(location, dimensions);
                    return dimensions;
                }
            } catch (IOException e) {
                return new int[] {IMG_WIDTH, IMG_HEIGHT};
            }
        }
        return new int[] {IMG_WIDTH, IMG_HEIGHT};
    }

    public static Component getCategoryName(String category) {
        return Component.translatable(
                String.format("yacl3.config.%s:config.category.%s", BetterClient.MOD_ID, category));
    }

    public static Component getGroupName(String category, String group) {
        return Component.translatable(
                String.format("yacl3.config.%s:config.category.%s.group.%s", BetterClient.MOD_ID, category, group));
    }

    private static Component getButtonOptionName(String option) {
        return Component.translatable(String.format("yacl3.config.%s:config.%s.button", BetterClient.MOD_ID, option));
    }

    private static Component getOptionName(String option) {
        return Component.translatable(String.format("yacl3.config.%s:config.%s", BetterClient.MOD_ID, option));
    }

    private static Component getDesc(String option, DescComponent descComponent) {
        MutableComponent component =
                Component.translatable(String.format("yacl3.config.%s:config.%s.desc", BetterClient.MOD_ID, option));
        if (descComponent != null) component.append(Component.literal("\n\n").append(descComponent.getText()));
        return component;
    }

    private static ResourceLocation getImage(String name) {
        return Utils.prefix(String.format("config/%s.png", name));
    }
}
