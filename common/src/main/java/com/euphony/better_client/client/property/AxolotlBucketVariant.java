package com.euphony.better_client.client.property;

import com.euphony.better_client.config.BetterClientConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record AxolotlBucketVariant(float variant) implements SelectItemModelProperty<Float> {
    public static final PrimitiveCodec<Float> VALUE_CODEC;
    public static final SelectItemModelProperty.Type<AxolotlBucketVariant, Float> TYPE;

    @Override
    public Float get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, ItemDisplayContext itemDisplayContext) {
        if(!BetterClientConfig.HANDLER.instance().enableAxolotlBucketFix) return 0f;

        int axolotlType = 0;
        Axolotl.Variant customData;
        DataComponentMap components = stack.getComponents();
        if (components.has(DataComponents.AXOLOTL_VARIANT)) {
            customData = components.get(DataComponents.AXOLOTL_VARIANT);
            if (customData != null) {
                axolotlType = customData.getId();
            }
        }
        return axolotlType * 0.01f;
    }

    @Override
    public @NotNull Codec<Float> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    public @NotNull Type<? extends SelectItemModelProperty<Float>, Float> type() {
        return TYPE;
    }

    static {
        VALUE_CODEC = Codec.FLOAT;
        TYPE = Type.create(RecordCodecBuilder.mapCodec((instance) -> instance.group(ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("variant", 0f).forGetter(AxolotlBucketVariant::variant)).apply(instance, AxolotlBucketVariant::new)), VALUE_CODEC);
    }
}
