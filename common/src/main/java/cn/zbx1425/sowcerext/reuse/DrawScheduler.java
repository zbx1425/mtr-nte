package cn.zbx1425.sowcerext.reuse;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.shader.ShaderManager;
import cn.zbx1425.sowcer.util.GlStateTracker;
import cn.zbx1425.sowcer.util.Profiler;
import cn.zbx1425.sowcerext.model.ModelCluster;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DrawScheduler {

    public final BatchManager batchManager = new BatchManager();
    public final ShaderManager shaderManager = new ShaderManager();

    private final List<DrawCallCluster> drawCalls = new LinkedList<>();

    public void reloadShaders(ResourceManager resourceManager) throws IOException {
        shaderManager.reloadShaders(resourceManager);
    }

    public void enqueue(ModelCluster model, Matrix4f pose, int light) {
        drawCalls.add(new DrawCallCluster(model, pose, light));
    }

    public void commit(MultiBufferSource vertexConsumers, boolean isOptimized, Profiler profiler) {
        if (isOptimized && !shaderManager.isReady()) return;
        if (drawCalls.size() < 1) return;
        for (DrawCallCluster drawCall : drawCalls) {
            if (isOptimized) {
                drawCall.model.renderOpaqueOptimized(batchManager, drawCall.pose, drawCall.light, profiler);
            } else {
                drawCall.model.renderOpaqueUnoptimized(vertexConsumers, drawCall.pose, drawCall.light, profiler);
            }
        }
        if (isOptimized) {
            GlStateTracker.capture();
            commitRaw(profiler);
            GlStateTracker.restore();
        }
        for (DrawCallCluster drawCall : drawCalls) {
            drawCall.model.renderTranslucent(vertexConsumers, drawCall.pose, drawCall.light, profiler);
        }
        drawCalls.clear();
    }

    public void commitRaw(Profiler profiler) {
        batchManager.drawAll(shaderManager, profiler);
    }

    private static class DrawCallCluster {
        public ModelCluster model;
        public Matrix4f pose;
        public int light;

        public DrawCallCluster(ModelCluster model, Matrix4f pose, int light) {
            this.model = model;
            this.pose = pose;
            this.light = light;
        }
    }
}
