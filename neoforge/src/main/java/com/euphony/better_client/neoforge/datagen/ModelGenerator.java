package com.euphony.better_client.neoforge.datagen;

import com.euphony.better_client.BetterClient;
import com.euphony.better_client.client.property.AxolotlBucketVariant;
import com.euphony.better_client.utils.Utils;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.List;

public class ModelGenerator extends ModelProvider {
    public ModelGenerator(PackOutput output) {
        super(output, BetterClient.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        generateAxolotlBucket(itemModels, Items.AXOLOTL_BUCKET);
    }

    public void generateAxolotlBucket(ItemModelGenerators itemModels, Item item) {
        ItemModel.Unbaked itemmodel$unbaked = createAxolotlBucketModel("_wild", itemModels);
        ItemModel.Unbaked itemmodel$unbaked1 = createAxolotlBucketModel("_gold", itemModels);
        ItemModel.Unbaked itemmodel$unbaked2 = createAxolotlBucketModel("_cyan", itemModels);
        ItemModel.Unbaked itemmodel$unbaked3 = createAxolotlBucketModel("_blue", itemModels);
        itemModels.itemModelOutput.accept(
                item,
                ItemModelUtils.select(
                        new AxolotlBucketVariant(0),
                        itemmodel$unbaked,
                        new SelectItemModel.SwitchCase<>(
                                List.of(0.0f),
                                ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(Items.AXOLOTL_BUCKET))),
                        new SelectItemModel.SwitchCase<>(List.of(0.01f), itemmodel$unbaked),
                        new SelectItemModel.SwitchCase<>(List.of(0.02f), itemmodel$unbaked1),
                        new SelectItemModel.SwitchCase<>(List.of(0.03f), itemmodel$unbaked2),
                        new SelectItemModel.SwitchCase<>(List.of(0.04f), itemmodel$unbaked3)));
    }

    public ItemModel.Unbaked createAxolotlBucketModel(String suffix, ItemModelGenerators itemModels) {
        Identifier resourceLocation = Utils.prefix("item/axolotl_bucket" + suffix);
        return ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM.create(
                resourceLocation, TextureMapping.layer0(resourceLocation), itemModels.modelOutput));
    }

    public void itemModel(ItemModelGenerators itemModels, Item item) {
        this.itemModel(itemModels, item, ModelTemplates.FLAT_ITEM);
    }

    public void itemModel(ItemModelGenerators itemModels, Item item, ModelTemplate template) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        Identifier textureLoc =
                Identifier.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath());
        TextureMapping textureMapping = new TextureMapping().put(TextureSlot.LAYER0, textureLoc);
        itemModels.itemModelOutput.accept(
                item,
                new BlockModelWrapper.Unbaked(
                        template.create(item, textureMapping, itemModels.modelOutput), Collections.emptyList()));
    }

    public void itemModel(ItemModelGenerators itemModels, Item item, String loc) {
        this.itemModel(itemModels, item, ModelTemplates.FLAT_ITEM, loc);
    }

    public void itemModel(ItemModelGenerators itemModels, Item item, ModelTemplate template, String loc) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        Identifier textureLoc = Identifier.fromNamespaceAndPath(itemId.getNamespace(), "item/" + loc);
        TextureMapping textureMapping = new TextureMapping().put(TextureSlot.LAYER0, textureLoc);
        itemModels.itemModelOutput.accept(
                item,
                new BlockModelWrapper.Unbaked(
                        template.create(item, textureMapping, itemModels.modelOutput), Collections.emptyList()));
    }
}
