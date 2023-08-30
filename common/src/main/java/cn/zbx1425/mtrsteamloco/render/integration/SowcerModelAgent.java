package cn.zbx1425.mtrsteamloco.render.integration;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.mappings.ModelDataWrapper;
import mtr.mappings.ModelMapper;

public class SowcerModelAgent extends ModelMapper {

    public final ModelCluster uploadedModel;

    private final boolean bbCoords;

    public SowcerModelAgent(RawModel rawModel, boolean bbCoords) {
        super(new ModelDataWrapper(null, 0, 0));
        this.uploadedModel = MainClient.modelManager.uploadVertArrays(rawModel);
        this.bbCoords = bbCoords;
    }

    @Override
    public void render(PoseStack matrices, VertexConsumer vertices, float x, float y, float z, float rotateY, int light, int overlay) {
        Matrix4f partPose = new Matrix4f(matrices.last().pose()).copy();
        if (!bbCoords) partPose.rotateX((float) Math.PI); // Undo MTR's blockbench compatibility rotation
        Matrix4f localPose = Matrix4f.translation(x / 16f, y / 16f, z / 16f);
        localPose.rotateY(rotateY);
        partPose.multiply(localPose);
        MainClient.drawScheduler.enqueue(uploadedModel, partPose, light);
    }


    @Override
    public void render(PoseStack matrices, VertexConsumer vertices, float x, float z, float rotateY, int light, int overlay) {
        this.render(matrices, vertices, x, 0.0F, z, rotateY, light, overlay);
    }
}
