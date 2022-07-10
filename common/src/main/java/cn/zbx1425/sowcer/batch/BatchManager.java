package cn.zbx1425.sowcer.batch;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.VertArray;
import cn.zbx1425.sowcer.shader.ShaderManager;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import org.lwjgl.opengl.GL33;

import java.util.*;

public class BatchManager {

    public HashMap<BatchTuple, Queue<RenderCall>> batches = new HashMap<>();

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
        int vaoPrev = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING);

        for (Map.Entry<BatchTuple, Queue<RenderCall>> entry : batches.entrySet()) {
            if (entry.getKey().materialProp.translucent) continue;
            shaderManager.setupShaderBatchState(entry.getKey().materialProp, entry.getKey().shaderProp);
            Queue<RenderCall> queue = entry.getValue();
            while (!queue.isEmpty()) {
                RenderCall renderCall = queue.poll();
                renderCall.draw();
            }
        }

        for (Map.Entry<BatchTuple, Queue<RenderCall>> entry : batches.entrySet()) {
            if (!entry.getKey().materialProp.translucent) continue;
            shaderManager.setupShaderBatchState(entry.getKey().materialProp, entry.getKey().shaderProp);
            Queue<RenderCall> queue = entry.getValue();
            while (!queue.isEmpty()) {
                RenderCall renderCall = queue.poll();
                renderCall.draw();
            }
        }

        GL33.glBindVertexArray(vaoPrev);
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
            if (enqueueProp.attrState != null) enqueueProp.attrState.apply(vertArray.mapping, VertAttrSrc.ENQUEUE);
            if (vertArray.materialProp.attrState != null) vertArray.materialProp.attrState.apply(vertArray.mapping, VertAttrSrc.MATERIAL);
            vertArray.draw();
        }
    }
}
