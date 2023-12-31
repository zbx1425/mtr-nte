package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcerext.model.integration.BufferSourceProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
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

@Mixin(value = LevelRenderer.class, priority = 100)
public class LevelRendererMixin {

    @Shadow @Final private RenderBuffers renderBuffers;

    @Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=destroyProgress", ordinal = 0))
#if MC_VERSION >= "11903"
    private void afterBlockEntities(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, org.joml.Matrix4f matrix4f, CallbackInfo ci) {
#else
    private void afterBlockEntities(PoseStack matrices, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, com.mojang.math.Matrix4f matrix4f, CallbackInfo ci) {
#endif
        Minecraft.getInstance().level.getProfiler().popPush("NTEBlockEntities");
        BufferSourceProxy vertexConsumersProxy = new BufferSourceProxy(renderBuffers.bufferSource());
        MainClient.drawScheduler.commit(vertexConsumersProxy, MainClient.drawContext);
        vertexConsumersProxy.commit();
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
#if MC_VERSION >= "11903"
    private void renderLevelLast(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, org.joml.Matrix4f matrix4f, CallbackInfo ci) {
#else
    private void renderLevelLast(PoseStack matrices, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, com.mojang.math.Matrix4f matrix4f, CallbackInfo ci) {
#endif
        MainClient.drawContext.resetFrameProfiler();
    }

    // Sodium applies @Overwrite to them so have to inject them all, rather than just setSectionDirty(IIIZ)
    // TODO Will it include unnecessary updates?

    @Inject(method = "setSectionDirtyWithNeighbors", at = @At("HEAD"))
    private void setSectionDirtyWithNeighbors(int sectionX, int sectionY, int sectionZ, CallbackInfo ci) {
        for (int i = sectionZ - 1; i <= sectionZ + 1; ++i) {
            for (int j = sectionX - 1; j <= sectionX + 1; ++j) {
                MainClient.railRenderDispatcher.registerLightUpdate(j, sectionY - 1, sectionY + 1, i);
            }
        }
    }

    @Inject(method = "setSectionDirty(IIIZ)V", at = @At("HEAD"))
    private void setSectionDirty(int sectionX, int sectionY, int sectionZ, boolean reRenderOnMainThread, CallbackInfo ci) {
        MainClient.railRenderDispatcher.registerLightUpdate(sectionX, sectionY, sectionY, sectionZ);
    }

    @Inject(method = "setSectionDirty(III)V", at = @At("HEAD"))
    private void setSectionDirty(int sectionX, int sectionY, int sectionZ, CallbackInfo ci) {
        MainClient.railRenderDispatcher.registerLightUpdate(sectionX, sectionY, sectionY, sectionZ);
    }

}
