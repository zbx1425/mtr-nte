package cn.zbx1425.sowcer.batch;

import cn.zbx1425.sowcer.vertex.VertAttrState;

/** Additional property affecting rendering process. Set when enqueue. Does not affect batching. */
public class EnqueueProp {

    /** The vertex attribute values to use for those specified with VertAttrSrc ENQUEUE. */
    public VertAttrState attrState;

    public EnqueueProp(VertAttrState attrState) {
        this.attrState = attrState;
    }
}
