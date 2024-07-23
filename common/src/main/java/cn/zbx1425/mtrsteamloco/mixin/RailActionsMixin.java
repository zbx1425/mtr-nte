package cn.zbx1425.mtrsteamloco.mixin;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import mtr.data.Rail;
import mtr.data.RailwayData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(Rail.RailActions.class)
public abstract class RailActionsMixin {

    @Shadow(remap = false) protected abstract boolean create(boolean includeMiddle, Consumer<Vec3> consumer);

    @Shadow(remap = false) @Final private Set<BlockPos> blacklistedPos;
    @Unique private final Long2IntMap nte$placedState = new Long2IntOpenHashMap();

    @Shadow(remap = false) @Final private boolean isSlab;

    @Shadow @Final private BlockState state;

    @Shadow @Final private Level world;

    @Shadow private static BlockPos getHalfPos(BlockPos pos, boolean isTopHalf) { return null; }

    @Shadow(remap = false) private static boolean canPlace(Level world, BlockPos pos) { return true; }

    @Shadow(remap = false) @Final private int radius;

    /**
     * @author Zbx1425
     * @reason To speed up bridge construction
     */
    @Overwrite(remap = false)
    private boolean createBridge() {
        return this.create(false, (editPos) -> {
            double refY = editPos.y;
            BlockPos pos = RailwayData.newBlockPos(editPos.x, refY, editPos.z);
            boolean isTopHalf = refY - Math.floor(refY) >= 0.5;

            BlockPos placePos, airPos;
            BlockState placeState;
            if (this.isSlab && isTopHalf) {
                placePos = pos;
                airPos = pos.above();
                placeState = this.state.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            } else {
                placePos = pos.below();
                airPos = pos;
                placeState = this.isSlab ? this.state.setValue(SlabBlock.TYPE, SlabType.DOUBLE) : this.state;
            }

            int slabPriority = isTopHalf ? 2 : 1;
            if (nte$placedState.get(placePos.asLong()) < slabPriority && canPlace(world, placePos)) {
                world.setBlockAndUpdate(placePos, placeState);
                nte$placedState.put(placePos.asLong(), slabPriority);
            }

            if (nte$placedState.get(airPos.asLong()) < 3 && canPlace(world, airPos)) {
                world.setBlockAndUpdate(airPos, Blocks.AIR.defaultBlockState());
                nte$placedState.put(airPos.asLong(), 3);
            }
        });
    }

    @Redirect(method = "create", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(D)D"), remap = false)
    private double redirectCreateAbs(double a) {
        return radius == 0 ? -65472 : Math.abs(a);
    }

    @ModifyConstant(method = "create", constant = @Constant(doubleValue = 0.01, ordinal = 0), remap = false)
    private double modifyCreateInterval1(double original) {
        return 0.1;
    }

    @ModifyConstant(method = "create", constant = @Constant(doubleValue = 0.01, ordinal = 3), remap = false)
    private double modifyCreateInterval2(double original) {
        return 0.1;
    }

}
