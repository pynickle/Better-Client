package com.euphony.better_client.keymapping;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class BCKeyMappings {
    public static final KeyMapping BUNDLE_UP = new KeyMapping(
            "key.better_client.bundle_up",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.better_client.keymapping"
    );
}
