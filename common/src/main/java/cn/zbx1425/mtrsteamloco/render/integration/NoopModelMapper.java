package cn.zbx1425.mtrsteamloco.render.integration;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mtr.mappings.ModelDataWrapper;
import mtr.mappings.ModelMapper;

public class NoopModelMapper extends ModelMapper {

    public static final NoopModelMapper INSTANCE = new NoopModelMapper();

    public NoopModelMapper() {
        super(new ModelDataWrapper(null, 0, 0));
    }

    @Override
    public void render(PoseStack matrices, VertexConsumer vertices, float x, float y, float z, float rotateY, int light, int overlay) {

    }


    @Override
    public void render(PoseStack matrices, VertexConsumer vertices, float x, float z, float rotateY, int light, int overlay) {

    }
}
