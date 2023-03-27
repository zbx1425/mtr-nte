package cn.zbx1425.mtrsteamloco.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

#if MC_VERSION >= "11903"
    @Inject(method = "setShaderLights", at = @At("HEAD"))
    private static void setShaderLights(org.joml.Vector3f vector3f, org.joml.Vector3f vector3f2, CallbackInfo ci) {
        RenderSystem.getModelViewMatrix().transformDirection(vector3f);
        RenderSystem.getModelViewMatrix().transformDirection(vector3f2);
    }
#else
    @Inject(method = "setShaderLights", at = @At("HEAD"))
    private static void setShaderLights(com.mojang.math.Vector3f vector3f, com.mojang.math.Vector3f vector3f2, CallbackInfo ci) {
        com.mojang.math.Matrix3f rotMatrix = new com.mojang.math.Matrix3f(RenderSystem.getModelViewMatrix());
        vector3f.transform(rotMatrix);
        vector3f2.transform(rotMatrix);
    }
#endif
}
