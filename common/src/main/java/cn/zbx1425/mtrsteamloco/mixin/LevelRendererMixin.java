package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.MainClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow @Final private RenderBuffers renderBuffers;

    @Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=destroyProgress", ordinal = 0))
#if MC_VERSION >= "11903"
    private void afterBlockEntities(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, org.joml.Matrix4f matrix4f, CallbackInfo ci) {
#else
    private void afterBlockEntities(PoseStack matrices, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, com.mojang.math.Matrix4f matrix4f, CallbackInfo ci) {
#endif
        MainClient.drawScheduler.commit(renderBuffers.bufferSource(), ClientConfig.useRenderOptimization(), MainClient.profiler);
    }
}
