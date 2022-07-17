package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(at = @At("TAIL"), method = "renderLevel")
    private void renderLevel(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer,
                             LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        if (MainClient.shaderManager.isReady()) {
            MainClient.batchManager.drawAll(MainClient.shaderManager);
        }
    }
}
