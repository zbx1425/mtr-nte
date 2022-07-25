package cn.zbx1425.sowcerext.multipart.animated;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.util.AttrUtil;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.PartBase;
import com.mojang.math.Matrix4f;

public class StaticPart extends PartBase {

    private final VertArrays model;

    public StaticPart(VertArrays model) {
        this.model = model;
    }

    @Override
    public void update(MultipartUpdateProp prop) {

    }

    @Override
    public VertArrays getModel(MultipartUpdateProp prop) {
        return model;
    }

    @Override
    public Matrix4f getTransform(MultipartUpdateProp prop) {
        return parent == null ? AttrUtil.MAT_NO_TRANSFORM : parent.getTransform(prop);
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    public PartBase copy() {
        return new StaticPart(model);
    }
}
