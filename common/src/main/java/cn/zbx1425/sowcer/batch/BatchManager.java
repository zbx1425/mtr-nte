package cn.zbx1425.sowcer.batch;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.VertArray;
import cn.zbx1425.sowcer.shader.ShaderManager;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import org.lwjgl.opengl.GL33;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class BatchManager {

    public HashMap<BatchProp, Queue<DrawCall>> batches = new HashMap<>();

    public void enqueue(VertArrays model, VertAttrState callAttrState) {
        for (VertArray vertArray : model.meshList) {
            enqueue(vertArray, callAttrState);
        }
    }

    public void enqueue(VertArray vertArray, VertAttrState callAttrState) {
        Queue<DrawCall> queue = batches.computeIfAbsent(vertArray.batchProp, (key) -> new LinkedList<>());
        queue.add(new DrawCall(vertArray, callAttrState));
    }

    public void drawAll(ShaderManager shaderManager) {
        int vaoPrev = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING);

        for (Map.Entry<BatchProp, Queue<DrawCall>> entry : batches.entrySet()) {
            shaderManager.setupShaderState(entry.getKey());
            Queue<DrawCall> queue = entry.getValue();
            while (!queue.isEmpty()) {
                DrawCall drawCall = queue.poll();
                drawCall.draw(entry.getKey());
            }
        }

        GL33.glBindVertexArray(vaoPrev);
    }
}
