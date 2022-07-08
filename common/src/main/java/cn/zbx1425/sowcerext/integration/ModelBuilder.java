package cn.zbx1425.sowcerext.integration;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class ModelBuilder implements MultiBufferSource {

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return null;
    }
}
