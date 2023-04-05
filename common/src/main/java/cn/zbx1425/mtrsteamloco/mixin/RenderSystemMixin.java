package cn.zbx1425.mtrsteamloco.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

#if MC_VERSION >= "11903"

    @Shadow @Final
    private static org.joml.Vector3f[] shaderLightDirections;

    @Inject(method = "setShaderLights", at = @At("TAIL"))
    private static void setShaderLights(org.joml.Vector3f vector3f, org.joml.Vector3f vector3f2, CallbackInfo ci) {
        shaderLightDirections[0] = new org.joml.Vector3f(shaderLightDirections[0]);
        RenderSystem.getModelViewMatrix().transformDirection(shaderLightDirections[0]);
        shaderLightDirections[1] = new org.joml.Vector3f(shaderLightDirections[1]);
        RenderSystem.getModelViewMatrix().transformDirection(shaderLightDirections[1]);
    }
#else

    @Shadow @Final
    private static com.mojang.math.Vector3f[] shaderLightDirections;

    @Inject(method = "setShaderLights", at = @At("TAIL"))
    private static void setShaderLights(com.mojang.math.Vector3f vector3f, com.mojang.math.Vector3f vector3f2, CallbackInfo ci) {
        com.mojang.math.Matrix3f rotMatrix = new com.mojang.math.Matrix3f(RenderSystem.getModelViewMatrix());
        shaderLightDirections[0] = shaderLightDirections[0].copy();
        shaderLightDirections[0].transform(rotMatrix);
        shaderLightDirections[1] = shaderLightDirections[1].copy();
        shaderLightDirections[1].transform(rotMatrix);
    }
#endif
}
