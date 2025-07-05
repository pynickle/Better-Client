package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Options.class)
public class OptionsMixin implements IOptions {
    @Unique
    private OptionInstance<Boolean> better_client$pauseMusic;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(Minecraft minecraft, File file, CallbackInfo ci) {
        this.better_client$pauseMusic = OptionInstance.createBoolean("options.pauseMusic", true);
    }

    @Inject(method = "processOptions", at = @At("TAIL"))
    private void addModOptions(Options.FieldAccess fieldAccess, CallbackInfo ci) {
        fieldAccess.process("pauseMusic", this.better_client$pauseMusic);
    }

    @Override
    public OptionInstance<Boolean> better_client$pauseMusic() {
        return this.better_client$pauseMusic;
    }
}
