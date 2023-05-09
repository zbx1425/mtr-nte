package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import mtr.block.BlockNode;
import mtr.block.IBlock;
import mtr.data.TransportMode;
import mtr.mappings.BlockDirectionalMapper;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockNode.class)
public abstract class BlockNodeMixin extends BlockDirectionalMapper {

    private RenderShape renderShape;

    private BlockNodeMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void ctorTail(TransportMode transportMode, CallbackInfo ci) {
        if (transportMode == TransportMode.TRAIN || transportMode == TransportMode.AIRPLANE) {
            renderShape = RenderShape.INVISIBLE;
        } else {
            renderShape = RenderShape.MODEL;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        if (ClientConfig.enableRail3D && IBlock.getStatePropertySafe(state, BlockNode.IS_CONNECTED)) {
            return renderShape;
        } else {
            return RenderShape.MODEL;
        }
    }
}
