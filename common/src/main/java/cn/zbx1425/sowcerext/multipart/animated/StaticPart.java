package cn.zbx1425.sowcerext.multipart.animated;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.PartBase;
import com.mojang.math.Matrix4f;

public class StaticPart extends PartBase {

    private VertArrays model;

    private static Matrix4f NO_TRANSFORM = new Matrix4f();
    static {
        NO_TRANSFORM.setIdentity();
    }

    public StaticPart(VertArrays model) {
        this.model = model;
    }

    @Override
    public void update(MultipartUpdateProp prop) {

    }

    @Override
    public VertArrays getModel() {
        return model;
    }

    @Override
    public Matrix4f getTransform() {
        return NO_TRANSFORM;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public PartBase clone() {
        return new StaticPart(model);
    }
}
