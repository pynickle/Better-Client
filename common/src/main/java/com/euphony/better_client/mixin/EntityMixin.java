package com.euphony.better_client.mixin;

import com.euphony.better_client.service.ItemFrameVisibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    private Level level;

    @Unique
    private final Entity better_client$TARGET = (Entity) (Object) this;

    @Inject(method = "isInvisible", at = @At("RETURN"), cancellable = true)
    private void isInvisible(CallbackInfoReturnable<Boolean> cir) {
        // 只在客户端处理物品展示框
        if (this.better_client$TARGET instanceof ItemFrame && this.level.isClientSide()) {
            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;

            BlockPos entityId = ((ItemFrame) this.better_client$TARGET).getPos();
            boolean isHidden = ItemFrameVisibilityManager.isFrameHidden(entityId);

            // 如果物品展示框被标记为隐形
            if (isHidden) {
                // 但玩家持有物品展示框时显示
                if (player != null && (player.isHolding(Items.ITEM_FRAME) || player.isHolding(Items.GLOW_ITEM_FRAME))) {
                    cir.setReturnValue(false);
                } else {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
