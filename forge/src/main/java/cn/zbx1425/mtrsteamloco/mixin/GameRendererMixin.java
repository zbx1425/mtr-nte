package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.model.ModelTrainD51;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.GameRenderer.class)
public class GameRendererMixin {

    @Inject(at = @At("TAIL"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    private void renderLevel(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        if (MainClient.shaderManager.isReady()) {
            if (ModelTrainD51.glVaos != null) {
                MainClient.batchManager.enqueue(ModelTrainD51.glVaos, new EnqueueProp(new VertAttrState()
                ), new ShaderProp().setEyeTransformInModelMatrix(false));
            }
            MainClient.batchManager.drawAll(MainClient.shaderManager);
        }
    }
}
