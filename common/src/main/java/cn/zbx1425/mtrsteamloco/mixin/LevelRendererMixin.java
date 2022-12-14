package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcer.util.GLStateCapture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    private static final GLStateCapture glState = new GLStateCapture();

    @Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=destroyProgress", ordinal = 0))
    private void afterBlockEntities(PoseStack matrices, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        if (MainClient.shaderManager.isReady()) {
            glState.capture();
            MainClient.batchManager.drawAll(MainClient.shaderManager, MainClient.profiler);
            glState.restore();
        }
    }
}
