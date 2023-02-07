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

    private final List<ClusterDrawCall> drawCalls = new LinkedList<>();

    private Runnable immediateDrawCall;

    public void reloadShaders(ResourceManager resourceManager) throws IOException {
        shaderManager.reloadShaders(resourceManager);
    }

    public void enqueue(ModelCluster model, Matrix4f pose, int light) {
        drawCalls.add(new ClusterDrawCall(model, pose, light));
    }

    public void commit(MultiBufferSource vertexConsumers, boolean isOptimized, Profiler profiler) {
        if (isOptimized && !shaderManager.isReady()) return;
        // if (drawCalls.size() < 1) return;
        for (ClusterDrawCall drawCall : drawCalls) {
            if (isOptimized && drawCall.model.isUploaded()) {
                drawCall.model.renderOpaqueOptimized(batchManager, drawCall.pose, drawCall.light, profiler);
            } else {
                drawCall.model.renderOpaqueUnoptimized(vertexConsumers, drawCall.pose, drawCall.light, profiler);
            }
        }
        // if (isOptimized) {
        GlStateTracker.capture();
        commitRaw(profiler);
        if (immediateDrawCall != null) immediateDrawCall.run();
        GlStateTracker.restore();
        // }
        for (ClusterDrawCall drawCall : drawCalls) {
            drawCall.model.renderTranslucent(vertexConsumers, drawCall.pose, drawCall.light, profiler);
        }
        drawCalls.clear();
    }

    public void commitRaw(Profiler profiler) {
        batchManager.drawAll(shaderManager, profiler);
    }

    public void setImmediateDrawCall(Runnable runnable) {
        this.immediateDrawCall = runnable;
    }

    private static class ClusterDrawCall {
        public ModelCluster model;
        public Matrix4f pose;
        public int light;

        public ClusterDrawCall(ModelCluster model, Matrix4f pose, int light) {
            this.model = model;
            this.pose = pose;
            this.light = light;
        }
    }
}
