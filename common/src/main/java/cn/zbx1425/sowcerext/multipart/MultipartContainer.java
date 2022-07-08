package cn.zbx1425.sowcerext.multipart;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.VertArray;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
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

    public void enqueueAll(BatchManager batchManager, Matrix4f basePose, int light) {
        int shaderLightmapUV = VertAttrType.exchangeLightmapUVBits(light);
        for (PartBase part : parts) {
            VertArray model = part.getModel();
            if (model == null) continue;
            Matrix4f partPose = basePose.copy();
            partPose.multiply(part.getTransform());
            batchManager.enqueue(model, new EnqueueProp(
                    new VertAttrState().setModelMatrix(partPose).setLightmapUV(shaderLightmapUV)
            ), ShaderProp.DEFAULT);
        }
    }
}
