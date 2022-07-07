package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.GameRenderer.class)
public class GameRendererMixin {

    @Inject(at = @At("TAIL"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    private void renderLevel(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        /*if (MainClient.shaderManager.isReady()) {
            MainClient.batchManager.drawAll(MainClient.shaderManager);
        }*/
    }
}
