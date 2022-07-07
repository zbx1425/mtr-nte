package cn.zbx1425.sowcer.batch;

import cn.zbx1425.sowcer.object.VertArray;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrState;

public class DrawCall {

    public VertArray vertArray;

    public VertAttrState callAttrState;

    public DrawCall(VertArray vertArray, VertAttrState callAttrState) {
        this.vertArray = vertArray;
        this.callAttrState = callAttrState;
    }

    public void draw(BatchProp batch) {
        vertArray.bind();
        if (callAttrState != null) callAttrState.apply(vertArray.mapping, VertAttrSrc.ENQUEUE_CALL);
        if (batch.attrState != null) batch.attrState.apply(vertArray.mapping, VertAttrSrc.BATCH);
        vertArray.draw();
    }

}
