package cn.zbx1425.sowcer.vertex;

public enum VertAttrSrc {
    /** Specified dynamically from code when the draw call (BatchManager.enqueue) is placed,
     *  or in MaterialProp during model loading. MaterialProp has priority. */
    GLOBAL,

    /** Stored statically in OpenGL vertex buffer. */
    VERTEX_BUF,

    /** Stored statically in OpenGL instance buffer. */
    INSTANCE_BUF,
}
