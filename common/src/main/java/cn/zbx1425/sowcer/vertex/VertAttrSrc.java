package cn.zbx1425.sowcer.vertex;

public enum VertAttrSrc {
    /** Specified dynamically from code when the draw call (BatchManager.enqueue) is placed. */
    ENQUEUE,

    /** Specified statically in MaterialProp during model loading. */
    MATERIAL,

    /** Stored statically in OpenGL vertex buffer. */
    VERTEX_BUF,

    /** Stored statically in OpenGL instance buffer. */
    INSTANCE_BUF,
}
