package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.util.GLStateCapture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import mtr.data.Rail;
import mtr.data.RailType;
import mtr.data.TransportMode;
import mtr.entity.EntitySeat;
import mtr.render.RenderTrains;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(mtr.render.RenderTrains.class)
public class RenderTrainsMixin {

    private static final GLStateCapture glState = new GLStateCapture();

    @Inject(at = @At("TAIL"), remap = false,
            method = "render(Lmtr/entity/EntitySeat;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
    private static void render(EntitySeat entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (MainClient.shaderManager.isReady()) {
            glState.capture();
            Matrix4f viewMatrix = matrices.last().pose();
            MainClient.railRenderDispatcher.updateAndEnqueueAll(Minecraft.getInstance().level, MainClient.batchManager, viewMatrix);
            MainClient.batchManager.drawAll(MainClient.shaderManager);
            glState.restore();
        }
    }

    @Inject(at = @At("HEAD"), remap = false, cancellable = true,
            method = "renderRailStandard(Lnet/minecraft/world/level/Level;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lmtr/data/Rail;FZFLjava/lang/String;FFFF)V")
    private static void renderRailStandard(Level world, PoseStack matrices, MultiBufferSource vertexConsumers, Rail rail, float yOffset, boolean renderColors, float railWidth, String texture, float u1, float v1, float u2, float v2, CallbackInfo ci) {
        if (rail.transportMode == TransportMode.TRAIN && rail.railType != RailType.NONE) {
            MainClient.railRenderDispatcher.registerRail(rail);
            ci.cancel();
        }
    }

    @Redirect(method = "lambda$renderRailStandard$13", remap = false, at = @At(value = "INVOKE", target = "Lmtr/render/RenderTrains;shouldNotRender(Lnet/minecraft/core/BlockPos;ILnet/minecraft/core/Direction;)Z"))
    private static boolean shouldNotRender(BlockPos pos, int maxDistance, Direction facing) {
        return false;
    }
}
