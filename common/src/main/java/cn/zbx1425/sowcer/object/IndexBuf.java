package cn.zbx1425.sowcer.object;

import org.lwjgl.opengl.GL33;

public class IndexBuf extends VertBuf {

    public int faceCount;
    public PrimitiveMode primitiveMode;
    public int indexType;

    public int vertexCount;

    public IndexBuf(int faceCount, PrimitiveMode primitiveMode, int indexType) {
        this.faceCount = faceCount;
        this.primitiveMode = primitiveMode;
        this.indexType = indexType;

        this.vertexCount = primitiveMode.vertPerPrim * faceCount;
    }

    public enum PrimitiveMode {
        TRIANGLES(GL33.GL_TRIANGLES, 3),
        QUADS(GL33.GL_QUADS, 4),
        ;

        public final int glMode, vertPerPrim;

        PrimitiveMode(int glMode, int vertPerPrim) {
            this.glMode = glMode;
            this.vertPerPrim = vertPerPrim;
        }
    }
}
