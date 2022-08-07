package cn.zbx1425.mtrsteamloco.block;

import cn.zbx1425.mtrsteamloco.Main;
import mtr.block.IBlock;
import mtr.mappings.*;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
import org.apache.commons.lang3.SerializationUtils;

import java.util.HashMap;

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
                BlockState mainBlockState = level.getBlockState(mainBlockPos);
                BlockEntityStatisticTurnstile blockEntity = (BlockEntityStatisticTurnstile) level.getBlockEntity(mainBlockPos);
                assert blockEntity != null;

                if (blockEntity.isActive) {
                    if (blockEntity.handleVisitor((Player) entity)) {
                        ((Player) entity).displayClientMessage(new TranslatableComponent("gui.mtrsteamloco.statistic_turnstile_visitor_new", blockEntity.visitorCount), true);
                    } else {
                        ((Player) entity).displayClientMessage(new TranslatableComponent("gui.mtrsteamloco.statistic_turnstile_visitor_old"), true);
                    }
                }

                level.playSound(null, blockPos, mtr.SoundEvents.TICKET_BARRIER, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(blockPos, state.setValue(OPEN, true));
                if (!level.getBlockTicks().hasScheduledTick(blockPos, this)) {
                    Utilities.scheduleBlockTick(level, blockPos, this, 40);
                }

                if (!mainBlockPos.equals(blockPos) && blockEntity.isActive) {
                    level.sendBlockUpdated(mainBlockPos, mainBlockState, mainBlockState, Block.UPDATE_CLIENTS);
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
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            if (!player.isCreative()) return InteractionResult.PASS;
            BlockPos mainBlockPos = searchMainBlock(level, blockPos);
            BlockState mainBlockState = level.getBlockState(mainBlockPos);
            BlockEntityStatisticTurnstile blockEntity = (BlockEntityStatisticTurnstile) level.getBlockEntity(mainBlockPos);
            assert blockEntity != null;
            if (player.isHolding(mtr.Items.BRUSH.get())) {
                if (blockEntity.isActive) {
                    blockEntity.isActive = false;
                    blockEntity.visitorCount = 0;
                    blockEntity.visitCount = 0;
                    blockEntity.readOffset = 0;
                    blockEntity.visitors = new HashMap<>();
                    player.displayClientMessage(new TranslatableComponent("gui.mtrsteamloco.statistic_turnstile_deactivated"), true);
                } else {
                    blockEntity.isActive = true;
                    player.displayClientMessage(new TranslatableComponent("gui.mtrsteamloco.statistic_turnstile_activated"), true);
                }
            } else {
                final int PAGE_SIZE = 20;
                StringBuilder sb = new StringBuilder();
                for (int i = blockEntity.readOffset; i < Math.min(blockEntity.visitorCount, blockEntity.readOffset + PAGE_SIZE); ++i) {
                    HashMap.Entry<String, Integer> entry = blockEntity.visitors.entrySet().stream().skip(i).findFirst().orElseThrow();
                    if (i != blockEntity.readOffset) sb.append(", ");
                    sb.append(entry.getKey()).append("[").append(entry.getValue()).append("]");
                }
                player.sendMessage(new TranslatableComponent("gui.mtrsteamloco.statistic_view",
                        sb.toString(), blockEntity.readOffset / PAGE_SIZE + 1, blockEntity.visitorCount / PAGE_SIZE + 1,
                        blockEntity.visitorCount, blockEntity.visitCount), Util.NIL_UUID);
                blockEntity.readOffset += PAGE_SIZE;
                if (blockEntity.readOffset >= blockEntity.visitorCount) blockEntity.readOffset = 0;
            }
            blockEntity.setChanged();
            level.sendBlockUpdated(mainBlockPos, mainBlockState, mainBlockState, Block.UPDATE_CLIENTS);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public BlockEntityMapper createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntityStatisticTurnstile(blockPos, blockState);
    }

    public static class BlockEntityStatisticTurnstile extends BlockEntityMapper {

        public int visitCount = 0;
        public int visitorCount = 0;
        public HashMap<String, Integer> visitors;

        public int readOffset = 0;

        public boolean isActive = true;

        public BlockEntityStatisticTurnstile(BlockPos pos, BlockState state) {
            super(Main.BLOCK_ENTITY_TYPE_STATISTIC_TURNSTILE.get(), pos, state);
        }

        public boolean handleVisitor(Player player) {
            if (!isActive) return false;
            setChanged();
            visitCount++;
            if (visitors == null) visitors = new HashMap<>();
            String playerName = player.getGameProfile().getName();
            // String playerName = UUID.randomUUID().toString().substring(0, 8);
            if (!visitors.containsKey(playerName)) {
                visitorCount++;
                visitors.put(playerName, 1);
                return true;
            } else {
                visitors.put(playerName, visitors.get(playerName) + 1);
                return false;
            }
        }

        @Override
        public void readCompoundTag(CompoundTag compoundTag) {
            visitCount = compoundTag.getInt("visitCount");
            visitorCount = compoundTag.getInt("visitorCount");
            readOffset = compoundTag.getInt("readOffset");
            isActive = compoundTag.getBoolean("isActive");
            if (compoundTag.contains("visitors")) {
                try {
                    visitors = SerializationUtils.deserialize(compoundTag.getByteArray("visitors"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    visitors = null;
                }
            }
        }

        @Override
        public void writeCompoundTag(CompoundTag compoundTag) {
            if (visitors != null) {
                compoundTag.putByteArray("visitors", SerializationUtils.serialize(visitors));
                visitorCount = visitors.size();
            }
            compoundTag.putInt("visitCount", visitCount);
            compoundTag.putInt("visitorCount", visitorCount);
            compoundTag.putInt("readOffset", readOffset);
            compoundTag.putBoolean("isActive", isActive);
        }

        @Override
        public CompoundTag getUpdateTag() {
            CompoundTag compoundTag = super.getUpdateTag();
            compoundTag.putInt("visitCount", visitCount);
            compoundTag.putInt("visitorCount", visitorCount);
            compoundTag.putInt("readOffset", readOffset);
            compoundTag.putBoolean("isActive", isActive);
            return compoundTag;
        }

        @Override
        public Packet<ClientGamePacketListener> getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }
    }
}
