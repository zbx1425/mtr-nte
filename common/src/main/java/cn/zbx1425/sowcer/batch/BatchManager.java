package cn.zbx1425.sowcer.batch;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.VertArray;
import cn.zbx1425.sowcer.shader.ShaderManager;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class BatchManager {

    public HashMap<BatchTuple, Queue<RenderCall>> batches = new HashMap<>();

    public int drawCallCount = 0;
    public int batchCount = 0;
    public int faceCount = 0;

    public void enqueue(VertArrays model, EnqueueProp enqueueProp, ShaderProp shaderProp) {
        for (VertArray vertArray : model.meshList) {
            enqueue(vertArray, enqueueProp, shaderProp);
        }
    }

    public void enqueue(VertArray vertArray, EnqueueProp enqueueProp, ShaderProp shaderProp) {
        Queue<RenderCall> queue = batches.computeIfAbsent(
                new BatchTuple(vertArray.materialProp, shaderProp),
                (key) -> new LinkedList<>()
        );
        queue.add(new RenderCall(vertArray, enqueueProp));
    }

    public void drawAll(ShaderManager shaderManager) {
        faceCount = 0;
        drawCallCount = 0;
        batchCount = batches.size();

        for (Map.Entry<BatchTuple, Queue<RenderCall>> entry : batches.entrySet()) {
            if (entry.getKey().materialProp.translucent || entry.getKey().materialProp.cutoutHack) continue;
            drawBatch(shaderManager, entry);
        }

        for (Map.Entry<BatchTuple, Queue<RenderCall>> entry : batches.entrySet()) {
            if (!entry.getKey().materialProp.cutoutHack) continue;
            drawBatch(shaderManager, entry);
        }

        for (Map.Entry<BatchTuple, Queue<RenderCall>> entry : batches.entrySet()) {
            if (!entry.getKey().materialProp.translucent) continue;
            drawBatch(shaderManager, entry);
        }

        batches.clear();
    }

    private void drawBatch(ShaderManager shaderManager, Map.Entry<BatchTuple, Queue<RenderCall>> entry) {
        shaderManager.setupShaderBatchState(entry.getKey().materialProp, entry.getKey().shaderProp);
        Queue<RenderCall> queue = entry.getValue();
        while (!queue.isEmpty()) {
            RenderCall renderCall = queue.poll();
            renderCall.draw();
            drawCallCount++;
            faceCount += renderCall.vertArray.faceCount;
        }
    }

    private static class BatchTuple {

        public MaterialProp materialProp;
        public ShaderProp shaderProp;

        public BatchTuple(MaterialProp materialProp, ShaderProp shaderProp) {
            this.materialProp = materialProp;
            this.shaderProp = shaderProp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BatchTuple that = (BatchTuple) o;
            return materialProp.equals(that.materialProp) && shaderProp.equals(that.shaderProp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(materialProp, shaderProp);
        }
    }

    private static class RenderCall {

        public VertArray vertArray;
        public EnqueueProp enqueueProp;

        public RenderCall(VertArray vertArray, EnqueueProp enqueueProp) {
            this.vertArray = vertArray;
            this.enqueueProp = enqueueProp;
        }

        public void draw() {
            vertArray.bind();
            if (enqueueProp.attrState != null) enqueueProp.attrState.apply(vertArray.materialProp);
            if (vertArray.materialProp.attrState != null) vertArray.materialProp.attrState.apply(vertArray.materialProp);
            vertArray.draw();
        }
    }
}
