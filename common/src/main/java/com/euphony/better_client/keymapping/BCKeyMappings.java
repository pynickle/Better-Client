package com.euphony.better_client.keymapping;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class BCKeyMappings {
    public static final String CATEGORY = "key.categories.better_client";

    public static KeyMapping bundleUp() {
        return Holder.BUNDLE_UP;
    }

    private BCKeyMappings() {
    }

    private static final class Holder {
        private static final KeyMapping BUNDLE_UP = new KeyMapping(
                "key.better_client.bundle_up", InputConstants.Type.KEYSYM, InputConstants.KEY_R, CATEGORY);
    }
}
