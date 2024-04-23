package cn.zbx1425.sowcer.vertex;

public enum VertAttrSrc {
    /** In MaterialProp or EnqueueProp, MaterialProp has priority */
    GLOBAL,

    /** In vertex VBO */
    VERTEX_BUF,

    /** If specified in EnqueueProp or MaterialProp use global, otherwise use vertex VBO */
    VERTEX_BUF_OR_GLOBAL,

    /** In instance VBO */
    INSTANCE_BUF,

    /** If specified in EnqueueProp or MaterialProp use global, otherwise use instance VBO */
    INSTANCE_BUF_OR_GLOBAL;

    public boolean isToggleable() {
        return this == VERTEX_BUF_OR_GLOBAL || this == INSTANCE_BUF_OR_GLOBAL;
    }

    public boolean inVertBuf() {
        return this == VERTEX_BUF || this == VERTEX_BUF_OR_GLOBAL;
    }
}
