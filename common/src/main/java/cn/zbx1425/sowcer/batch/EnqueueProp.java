package cn.zbx1425.sowcer.batch;

import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import org.lwjgl.opengl.GL33;

/** Additional property affecting rendering process. Set when enqueue. Does not affect batching. */
public class EnqueueProp {

    public static EnqueueProp DEFAULT = new EnqueueProp(null);

    /** The vertex attribute values to use for those specified with VertAttrSrc ENQUEUE. */
    public VertAttrState attrState;

    public VertAttrType toggleableAttr;

    public EnqueueProp(VertAttrState attrState) {
        this.attrState = attrState;
    }

    public EnqueueProp(VertAttrState attrState, VertAttrType toggleableAttr) {
        this.attrState = attrState;
        this.toggleableAttr = toggleableAttr;
    }

    public void applyToggleableAttr() {
        if (toggleableAttr != null) {
            if (attrState != null && attrState.hasAttr(toggleableAttr)) {
                GL33.glDisableVertexAttribArray(toggleableAttr.location);
            } else {
                GL33.glEnableVertexAttribArray(toggleableAttr.location);
            }
        }
    }
}
