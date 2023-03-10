package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.network.PacketScreen;
import mtr.block.BlockNode;
import mtr.mappings.BlockDirectionalMapper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockNode.class)
public abstract class BlockNodeMixin extends BlockDirectionalMapper {

    public BlockNodeMixin(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return super.use(state, level, pos, player, hand, hit);
        if (player.getMainHandItem().is(mtr.Items.BRUSH.get())) {
            if (player.isShiftKeyDown()) {
                PacketScreen.sendBlockEntityScreenS2C((ServerPlayer)player, "brush_edit_rail", BlockPos.ZERO);
            } else {
                CompoundTag railBrushProp = player.getMainHandItem().getTagElement("NTERailBrush");
                if (railBrushProp == null) return InteractionResult.FAIL;

            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }
}
