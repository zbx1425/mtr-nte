package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.entity.EntitySeat;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(mtr.render.RenderTrains.class)
public class RenderTrainsMixin {

    @Inject(at = @At("TAIL"), remap = false,
            method = "render(Lmtr/entity/EntitySeat;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
    private static void render(EntitySeat entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (MainClient.shaderManager.isReady()) {
            MainClient.batchManager.drawAll(MainClient.shaderManager);
        }
    }
}
