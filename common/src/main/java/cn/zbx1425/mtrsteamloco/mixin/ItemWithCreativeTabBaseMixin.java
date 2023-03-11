package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.data.RailExtraSupplier;
import cn.zbx1425.mtrsteamloco.gui.BrushEditRailScreen;
import cn.zbx1425.mtrsteamloco.network.PacketScreen;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateRail;
import cn.zbx1425.mtrsteamloco.render.RailPicker;
import mtr.item.ItemWithCreativeTabBase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemWithCreativeTabBase.class)
public abstract class ItemWithCreativeTabBaseMixin extends Item {

    public ItemWithCreativeTabBaseMixin(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState blockState = level.getBlockState(context.getClickedPos());
        if (blockState.getBlock() instanceof mtr.block.BlockNode) {
            if (context.isSecondaryUseActive()) {
                if (level.isClientSide) return super.useOn(context);
                PacketScreen.sendBlockEntityScreenS2C((ServerPlayer)context.getPlayer(), "brush_edit_rail", BlockPos.ZERO);
            } else {
                if (!level.isClientSide) return super.useOn(context);
                CompoundTag railBrushProp = context.getPlayer().getMainHandItem().getTagElement("NTERailBrush");
                BrushEditRailScreen.applyBrushToPickedRail(railBrushProp);
            }
            return InteractionResult.SUCCESS;
        } else {
            return super.useOn(context);
        }
    }
}
