package cn.zbx1425.sowcerext.multipart;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import com.mojang.math.Matrix4f;

import java.util.*;

public class MultipartContainer {

    public List<PartBase> parts = new ArrayList<>();

    public void updateAndEnqueueAll(MultipartUpdateProp prop, BatchManager batchManager, Matrix4f basePose, int light, ShaderProp shaderProp) {
        for (PartBase part : parts) {
            part.update(prop);
        }
        int shaderLightmapUV = VertAttrType.exchangeLightmapUVBits(light);
        for (PartBase part : parts) {
            VertArrays model = part.getModel(prop);
            if (model == null) continue;
            Matrix4f partPose = basePose.copy();
            partPose.multiply(part.getTransform(prop));

            batchManager.enqueue(model, new EnqueueProp(
                    new VertAttrState().setModelMatrix(partPose).setLightmapUV(shaderLightmapUV)
            ), shaderProp);
        }
    }

    public void topologicalSort() {
        List<PartBase> result = new ArrayList<>(parts.size());
        HashMap<PartBase, Integer> inDeg = new HashMap<>();
        Queue<PartBase> queue = new LinkedList<>();
        for (PartBase part : parts) {
            int crntInDeg = part.parent == null ? 0 : 1;
            inDeg.put(part, crntInDeg);
            if (crntInDeg == 0) queue.add(part);
        }
        while (!queue.isEmpty()) {
            PartBase partU = queue.poll();
            result.add(partU);
            for (PartBase partV : parts) {
                if (partV.parent != partU) continue;
                int crntInDeg = inDeg.get(partV) - 1;
                inDeg.put(partV, crntInDeg);
                if (crntInDeg == 0) {
                    queue.add(partV);
                }
            }
        }
        if (result.size() != parts.size()) throw new IllegalArgumentException("Multipart contains loop reference.");
        this.parts = result;
    }

}
