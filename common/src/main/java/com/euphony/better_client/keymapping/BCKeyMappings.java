package com.euphony.better_client.keymapping;

import com.euphony.better_client.utils.Utils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class BCKeyMappings {
    private BCKeyMappings() {
    }

    public static KeyMapping bundleUp() {
        return Holder.BUNDLE_UP;
    }

    public static KeyMapping fullBrightnessToggle() {
        return Holder.FULL_BRIGHTNESS_TOGGLE;
    }

    public static KeyMapping itemFrameInvisibilityToggle() {
        return Holder.ITEM_FRAME_INVISIBILITY_TOGGLE;
    }

    private static final class Holder {
        private static final KeyMapping.Category KEYMAPPING_CATEGORY =
                KeyMapping.Category.register(Utils.prefix("keymapping"));

        private static final KeyMapping BUNDLE_UP = new KeyMapping(
                "key.better_client.bundle_up", InputConstants.Type.KEYSYM, InputConstants.KEY_R, KEYMAPPING_CATEGORY);

        private static final KeyMapping FULL_BRIGHTNESS_TOGGLE = new KeyMapping(
                "key.better_client.full_brightness_toggle",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_G,
                KEYMAPPING_CATEGORY);

        private static final KeyMapping ITEM_FRAME_INVISIBILITY_TOGGLE = new KeyMapping(
                "key.better_client.item_frame_invisibility_toggle",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_T,
                KEYMAPPING_CATEGORY);
    }
}
