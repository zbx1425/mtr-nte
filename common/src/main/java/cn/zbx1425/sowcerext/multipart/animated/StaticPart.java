package cn.zbx1425.sowcerext.multipart.animated;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.util.AttrUtil;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.PartBase;
import cn.zbx1425.sowcerext.reuse.ModelManager;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;

public class StaticPart extends PartBase {

    private final ModelCluster model;

    private StaticPart(ModelCluster model) {
        this.model = model;
    }

    public StaticPart(RawModel rawModel, ModelManager modelManager) {
        model = modelManager.uploadVertArrays(rawModel);
    }

    @Override
    public void update(MultipartUpdateProp prop) {

    }

    @Override
    public ModelCluster getModel(MultipartUpdateProp prop) {
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
        StaticPart result = new StaticPart(model);
        return result;
    }
}
