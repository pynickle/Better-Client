package com.euphony.better_client.client.events;

import com.euphony.better_client.config.Config;
import com.euphony.better_client.keymapping.BCKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.euphony.better_client.BetterClient.config;

public class ClickThroughEvent {
    private static final TagKey<Block> COMMON_CHESTS =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "chests"));
    private static final TagKey<Block> COMMON_BARRELS =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "barrels"));

    private static boolean wasKeyPressed = false;
    private static boolean dyeOnSign = false;

    private static List<String> cachedContainerIds = null;
    private static Set<Block> extraContainers = Set.of();

    public static void clientLevelPre(ClientLevel clientLevel) {
        boolean isKeyPressed = BCKeyMappings.clickThroughToggle().isDown();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() == null && isKeyPressed && !wasKeyPressed) {
            clickThroughToggle(minecraft);
        }

        wasKeyPressed = isKeyPressed;
    }

    public static void clickThroughToggle(Minecraft minecraft) {
        config.enableClickThrough = !config.enableClickThrough;
        Config.save();

        LocalPlayer player = minecraft.player;
        if (player != null) {
            player.sendOverlayMessage(Component.translatable(config.enableClickThrough
                    ? "text.better_client.click_through.active"
                    : "text.better_client.click_through.inactive"));
        }
    }

    /** Redirects wall signs, banners, and item frames to an interactive container behind them. */
    public static HitResult redirectHitResult(HitResult hitResult, LocalPlayer player, ClientLevel level) {
        dyeOnSign = false;
        if (!config.enableClickThrough || hitResult == null) {
            return hitResult;
        }

        if (hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof ItemFrame itemFrame) {
            BlockPos attachedPos = itemFrame.getPos().relative(itemFrame.getDirection().getOpposite());
            if (!player.isShiftKeyDown() && isContainerAt(attachedPos, level)) {
                return new BlockHitResult(hitResult.getLocation(), itemFrame.getDirection(), attachedPos, false);
            }
        } else if (hitResult instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = blockHit.getBlockPos();
            BlockState state = level.getBlockState(blockPos);
            if (state.getBlock() instanceof WallSignBlock) {
                return redirectFromWallSign(blockHit, state, player, level);
            }
            if (state.getBlock() instanceof WallBannerBlock) {
                BlockPos attachedPos = blockPos.relative(state.getValue(WallBannerBlock.FACING).getOpposite());
                if (!player.isShiftKeyDown() && isContainerAt(attachedPos, level)) {
                    return new BlockHitResult(blockHit.getLocation(), blockHit.getDirection(), attachedPos, false);
                }
            }
        }
        return hitResult;
    }

    private static HitResult redirectFromWallSign(
            BlockHitResult blockHit, BlockState state, LocalPlayer player, ClientLevel level) {
        BlockPos blockPos = blockHit.getBlockPos();
        BlockPos attachedPos = blockPos.relative(state.getValue(WallSignBlock.FACING).getOpposite());
        if (!isContainerAt(attachedPos, level) || !(level.getBlockEntity(blockPos) instanceof SignBlockEntity)) {
            return blockHit;
        }

        boolean holdingSignApplicator =
                player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SignApplicator;
        if (holdingSignApplicator && config.clickThroughSneakToDye) {
            if (player.isShiftKeyDown()) {
                // Let sign applicators use the sign while sneaking.
                dyeOnSign = true;
                return blockHit;
            }
            return new BlockHitResult(blockHit.getLocation(), blockHit.getDirection(), attachedPos, false);
        }
        if (!holdingSignApplicator && !player.isShiftKeyDown()) {
            return new BlockHitResult(blockHit.getLocation(), blockHit.getDirection(), attachedPos, false);
        }
        return blockHit;
    }

    /** Suppresses secondary use once so a sneaking player can apply a sign applicator. */
    public static boolean consumeDyeOnSign() {
        if (!dyeOnSign) {
            return false;
        }
        dyeOnSign = false;
        return true;
    }

    private static boolean isContainerAt(BlockPos pos, ClientLevel level) {
        if (!config.clickThroughOnlyContainers) {
            return true;
        }
        if (level.getBlockEntity(pos) instanceof BaseContainerBlockEntity) {
            return true;
        }
        BlockState state = level.getBlockState(pos);
        return state.is(COMMON_CHESTS)
                || state.is(COMMON_BARRELS)
                || state.is(BlockTags.GUARDED_BY_PIGLINS)
                || extraContainers().contains(state.getBlock());
    }

    private static Set<Block> extraContainers() {
        List<String> ids = config.clickThroughContainers;
        if (!ids.equals(cachedContainerIds)) {
            cachedContainerIds = List.copyOf(ids);
            extraContainers = ids.stream()
                    .map(Identifier::tryParse)
                    .filter(Objects::nonNull)
                    .flatMap(id -> BuiltInRegistries.BLOCK.getOptional(id).stream())
                    .collect(Collectors.toUnmodifiableSet());
        }
        return extraContainers;
    }
}
