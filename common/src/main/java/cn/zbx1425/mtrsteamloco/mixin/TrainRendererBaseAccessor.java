package cn.zbx1425.mtrsteamloco.mixin;

import mtr.render.TrainRendererBase;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrainRendererBase.class)
public interface TrainRendererBaseAccessor {

    @Accessor
    static MultiBufferSource getVertexConsumers() {
        return null;
    }
}
