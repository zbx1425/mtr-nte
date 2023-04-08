package cn.zbx1425.mtrsteamloco.render.block;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.PoseStackUtil;
import cn.zbx1425.sowcerext.model.ModelCluster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mtr.RegistryObject;
import mtr.block.IBlock;
import mtr.mappings.BlockEntityRendererMapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
#if MC_VERSION >= "11904"
import net.minecraft.world.item.ItemDisplayContext;
#else
import net.minecraft.client.renderer.block.model.ItemTransforms;
#endif
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BlockEntityEyeCandyRenderer extends BlockEntityRendererMapper<BlockEyeCandy.BlockEntityEyeCandy> {

    public BlockEntityEyeCandyRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    private static final RegistryObject<ItemStack> BRUSH_ITEM_STACK = new RegistryObject<>(() -> new ItemStack(mtr.Items.BRUSH.get(), 1));

    private static final RegistryObject<ItemStack> BARRIER_ITEM_STACK = new RegistryObject<>(() -> new ItemStack(net.minecraft.world.item.Items.BARRIER, 1));


    @Override
    public void render(BlockEyeCandy.BlockEntityEyeCandy blockEntity, float f, @NotNull PoseStack matrices, @NotNull MultiBufferSource vertexConsumers, int light, int overlay) {
        final Level world = blockEntity.getLevel();
        if (world == null) return;

        int lightToUse = blockEntity.fullLight ? LightTexture.pack(15, 15) : light;

        ModelCluster model = EyeCandyRegistry.getModel(blockEntity.prefabId);
        if (model == null || Minecraft.getInstance().player.getMainHandItem().is(mtr.Items.BRUSH.get())) {
            matrices.pushPose();
            matrices.translate(0.5f, 0.5f, 0.5f);
            PoseStackUtil.rotY(matrices, (float) ((System.currentTimeMillis() % 1000) * (Math.PI * 2 / 1000)));
#if MC_VERSION >= "11904"
            if (blockEntity.prefabId != null && model == null) {
                Minecraft.getInstance().getItemRenderer().renderStatic(BARRIER_ITEM_STACK.get(), ItemDisplayContext.GROUND, lightToUse, 0, matrices, vertexConsumers, world, 0);
            } else {
                Minecraft.getInstance().getItemRenderer().renderStatic(BRUSH_ITEM_STACK.get(), ItemDisplayContext.GROUND, lightToUse, 0, matrices, vertexConsumers, world, 0);
            }
#else
            if (blockEntity.prefabId != null && model == null) {
                Minecraft.getInstance().getItemRenderer().renderStatic(BARRIER_ITEM_STACK.get(), ItemTransforms.TransformType.GROUND, lightToUse, 0, matrices, vertexConsumers, 0);
            } else {
                Minecraft.getInstance().getItemRenderer().renderStatic(BRUSH_ITEM_STACK.get(), ItemTransforms.TransformType.GROUND, lightToUse, 0, matrices, vertexConsumers, 0);
            }
#endif
            // Minecraft.getInstance().getBlockRenderer().renderSingleBlock(mtr.Blocks.LOGO.get().defaultBlockState(), matrices, vertexConsumers, light, overlay);
            matrices.popPose();
        }
        if (model == null) return;

        final BlockPos pos = blockEntity.getBlockPos();
        final Direction facing = IBlock.getStatePropertySafe(world, pos, BlockEyeCandy.FACING);
        matrices.pushPose();
        matrices.translate(0.5f, 0f, 0.5f);
        matrices.translate(blockEntity.translateX, blockEntity.translateY, blockEntity.translateZ);
        PoseStackUtil.rotX(matrices, blockEntity.rotateX);
        PoseStackUtil.rotY(matrices, blockEntity.rotateY);
        PoseStackUtil.rotZ(matrices, blockEntity.rotateZ);
        PoseStackUtil.rotY(matrices, -(float)Math.toRadians(facing.toYRot()) + (float)(Math.PI));
        MainClient.drawScheduler.enqueue(model, new Matrix4f(matrices.last().pose()), lightToUse);

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
