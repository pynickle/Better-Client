package com.euphony.better_client.mixin;

import com.euphony.better_client.service.ClientWeatherHandler;
import com.euphony.better_client.utils.enums.ClientWeather;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.euphony.better_client.BetterClient.config;

@Environment(EnvType.CLIENT)
@Mixin(Level.class)
public class LevelMixin {
    @ModifyReturnValue(method = "getRainLevel", at = @At("RETURN"))
    public float getRainLevel(float original) {
        if (!config.enableClientWeather) {
            return original;
        }

        ClientWeather mode = ClientWeatherHandler.getMode();

        if (mode == ClientWeather.CLEAR) {
            return 0f;
        } else if (mode == ClientWeather.RAIN || mode == ClientWeather.THUNDER) {
            return 1f;
        } else {
            return original;
        }
    }

    @ModifyReturnValue(method = "getThunderLevel", at = @At("RETURN"))
    public float getThunderLevel(float original) {
        if (!config.enableClientWeather) {
            return original;
        }

        ClientWeather mode = ClientWeatherHandler.getMode();

        if (mode == ClientWeather.CLEAR) {
            return 0f;
        } else if (mode == ClientWeather.THUNDER) {
            return 1f;
        } else {
            return original;
        }
    }
}
