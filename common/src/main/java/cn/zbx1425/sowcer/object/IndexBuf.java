package cn.zbx1425.sowcer.object;

import org.lwjgl.opengl.GL33;

public class IndexBuf extends VertBuf {

    public final int faceCount;
    public final int indexType;

    public final int vertexCount;

    public IndexBuf(int faceCount, int indexType) {
        this.faceCount = faceCount;
        this.indexType = indexType;
        this.vertexCount = faceCount * 3;
    }

}
