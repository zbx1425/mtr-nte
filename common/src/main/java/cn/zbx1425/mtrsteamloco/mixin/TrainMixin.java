package cn.zbx1425.mtrsteamloco.mixin;

import mtr.data.Depot;
import mtr.data.Train;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Train.class)
public class TrainMixin {

    @Inject(method = "simulateTrain", at = @At("HEAD"), remap = false)
    public void simulateTrain(Level world, float ticksElapsed, Depot depot, CallbackInfo ci) {
        Train instance = (Train)(Object)this;
        if (instance.isCurrentlyManual()) {
            TrainAccessor accessor = (TrainAccessor) this;
            accessor.setNextStoppingIndex(accessor.getDistances().size() - 1);
        }
    }

}
