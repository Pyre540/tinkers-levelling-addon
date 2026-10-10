package pyre.tinkerslevellingaddon.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
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

    public static boolean canPlaceBucketFluid(UseOnContext context, Player player, FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        Direction face = context.getClickedFace();
        Level world = context.getLevel();
        BlockPos target = context.getClickedPos();
        BlockPos offset = target.relative(face);
        if (!world.mayInteract(player, target) || !player.mayUseItemAt(offset, face, context.getItemInHand())) {
            return false;
        }

        BlockState existing = world.getBlockState(target);
        if (cannotContainFluid(world, target, existing, fluidStack.getFluid())) {
            target = offset;
            existing = world.getBlockState(target);
            if (!existing.isAir() && !existing.canBeReplaced(fluid) &&
                    cannotContainFluid(world, target, existing, fluidStack.getFluid())) {
                return false;
            }
        }

        return fluid.getFluidType().isVaporizedOnPlacement(world, target, fluidStack) ||
                world.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER) ||
                existing.getBlock() instanceof LiquidBlockContainer ||
                existing.canBeReplaced(fluid);
    }

    private static boolean canGlowBlockStay(Level world, BlockPos pos, Direction facing) {
        BlockPos placedOn = pos.relative(facing);

        boolean isSolidSide = Block.isFaceFull(world.getBlockState(placedOn)
                .getOcclusionShape(world, pos), facing.getOpposite());
        boolean isLiquid = world.getBlockState(pos).getBlock() instanceof LiquidBlock;

        return !isLiquid && isSolidSide;
    }

    private static boolean cannotContainFluid(Level world, BlockPos pos, BlockState state, Fluid fluid) {
        Block block = state.getBlock();
        return !(block instanceof LiquidBlockContainer container && container.canPlaceLiquid(world, pos, state, fluid));
    }

    private WorldUtil() {
        //hide constructor
    }
}
