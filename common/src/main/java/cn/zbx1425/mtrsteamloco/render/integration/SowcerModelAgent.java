package cn.zbx1425.mtrsteamloco.render.integration;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.util.AttrUtil;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import mtr.mappings.ModelDataWrapper;
import mtr.mappings.ModelMapper;

public class SowcerModelAgent extends ModelMapper {

    public final RawModel rawModel;
    public final ModelCluster uploadedModel;

    public SowcerModelAgent(RawModel rawModel) {
        super(new ModelDataWrapper(null, 0, 0));
        this.rawModel = rawModel;
        this.uploadedModel = MainClient.modelManager.uploadVertArrays(rawModel);
    }

    @Override
    public void render(PoseStack matrices, VertexConsumer vertices, float x, float y, float z, float rotateY, int light, int overlay) {
        Matrix4f partPose = new Matrix4f(matrices.last().pose().copy());
        partPose.rotateX((float) Math.PI); // Undo MTR's blockbench compatibility rotation
        Matrix4f localPose = new Matrix4f();
        localPose.translate(x / 16f, y / 16f, z / 16f);
        localPose.rotateY(rotateY);
        partPose.multiply(localPose);

        if (ClientConfig.getTrainRenderLevel() == RenderUtil.LEVEL_SOWCER) {
            uploadedModel.renderOptimized(MainClient.batchManager, RenderUtil.commonVertexConsumers, partPose, light);
        } else if (ClientConfig.getTrainRenderLevel() == RenderUtil.LEVEL_BLAZE) {
            uploadedModel.renderUnoptimized(RenderUtil.commonVertexConsumers, partPose, light);
        }
    }


    @Override
    public void render(PoseStack matrices, VertexConsumer vertices, float x, float z, float rotateY, int light, int overlay) {
        this.render(matrices, vertices, x, 0.0F, z, rotateY, light, overlay);
    }
}
