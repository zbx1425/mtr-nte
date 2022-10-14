package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import mtr.data.TrainClient;
import mtr.render.JonModelTrainRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = JonModelTrainRenderer.class, remap = false)
public class JonModelTrainRendererMixin {

    @Shadow @Final private TrainClient train;

    @Inject(method = "renderCar", at = @At("HEAD"), remap = false)
    public void renderCar(int par1, double par2, double par3, double par4, float par5, float par6, boolean par7, float par8, float par9, boolean par10, boolean par11, CallbackInfo ci) {
        if (RenderUtil.shouldSkipRenderTrain(train)) ci.cancel();
    }

    @Inject(method = "renderConnection", at = @At("HEAD"))
    public void renderConnection(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        if (RenderUtil.shouldSkipRenderTrain(train)) ci.cancel();
    }

    @Inject(method = "renderBarrier", at = @At("HEAD"), cancellable = true)
    public void renderBarrier(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        if (RenderUtil.shouldSkipRenderTrain(train)) ci.cancel();
    }
}
