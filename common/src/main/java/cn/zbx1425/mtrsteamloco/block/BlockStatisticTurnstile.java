package cn.zbx1425.mtrsteamloco.block;

import cn.zbx1425.mtrsteamloco.Main;
import mtr.block.IBlock;
import mtr.mappings.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.StringUtils;

public class BlockStatisticTurnstile extends BlockDirectionalMapper implements EntityBlockMapper {

    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    public BlockStatisticTurnstile() {
        super(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_GRAY).strength(2).noOcclusion());
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos blockPos, Entity entity) {
        if (!level.isClientSide && entity instanceof Player) {
            final Direction facing = IBlock.getStatePropertySafe(state, FACING);
            final Vec3 playerPosRotated = entity.position().subtract(blockPos.getX() + 0.5, 0, blockPos.getZ() + 0.5).yRot((float) Math.toRadians(facing.toYRot()));
            final boolean open = IBlock.getStatePropertySafe(state, OPEN);

            if (open && playerPosRotated.z > 0) {
                level.setBlockAndUpdate(blockPos, state.setValue(OPEN, false));
            } else if (!open && playerPosRotated.z < 0) {
                BlockPos mainBlockPos = searchMainBlock(level, blockPos);
                BlockEntityStatisticTurnstile blockEntity = (BlockEntityStatisticTurnstile) level.getBlockEntity(mainBlockPos);
                assert blockEntity != null;

                level.playSound(null, blockPos, mtr.SoundEvents.TICKET_BARRIER, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(blockPos, state.setValue(OPEN, true));
                if (!level.getBlockTicks().hasScheduledTick(blockPos, this)) {
                    Utilities.scheduleBlockTick(level, blockPos, this, 40);
                }
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos) {
        world.setBlockAndUpdate(pos, state.setValue(OPEN, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection()).setValue(OPEN, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        final Direction facing = IBlock.getStatePropertySafe(state, FACING);
        return IBlock.getVoxelShapeByDirection(12, 0, 0, 16, 15, 16, facing);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        final Direction facing = IBlock.getStatePropertySafe(state, FACING);
        final boolean open = IBlock.getStatePropertySafe(state, OPEN);
        final VoxelShape base = IBlock.getVoxelShapeByDirection(15, 0, 0, 16, 24, 16, facing);
        return open ? base : Shapes.or(IBlock.getVoxelShapeByDirection(0, 0, 7, 16, 24, 9, facing), base);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    public BlockPos searchMainBlock(Level level, BlockPos start) {
        BlockPos cursor = new BlockPos(start);
        while (level.getBlockState(cursor.west()).is(this)) cursor = cursor.west();
        while (level.getBlockState(cursor.north()).is(this)) cursor = cursor.north();
        return cursor;
    }

    @Override
    public BlockEntityMapper createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntityStatisticTurnstile(blockPos, blockState);
    }

    public static class BlockEntityStatisticTurnstile extends BlockEntityMapper {

        public String counterName;

        public boolean isActive() {
            return counterName != null;
        }

        public BlockEntityStatisticTurnstile(BlockPos pos, BlockState state) {
            super(Main.BLOCK_ENTITY_TYPE_STATISTIC_TURNSTILE.get(), pos, state);
        }

        @Override
        public void readCompoundTag(CompoundTag compoundTag) {
            counterName = compoundTag.getString("counterName");
            if (StringUtils.isEmpty(counterName)) counterName = null;
        }

        @Override
        public void writeCompoundTag(CompoundTag compoundTag) {
            compoundTag.putString("counterName", counterName == null ? "" : counterName);
        }
    }
}
