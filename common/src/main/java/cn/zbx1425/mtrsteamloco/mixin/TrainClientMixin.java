package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.data.DisplayRegistry;
import mtr.data.TrainClient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrainClient.class)
public class TrainClientMixin {

    @Inject(method = "simulateCar", at = @At(value = "INVOKE", target = "Lmtr/render/TrainRendererBase;renderCar(IDDDFFZZ)V"))
    private void simulateCar(Level world, int ridingCar, float ticksElapsed,
                             double carX, double carY, double carZ, float carYaw, float carPitch,
                             double prevCarX, double prevCarY, double prevCarZ, float prevCarYaw, float prevCarPitch,
                             boolean doorLeftOpen, boolean doorRightOpen, double realSpacing, CallbackInfo ci) {
        DisplayRegistry.handleDraw(((TrainClient)(Object)this).trainId, ((TrainClient)(Object)this),
                ridingCar, carX, carY, carZ, carYaw, carPitch, doorLeftOpen, doorRightOpen);
    }
}
