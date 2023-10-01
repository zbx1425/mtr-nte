package cn.zbx1425.mtrsteamloco.render.block;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import cn.zbx1425.mtrsteamloco.data.EyeCandyProperties;
import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.render.rail.RailRenderDispatcher;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptContextManager;
import cn.zbx1425.mtrsteamloco.render.scripting.eyecandy.EyeCandyScriptContext;
import cn.zbx1425.mtrsteamloco.render.scripting.train.ScriptedTrainRenderer;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.PoseStackUtil;
import cn.zbx1425.sowcerext.model.ModelCluster;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.RegistryObject;
import mtr.block.IBlock;
import mtr.client.ClientData;
import mtr.data.TrainClient;
import mtr.mappings.BlockEntityRendererMapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
#if MC_VERSION >= "11904"
import net.minecraft.world.item.ItemDisplayContext;
#else
import net.minecraft.client.renderer.block.model.ItemTransforms;
#endif
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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
        Matrix4f candyPose = new Matrix4f(matrices.last().pose()).copy();

        EyeCandyProperties prop = EyeCandyRegistry.getProperty(blockEntity.prefabId);
        if (prop == null || RailRenderDispatcher.isHoldingBrush) {
            matrices.pushPose();
            matrices.translate(0.5f, 0.5f, 0.5f);
            PoseStackUtil.rotY(matrices, (float) ((System.currentTimeMillis() % 1000) * (Math.PI * 2 / 1000)));
#if MC_VERSION >= "11904"
            if (blockEntity.prefabId != null && prop == null) {
                Minecraft.getInstance().getItemRenderer().renderStatic(BARRIER_ITEM_STACK.get(), ItemDisplayContext.GROUND, lightToUse, 0, matrices, vertexConsumers, world, 0);
            } else {
                Minecraft.getInstance().getItemRenderer().renderStatic(BRUSH_ITEM_STACK.get(), ItemDisplayContext.GROUND, lightToUse, 0, matrices, vertexConsumers, world, 0);
            }
#else
            if (blockEntity.prefabId != null && prop == null) {
                Minecraft.getInstance().getItemRenderer().renderStatic(BARRIER_ITEM_STACK.get(), ItemTransforms.TransformType.GROUND, lightToUse, 0, matrices, vertexConsumers, 0);
            } else {
                Minecraft.getInstance().getItemRenderer().renderStatic(BRUSH_ITEM_STACK.get(), ItemTransforms.TransformType.GROUND, lightToUse, 0, matrices, vertexConsumers, 0);
            }
#endif
            matrices.popPose();
        }
        if (prop == null) return;

        final BlockPos pos = blockEntity.getBlockPos();
        final Direction facing = IBlock.getStatePropertySafe(world, pos, BlockEyeCandy.FACING);
        candyPose.translate(0.5f, 0f, 0.5f);
        candyPose.translate(blockEntity.translateX, blockEntity.translateY, blockEntity.translateZ);
        candyPose.rotateY(-(float)Math.toRadians(facing.toYRot()) + (float)(Math.PI));
        candyPose.rotateX(blockEntity.rotateX);
        candyPose.rotateY(blockEntity.rotateY);
        candyPose.rotateZ(blockEntity.rotateZ);
        if (prop.model != null) {
            MainClient.drawScheduler.enqueue(prop.model, candyPose, lightToUse);
        }
        if (prop.script != null) {
            synchronized (blockEntity.scriptContext) {
                blockEntity.scriptContext.scriptResult.commit(MainClient.drawScheduler, candyPose, lightToUse);
            }
            prop.script.tryCallRenderFunctionAsync(blockEntity.scriptContext);
        }
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
