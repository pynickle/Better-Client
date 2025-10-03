package com.euphony.better_client.neoforge.datagen;

import com.euphony.better_client.BetterClient;
import net.minecraft.DetectedVersion;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Optional;

@EventBusSubscriber(modid = BetterClient.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(true, new ModelGenerator(output));

        generator.addProvider(true, new PackMetadataGenerator(output).add(PackMetadataSection.CLIENT_TYPE, new PackMetadataSection(
                Component.literal("Resources for Better Client"),
                new InclusiveRange<>(PackFormat.of(65), PackFormat.of(69)))));
    }
}
