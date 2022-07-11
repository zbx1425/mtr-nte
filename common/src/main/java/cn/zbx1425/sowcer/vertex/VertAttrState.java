package cn.zbx1425.sowcer.vertex;

import cn.zbx1425.sowcer.batch.MaterialProp;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

public class VertAttrState {

    public Vector3f position = new Vector3f();
    public int color = 0xFFFFFFFF;
    public float texU = 0.0F, texV = 0.0F;
    public int lightmapUV = 15 << 4 | 15 << 20;
    public Vector3f normal = Vector3f.YP;
    public Matrix4f matrixModel = new Matrix4f();

    public VertAttrState() {
        matrixModel.setIdentity();
    }

    public void apply(VertAttrMapping mapping, VertAttrSrc target, MaterialProp materialProp) {
        for (VertAttrType attr : VertAttrType.values()) {
            if (mapping.sources.get(attr) != target) continue;
            switch (attr) {
                case POSITION:
                    GL33.glVertexAttrib3f(attr.location, position.x(), position.y(), position.z());
                    break;
                case COLOR:
                    GL33.glVertexAttrib4Nub(attr.location, (byte)(color >>> 24), (byte)(color >>> 16), (byte)(color >>> 8), (byte)color);
                    break;
                case UV_TEXTURE:
                    GL33.glVertexAttrib2f(attr.location, texU, texV);
                    break;
                case UV_LIGHTMAP:
                    GL33.glVertexAttribI2i(attr.location, (short)(lightmapUV >>> 16), (short)lightmapUV);
                    break;
                case NORMAL:
                    GL33.glVertexAttrib3f(attr.location, normal.x(), normal.y(), normal.z());
                    break;
                case MATRIX_MODEL:
                    ByteBuffer byteBuf = ByteBuffer.allocate(64);
                    FloatBuffer floatBuf = byteBuf.asFloatBuffer();
                    matrixModel.store(floatBuf);
                    if (materialProp.billboard) {
                        GL33.glVertexAttrib4f(attr.location, 1, 0, 0, 0);
                        GL33.glVertexAttrib4f(attr.location + 1, 0, 1, 0, 0);
                        GL33.glVertexAttrib4f(attr.location + 2, 0, 0, 1, 0);
                        GL33.glVertexAttrib4f(attr.location + 3, floatBuf.get(12), floatBuf.get(13), floatBuf.get(14), floatBuf.get(15));
                    } else {
                        GL33.glVertexAttrib4f(attr.location, floatBuf.get(0), floatBuf.get(1), floatBuf.get(2), floatBuf.get(3));
                        GL33.glVertexAttrib4f(attr.location + 1, floatBuf.get(4), floatBuf.get(5), floatBuf.get(6), floatBuf.get(7));
                        GL33.glVertexAttrib4f(attr.location + 2, floatBuf.get(8), floatBuf.get(9), floatBuf.get(10), floatBuf.get(11));
                        GL33.glVertexAttrib4f(attr.location + 3, floatBuf.get(12), floatBuf.get(13), floatBuf.get(14), floatBuf.get(15));
                    }
                    break;
            }
        }
    }

    public VertAttrState setPosition(Vector3f position) {
        this.position = position;
        return this;
    }

    public VertAttrState setColor(int r, int g, int b, int a) {
        this.color = r << 24 | g << 16 | b << 8 | a;
        return this;
    }

    public VertAttrState setColor(int rgba) {
        this.color = rgba;
        return this;
    }

    public VertAttrState setTextureUV(float u, float v) {
        this.texU = u;
        this.texV = v;
        return this;
    }

    public VertAttrState setLightmapUV(short u, short v) {
        this.lightmapUV = u << 16 | v;
        return this;
    }

    public VertAttrState setLightmapUV(int uv) {
        this.lightmapUV = uv;
        return this;
    }

    public VertAttrState setNormal(Vector3f position) {
        this.normal = position;
        return this;
    }

    public VertAttrState setModelMatrix(Matrix4f matrix) {
        this.matrixModel = matrix;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertAttrState that = (VertAttrState) o;
        return color == that.color && Float.compare(that.texU, texU) == 0 && Float.compare(that.texV, texV) == 0 && lightmapUV == that.lightmapUV && position.equals(that.position) && normal.equals(that.normal) && matrixModel.equals(that.matrixModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, color, texU, texV, lightmapUV, normal, matrixModel);
    }
}
