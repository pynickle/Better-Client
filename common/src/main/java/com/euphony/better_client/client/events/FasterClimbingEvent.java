package com.euphony.better_client.client.events;

import com.euphony.better_client.config.BetterClientConfig;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class FasterClimbingEvent {

    public static void playerPre(Player player) {
        if (!player.level().isClientSide || !BetterClientConfig.HANDLER.instance().enableFasterClimbing) return;

        if (player.onClimbable()) {
            Climber climber = new Climber(player);

            if ((!player.isCrouching() && !player.getInBlockState().is(Blocks.SCAFFOLDING)) || (BetterClientConfig.HANDLER.instance().enableScaffolding && player.getInBlockState().is(Blocks.SCAFFOLDING))) {
                if (BetterClientConfig.HANDLER.instance().enableFasterDownward && climber.isFacingDownward()
                        && !climber.isMovingForward() && !climber.isMovingBackward()) {
                    climber.moveDownFaster();
                } else if (BetterClientConfig.HANDLER.instance().enableFasterUpward && climber.isFacingUpward()
                        && (climber.isMovingForward() || player.jumping)) {
                    climber.moveUpFaster();
                }
            }
        }
    }

    private record Climber(Player player) {
        private boolean isFacingDownward() {
            return player.getXRot() > 0;
        }

        private boolean isFacingUpward() {
            return player.getXRot() < 0;
        }

        private boolean isMovingForward() {
            return player.zza > 0;
        }

        private boolean isMovingBackward() {
            return player.zza < 0;
        }

        private float getSpeed() {
            return (float) (Math.sin(Math.abs(player.getXRot() * Math.PI / 180F))
                    * BetterClientConfig.HANDLER.instance().speedMultiplier
                    / 10);
        }

        public void moveUpFaster() {
            float dy = getSpeed();
            Vec3 move = new Vec3(0, dy, 0);
            player.move(MoverType.SELF, move);
        }

        public void moveDownFaster() {
            float dy = -getSpeed();
            Vec3 move = new Vec3(0, dy, 0);
            player.move(MoverType.SELF, move);
        }
    }
}
