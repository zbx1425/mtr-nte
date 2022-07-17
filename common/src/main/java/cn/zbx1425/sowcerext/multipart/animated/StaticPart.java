package cn.zbx1425.sowcerext.multipart.animated;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.PartBase;
import com.mojang.math.Matrix4f;

public class StaticPart extends PartBase {

    private final VertArrays model;

    private static final Matrix4f NO_TRANSFORM = new Matrix4f();
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
        return parent == null ? NO_TRANSFORM : parent.getTransform();
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    public PartBase copy() {
        return new StaticPart(model);
    }
}
