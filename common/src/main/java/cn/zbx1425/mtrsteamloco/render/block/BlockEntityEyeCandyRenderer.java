package cn.zbx1425.mtrsteamloco.render.block;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.ShadersModHandler;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.PoseStackUtil;
import cn.zbx1425.sowcerext.model.ModelCluster;
import com.mojang.blaze3d.vertex.PoseStack;
import cn.zbx1425.sowcer.math.Vector3f;
import mtr.block.IBlock;
import mtr.mappings.BlockEntityRendererMapper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BlockEntityEyeCandyRenderer extends BlockEntityRendererMapper<BlockEyeCandy.BlockEntityEyeCandy> {

    public BlockEntityEyeCandyRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(BlockEyeCandy.BlockEntityEyeCandy blockEntity, float f, @NotNull PoseStack matrices, @NotNull MultiBufferSource vertexConsumers, int light, int overlay) {
        final Level world = blockEntity.getLevel();
        if (world == null) return;

        ModelCluster model = EyeCandyRegistry.getModel(blockEntity.prefabId);
        if (model == null) {
            return;
        }

        final BlockPos pos = blockEntity.getBlockPos();
        final Direction facing = IBlock.getStatePropertySafe(world, pos, BlockEyeCandy.FACING);
        matrices.pushPose();
        matrices.translate(0.5f, 0f, 0.5f);
        PoseStackUtil.rotY(matrices, -(float)Math.toRadians(facing.toYRot()));

        if (ClientConfig.getTrainRenderLevel() == RenderUtil.LEVEL_SOWCER) {
            model.renderOptimized(MainClient.batchManager, vertexConsumers, new Matrix4f(matrices.last().pose()), light);
        } else {
            model.renderUnoptimized(vertexConsumers, new Matrix4f(matrices.last().pose()), light);
        }

        matrices.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEyeCandy.@NotNull BlockEntityEyeCandy blockEntity) {
        return true;
    }

    @Override
    public boolean shouldRender(BlockEyeCandy.@NotNull BlockEntityEyeCandy blockEntity, @NotNull Vec3 vec3) {
        return true;
    }
}
