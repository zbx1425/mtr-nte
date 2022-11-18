package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.sowcer.util.GLStateCapture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import mtr.screen.ResourcePackCreatorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResourcePackCreatorScreen.class)
public class ResourcePackCreatorScreenMixin {

    @Shadow
    private static int guiCounter;

    private static final GLStateCapture glState = new GLStateCapture();

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("TAIL"))
    private static void render(PoseStack matrices, CallbackInfo ci) {
        if (guiCounter == 0 || ClientConfig.getTrainRenderLevel() < RenderUtil.LEVEL_SOWCER) return;
        if (MainClient.shaderManager.isReady()) {
            glState.capture();
            MainClient.batchManager.drawAll(MainClient.shaderManager);
            glState.restore();
        }
    }
}
