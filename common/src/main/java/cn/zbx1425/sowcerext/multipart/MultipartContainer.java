package cn.zbx1425.sowcerext.multipart;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.util.AttrUtil;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.*;

public class MultipartContainer {

    public List<PartBase> parts = new ArrayList<>();

    public void updateAndEnqueueAll(MultipartUpdateProp prop, BatchManager batchManager, MultiBufferSource vertexConsumers, Matrix4f basePose, int light) {
        for (PartBase part : parts) {
            part.update(prop);
        }
        for (PartBase part : parts) {
            ModelCluster model = part.getModel(prop);
            if (model == null) continue;
            Matrix4f partPose = basePose.copy();
            partPose.multiply(part.getTransform(prop));
            model.renderOptimized(batchManager, vertexConsumers, partPose, light);
        }
    }

    public void updateAndEnqueueAll(MultipartUpdateProp prop, MultiBufferSource vertexConsumers, Matrix4f basePose, int light) {
        for (PartBase part : parts) {
            part.update(prop);
        }
        for (PartBase part : parts) {
            ModelCluster model = part.getModel(prop);
            if (model == null) continue;
            Matrix4f partPose = basePose.copy();
            partPose.multiply(part.getTransform(prop));
            model.renderUnoptimized(vertexConsumers, partPose, light);
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
