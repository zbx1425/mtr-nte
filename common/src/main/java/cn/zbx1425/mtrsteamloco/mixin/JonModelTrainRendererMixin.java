package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import mtr.data.TrainClient;
import mtr.render.JonModelTrainRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = JonModelTrainRenderer.class, remap = false)
public class JonModelTrainRendererMixin {

    @Shadow @Final private TrainClient train;

    @Inject(method = "renderCar", at = @At("HEAD"), remap = false, cancellable = true)
    public void renderCar(int carIndex, double x, double y, double z, float yaw, float pitch, boolean doorLeftOpen, boolean doorRightOpen, CallbackInfo ci) {
        if (RenderUtil.shouldSkipRenderTrain(train)) ci.cancel();
    }

    @Inject(method = "renderConnection", at = @At("HEAD"), cancellable = true)
    public void renderConnection(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        if (RenderUtil.shouldSkipRenderTrain(train)) ci.cancel();
    }

    @Inject(method = "renderBarrier", at = @At("HEAD"), cancellable = true)
    public void renderBarrier(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        if (RenderUtil.shouldSkipRenderTrain(train)) ci.cancel();
    }
}
