package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.RailPicker;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.sowcer.util.GlStateTracker;
import com.mojang.blaze3d.vertex.PoseStack;
import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.Items;
import mtr.data.Rail;
import mtr.data.RailType;
import mtr.data.TransportMode;
import mtr.entity.EntitySeat;
import mtr.render.RenderTrains;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderTrains.class)
public class RenderTrainsMixin {

    @Inject(at = @At("HEAD"),
            method = "render(Lmtr/entity/EntitySeat;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
    private static void renderHead(EntitySeat entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        RenderUtil.commonVertexConsumers = vertexConsumers;
        MainClient.profiler.beginFrame();
    }

    @Inject(at = @At("TAIL"),
            method = "render(Lmtr/entity/EntitySeat;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
    private static void renderTail(EntitySeat entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        Matrix4f viewMatrix = new Matrix4f(matrices.last().pose());
        if (ClientConfig.getRailRenderLevel() == RenderUtil.LEVEL_SOWCER) {
            GlStateTracker.capture();
            MainClient.railRenderDispatcher.updateAndEnqueueAll(Minecraft.getInstance().level, MainClient.drawScheduler.batchManager, viewMatrix);
            GlStateTracker.restore();
        }
        MainClient.drawScheduler.commit(vertexConsumers, ClientConfig.useRenderOptimization(), MainClient.profiler);

        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isHolding(Items.BRUSH.get())) {
            RailPicker.pick();
            RailPicker.render(matrices, vertexConsumers);
        } else {
            RailPicker.pickedRail = null;
        }
    }

    @Inject(at = @At("HEAD"), cancellable = true,
            method = "renderRailStandard(Lnet/minecraft/world/level/Level;Lmtr/data/Rail;FZFLjava/lang/String;FFFF)V")
    private static void renderRailStandard(Level world, Rail rail, float yOffset, boolean renderColors, float railWidth, String texture, float u1, float v1, float u2, float v2, CallbackInfo ci) {
        if (ClientConfig.getRailRenderLevel() == RenderUtil.LEVEL_NONE) {
            ci.cancel();
            return;
        }
        if (ClientConfig.getRailRenderLevel() == RenderUtil.LEVEL_SOWCER) {
            if (rail.transportMode == TransportMode.TRAIN && rail.railType != RailType.NONE) {
                    MainClient.railRenderDispatcher.registerRail(rail);
                    ci.cancel();
            }
        }
    }

}
