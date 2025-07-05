package com.euphony.better_client.client.init;

import com.euphony.better_client.keymapping.BCKeyMappings;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class KeyMapping {
    public static void registerKeyMapping() {
        KeyMappingRegistry.register(BCKeyMappings.BUNDLE_UP);
    }
}
