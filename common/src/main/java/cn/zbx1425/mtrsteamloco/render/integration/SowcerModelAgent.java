package cn.zbx1425.mtrsteamloco.render.integration;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.util.AttrUtil;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcerext.model.RawModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import mtr.mappings.ModelDataWrapper;
import mtr.mappings.ModelMapper;

public class SowcerModelAgent extends ModelMapper {

    public final RawModel rawModel;
    public final VertArrays uploadedModel;

    public SowcerModelAgent(RawModel rawModel) {
        super(new ModelDataWrapper(null, 0, 0));
        this.rawModel = rawModel;
        this.uploadedModel = MainClient.modelManager.uploadVertArrays(rawModel);
    }

    @Override
    public void render(PoseStack matrices, VertexConsumer vertices, float x, float y, float z, float rotateY, int light, int overlay) {
        int shaderLightmapUV = AttrUtil.exchangeLightmapUVBits(light);
        Matrix4f partPose = matrices.last().pose().copy();
        partPose.multiply(Vector3f.XP.rotation((float) Math.PI)); // Undo MTR's blockbench compatibility rotation
        Matrix4f localPose = new Matrix4f();
        localPose.setIdentity();
        localPose.translate(new Vector3f(x / 16f, y / 16f, z / 16f));
        localPose.multiply(Vector3f.YP.rotation(rotateY));
        partPose.multiply(localPose);

        if (ClientConfig.getTrainRenderLevel() == RenderUtil.LEVEL_SOWCER) {
            MainClient.batchManager.enqueue(uploadedModel, new EnqueueProp(
                    new VertAttrState().setColor(255, 255, 255, 255).setLightmapUV(shaderLightmapUV).setModelMatrix(partPose)
            ), ShaderProp.DEFAULT);
        } else if (ClientConfig.getTrainRenderLevel() == RenderUtil.LEVEL_BLAZE) {
            rawModel.writeBlazeBuffer(RenderUtil.commonVertexConsumers, partPose, light);
        }
    }


    @Override
    public void render(PoseStack matrices, VertexConsumer vertices, float x, float z, float rotateY, int light, int overlay) {
        this.render(matrices, vertices, x, 0.0F, z, rotateY, light, overlay);
    }
}
