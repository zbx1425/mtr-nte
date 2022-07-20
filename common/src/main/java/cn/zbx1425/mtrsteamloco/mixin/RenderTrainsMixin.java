package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.data.Rail;
import mtr.entity.EntitySeat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(mtr.render.RenderTrains.class)
public class RenderTrainsMixin {

    private static final VertAttrState ATTR_DISABLE_COLOR = new VertAttrState().setColor(-1);

    @Inject(at = @At("TAIL"), remap = false,
            method = "render(Lmtr/entity/EntitySeat;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
    private static void render(EntitySeat entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (MainClient.shaderManager.isReady()) {
            MainClient.railRenderDispatcher.renderAll(Minecraft.getInstance().level, MainClient.batchManager, new EnqueueProp(null));
            MainClient.batchManager.drawAll(MainClient.shaderManager);
        }
    }

    @Inject(at = @At("HEAD"), remap = false, cancellable = true,
            method = "renderRailStandard(Lnet/minecraft/world/level/Level;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lmtr/data/Rail;FZFLjava/lang/String;FFFF)V")
    private static void renderRailStandard(Level world, PoseStack matrices, MultiBufferSource vertexConsumers, Rail rail, float yOffset, boolean renderColors, float railWidth, String texture, float u1, float v1, float u2, float v2, CallbackInfo ci) {
        MainClient.railRenderDispatcher.registerRail(rail);
        ci.cancel();
    }
}
