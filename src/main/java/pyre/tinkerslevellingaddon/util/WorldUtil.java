package pyre.tinkerslevellingaddon.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.block.GlowBlock;

public class WorldUtil {

    public static boolean canPlaceGlow(Level world, BlockPos pos, Direction direction) {
        BlockState state = world.getBlockState(pos);
        GlowBlock glowBlock = TinkerCommons.glowBlock.get();
        if (state.getBlock() != glowBlock && state.canBeReplaced()) {
            if (canGlowBlockStay(world, pos, direction)) {
                return true;
            } else {
                for (Direction direction1 : Direction.values()) {
                    if (canGlowBlockStay(world, pos, direction1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean canIgnite(Level world, BlockPos pos, BlockState state, Direction sideHit, Direction facing) {
        return CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)
                || state.getBlock() instanceof TntBlock || BaseFireBlock.canBePlacedAt(world, pos.relative(sideHit), facing);
    }

    public static boolean isBrushable(BlockHitResult blockHit, Level world) {
        return world.getBlockEntity(blockHit.getBlockPos()) instanceof BrushableBlockEntity;
    }

    private static boolean canGlowBlockStay(Level world, BlockPos pos, Direction facing) {
        BlockPos placedOn = pos.relative(facing);

        boolean isSolidSide = Block.isFaceFull(world.getBlockState(placedOn)
                .getOcclusionShape(world, pos), facing.getOpposite());
        boolean isLiquid = world.getBlockState(pos).getBlock() instanceof LiquidBlock;

        return !isLiquid && isSolidSide;
    }

    private WorldUtil() {
        //hide constructor
    }
}
