package cn.zbx1425.sowcerext.model.integration;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.HashMap;
import java.util.Map;

public class BufferSourceProxy {

    private final MultiBufferSource bufferSource;
    private final Map<RenderType, BufferBuilderProxy> builders = new HashMap<>();

    public BufferSourceProxy(MultiBufferSource bufferSource) {
        this.bufferSource = bufferSource;
    }

    public BufferBuilderProxy getBuffer(RenderType renderType, boolean needSorting) {
        return builders.computeIfAbsent(renderType,
                type -> new BufferBuilderProxy(bufferSource.getBuffer(renderType), needSorting));
    }

    public void commit() {
        for (BufferBuilderProxy builder : builders.values()) {
            builder.commit();
        }
        builders.clear();
    }
}
