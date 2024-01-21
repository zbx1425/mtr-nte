package cn.zbx1425.mtrsteamloco.mixin;

import mtr.data.Rail;
import mtr.data.RailwayData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.function.Consumer;

@Mixin(Rail.RailActions.class)
public abstract class RailActionsMixin {

    @Shadow(remap = false) protected abstract boolean create(boolean includeMiddle, Consumer<Vec3> consumer);

    @Shadow @Final private Set<BlockPos> blacklistedPos;

    @Shadow(remap = false) @Final private boolean isSlab;

    @Shadow @Final private BlockState state;

    @Shadow @Final private Level world;

    @Shadow private static BlockPos getHalfPos(BlockPos pos, boolean isTopHalf) { throw new AssertionError(); }

    @Shadow(remap = false) private static boolean canPlace(Level world, BlockPos pos) { throw new AssertionError(); }

    @Shadow(remap = false) @Final private int radius;

    /**
     * @author Zbx1425
     * @reason To speed up bridge construction
     */
    @Overwrite(remap = false)
    private boolean createBridge() {
        return this.create(false, (editPos) -> {
            BlockPos pos = RailwayData.newBlockPos(editPos);
            boolean isTopHalf = editPos.y - Math.floor(editPos.y) >= 0.5;
            BlockPos placePos;
            BlockState placeState;
            boolean placeHalf;
            if (this.isSlab && isTopHalf) {
                placePos = pos;
                placeState = this.state.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
                placeHalf = false;
            } else {
                placePos = pos.below();
                placeState = this.isSlab ? this.state.setValue(SlabBlock.TYPE, SlabType.TOP) : this.state;
                placeHalf = true;
            }
            BlockPos halfPlacePos = getHalfPos(placePos, placeHalf);
            if (!this.blacklistedPos.contains(halfPlacePos) && canPlace(this.world, placePos)) {
                this.world.setBlockAndUpdate(placePos, placeState);
                this.blacklistedPos.add(halfPlacePos);
            }
        });
    }

    @Redirect(method = "create", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(D)D"), remap = false)
    private double redirectCreateAbs(double a) {
        return radius == 0 ? -65472 : Math.abs(a);
    }

}
