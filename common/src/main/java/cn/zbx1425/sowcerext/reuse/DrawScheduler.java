package cn.zbx1425.sowcerext.reuse;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.shader.ShaderManager;
import cn.zbx1425.sowcer.util.GlStateTracker;
import cn.zbx1425.sowcer.util.Profiler;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.integration.BufferSourceProxy;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DrawScheduler {

    public final BatchManager batchManager = new BatchManager();
    public final ShaderManager shaderManager = new ShaderManager();

    private final List<ClusterDrawCall> drawCalls = new LinkedList<>();

    public void reloadShaders(ResourceManager resourceManager) throws IOException {
        shaderManager.reloadShaders(resourceManager);
    }

    public void enqueue(ModelCluster model, Matrix4f pose, int light) {
        drawCalls.add(new ClusterDrawCall(model, pose, light));
    }

    public void commit(BufferSourceProxy vertexConsumers, boolean isOptimized, boolean sortTranslucent, Profiler profiler) {
        if (isOptimized && !shaderManager.isReady()) return;
        if (drawCalls.isEmpty()) return;
        if (isOptimized) {
            for (ClusterDrawCall drawCall : drawCalls)
                drawCall.model.enqueueOpaqueGl(batchManager, drawCall.pose, drawCall.light, profiler);
        } else {
            for (ClusterDrawCall drawCall : drawCalls)
                drawCall.model.enqueueOpaqueBlaze(vertexConsumers, drawCall.pose, drawCall.light, profiler);
        }
        if (isOptimized && sortTranslucent) {
            for (ClusterDrawCall drawCall : drawCalls)
                drawCall.model.enqueueTranslucentBlaze(vertexConsumers, drawCall.pose, drawCall.light, profiler);
        } else {
            for (ClusterDrawCall drawCall : drawCalls)
                drawCall.model.enqueueTranslucentGl(batchManager, drawCall.pose, drawCall.light, profiler);
        }
        if (isOptimized) {
            GlStateTracker.capture();
            commitRaw(profiler);
            GlStateTracker.restore();
        }
        drawCalls.clear();
    }

    public void commitRaw(Profiler profiler) {
        batchManager.drawAll(shaderManager, profiler);
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
