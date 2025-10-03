package com.euphony.better_client.keymapping;

import com.euphony.better_client.utils.Utils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class BCKeyMappings {
    private static final KeyMapping.Category KEYMAPPING_CATEGORY = KeyMapping.Category.register(Utils.prefix("keymapping"));

    public static final KeyMapping BUNDLE_UP = new KeyMapping(
            "key.better_client.bundle_up",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            KEYMAPPING_CATEGORY
    );

    public static final KeyMapping FULL_BRIGHTNESS_TOGGLE = new KeyMapping(
            "key.better_client.full_brightness_toggle",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_G,
            KEYMAPPING_CATEGORY
    );
}
