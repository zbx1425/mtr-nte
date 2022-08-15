package cn.zbx1425.mtrsteamloco.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(value = mtr.data.TrainServer.class, remap = false)
public class TrainServerMixin {

    private long lastSimulateTrainMillis = 0;
    private float realTicksElapsed = 1;

    @ModifyVariable(method = "simulateTrain", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public float injectSimulateTrain(float ticksElapsed) {
        if (lastSimulateTrainMillis == 0) {
            realTicksElapsed = 1;
        } else {
            realTicksElapsed = (System.currentTimeMillis() - lastSimulateTrainMillis) / 50f;
        }
        lastSimulateTrainMillis = System.currentTimeMillis();
        return realTicksElapsed;
    }

    @Redirect(method = "checkBlock", at = @At(value = "INVOKE", target = "Ljava/lang/Math;floor(D)D"))
    public double redirectCheckBlockSpeedFloor(double speed) {
        return Math.floor(speed * realTicksElapsed);
    }
}
