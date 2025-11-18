package com.euphony.better_client.client.events;

import com.euphony.better_client.config.BetterClientConfig;
import com.euphony.better_client.utils.Utils;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class ItemPropertiesEvent {
    public static void clientSetup(Minecraft minecraft) {
        ItemPropertiesRegistry.register(Items.AXOLOTL_BUCKET, Utils.prefix("variant"), (stack, level, entity, seed) -> {
            if (!BetterClientConfig.HANDLER.instance().enableAxolotlBucketFix) return 0;

            int axolotlType = 0;
            CustomData customData;
            DataComponentMap components = stack.getComponents();
            if (components.has(DataComponents.BUCKET_ENTITY_DATA)) {
                customData = components.get(DataComponents.BUCKET_ENTITY_DATA);
                if (customData != null) {
                    axolotlType = customData.copyTag().getInt("Variant");
                }
            }
            return axolotlType * 0.01f + 0.0001f;
        });
    }
}
