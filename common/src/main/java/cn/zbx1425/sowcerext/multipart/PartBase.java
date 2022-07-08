package cn.zbx1425.sowcerext.multipart;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.VertArray;
import com.mojang.math.Matrix4f;

public abstract class PartBase {

    public abstract void update(MultipartUpdateProp prop);

    public abstract VertArrays getModel();

    public abstract Matrix4f getTransform();

    public abstract boolean isStatic();
}
