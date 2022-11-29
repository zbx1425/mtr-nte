package cn.zbx1425.sowcerext.model.compat;

import cn.zbx1425.sowcerext.model.RawModel;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class RawModelBuilder implements MultiBufferSource {

    public RawModel model;

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {

        return null;
    }
}
