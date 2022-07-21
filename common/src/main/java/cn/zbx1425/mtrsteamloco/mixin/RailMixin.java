package cn.zbx1425.mtrsteamloco.mixin;

import mtr.data.Rail;
import mtr.data.RailType;
import mtr.data.TransportMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(mtr.data.Rail.class)
public class RailMixin {

    @Redirect(method = "renderSegment", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/Math;round(D)J"))
    private long redirectRenderSegmentRound(double r) {
        Rail instance = (Rail)(Object)this;
        if (instance.transportMode == TransportMode.TRAIN && instance.railType != RailType.NONE) {
            return Math.round(r) * 2;
        } else {
            return Math.round(r);
        }
    }
}
