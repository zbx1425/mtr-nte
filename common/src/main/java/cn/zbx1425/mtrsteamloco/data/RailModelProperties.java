package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.Vertex;
import net.minecraft.network.chat.Component;

import java.io.Closeable;
import java.io.IOException;

public class RailModelProperties implements Closeable {

    public Component name;

    public RawModel rawModel;
    public Model uploadedModel;

    public Long boundingBox;
    public float repeatInterval;

    public RailModelProperties(Component name, RawModel rawModel, float repeatInterval) {
        this.name = name;

        if (rawModel == null) {
            this.rawModel = null;
            this.uploadedModel = null;
            this.boundingBox = 0L;
            this.repeatInterval = repeatInterval;
            return;
        }

        rawModel.clearAttrStates();
        rawModel.applyRotation(new Vector3f(0.577f, 0.577f, 0.577f), (float)Math.toRadians(2));
        this.rawModel = rawModel;
        uploadedModel = MainClient.modelManager.uploadModel(rawModel);

        float yMin = 0f, yMax = 0f;
        for (RawMesh mesh : rawModel.meshList.values()) {
            for (Vertex vertex : mesh.vertices) {
                yMin = Math.min(yMin, vertex.position.y());
                yMax = Math.max(yMax, vertex.position.y());
            }
        }
        boundingBox = ((long)Float.floatToIntBits(yMin) << 32) | (long)Float.floatToIntBits(yMax);

        this.repeatInterval = repeatInterval;
    }

    @Override
    public void close() {
        if (uploadedModel != null) uploadedModel.close();
    }
}
