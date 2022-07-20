package cn.zbx1425.mtrsteamloco.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(mtr.data.Rail.class)
public class RailMixin {

    @Redirect(method = "renderSegment", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/Math;round(D)J"))
    private long redirectRenderSegmentRound(double r) {
        return Math.round(r) * 2;
    }
}
