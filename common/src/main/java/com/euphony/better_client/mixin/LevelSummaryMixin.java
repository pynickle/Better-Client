package com.euphony.better_client.mixin;

import com.euphony.better_client.api.IHasPlayTime;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelSummary.class)
public class LevelSummaryMixin implements IHasPlayTime {
    @Unique
    private int better_client$playTimeTicks = -1;

    @Override
    public void better_client$setPlayTimeTicks(int playTimeTicks) {
        this.better_client$playTimeTicks = playTimeTicks;
    }

    @Override
    public int better_client$getPlayTimeTicks() {
        return this.better_client$playTimeTicks;
    }
}
