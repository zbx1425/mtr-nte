package cn.zbx1425.sowcerext.multipart;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.VertArray;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcer.math.Matrix4f;

public abstract class PartBase {

    public PartBase parent;

    public abstract void update(MultipartUpdateProp prop);

    public abstract ModelCluster getModel(MultipartUpdateProp prop);

    public abstract Matrix4f getTransform(MultipartUpdateProp prop);

    public abstract boolean isStatic();

}
