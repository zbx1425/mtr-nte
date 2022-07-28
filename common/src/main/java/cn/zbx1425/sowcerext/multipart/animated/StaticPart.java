package cn.zbx1425.sowcerext.multipart.animated;

import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.util.AttrUtil;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.PartBase;
import cn.zbx1425.sowcerext.reuse.ModelManager;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class StaticPart extends PartBase {

    private final RawModel rawModel;
    private final VertArrays model;

    private StaticPart(RawModel rawModel, VertArrays model) {
        this.rawModel = rawModel;
        this.model = model;
    }

    public StaticPart(RawModel rawModel, ModelManager modelManager) {
        this.rawModel = rawModel;
        model = modelManager.uploadVertArrays(rawModel);
    }

    @Override
    public void update(MultipartUpdateProp prop) {

    }

    @Override
    public VertArrays getModel(MultipartUpdateProp prop) {
        return model;
    }

    @Override
    public RawModel getRawModel(MultipartUpdateProp prop) {
        return rawModel;
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
        StaticPart result = new StaticPart(rawModel, model);
        return result;
    }
}
