package com.euphony.better_client.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface BundlePanelContainerScreenAccessor {
    @Accessor("leftPos") int better_client$leftPos();
    @Accessor("leftPos") void better_client$leftPos(int value);
    @Accessor("topPos") int better_client$topPos();
    @Accessor("imageWidth") int better_client$imageWidth();
}
