package cn.zbx1425.sowcerext.multipart;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import com.mojang.math.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class MultipartContainer {

    public List<PartBase> parts = new ArrayList<>();

    public void update(MultipartUpdateProp prop) {
        for (PartBase part : parts) {
            part.update(prop);
        }
    }

    public void enqueueAll(BatchManager batchManager, Matrix4f basePose, int light, ShaderProp shaderProp) {
        int shaderLightmapUV = VertAttrType.exchangeLightmapUVBits(light);
        for (PartBase part : parts) {
            VertArrays model = part.getModel();
            if (model == null) continue;
            Matrix4f partPose = basePose.copy();
            partPose.multiply(part.getTransform());

            batchManager.enqueue(model, new EnqueueProp(
                    new VertAttrState().setModelMatrix(partPose).setLightmapUV(shaderLightmapUV)
            ), shaderProp);
        }
    }

    public MultipartContainer copy() {
        MultipartContainer result = new MultipartContainer();
        result.parts = new ArrayList<>(this.parts.size());
        for (PartBase part : parts) {
            result.parts.add(part.copy());
        }
        return result;
    }
}
