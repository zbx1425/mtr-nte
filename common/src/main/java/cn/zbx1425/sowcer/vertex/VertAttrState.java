package cn.zbx1425.sowcer.vertex;

import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcer.util.AttrUtil;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

public class VertAttrState {

    public Vector3f position;
    public Integer color;
    public Float texU, texV;
    public Integer lightmapUV;
    public Vector3f normal;
    public Matrix4f matrixModel;

    public void apply(MaterialProp materialProp) {
        for (VertAttrType attr : VertAttrType.values()) {
            switch (attr) {
                case POSITION:
                    if (position == null) continue;
                    GL33.glVertexAttrib3f(attr.location, position.x(), position.y(), position.z());
                    break;
                case COLOR:
                    if (color == null) continue;
                    GL33.glVertexAttrib4Nub(attr.location, (byte)(color >>> 24), (byte)(color >>> 16), (byte)(color >>> 8), (byte)(int)color);
                    break;
                case UV_TEXTURE:
                    if (texU == null || texV == null) continue;
                    GL33.glVertexAttrib2f(attr.location, texU, texV);
                    break;
                case UV_LIGHTMAP:
                    if (lightmapUV == null) continue;
                    GL33.glVertexAttribI2i(attr.location, (short)(lightmapUV >>> 16), (short)(int)lightmapUV);
                    break;
                case NORMAL:
                    if (normal == null) continue;
                    GL33.glVertexAttrib3f(attr.location, normal.x(), normal.y(), normal.z());
                    break;
                case MATRIX_MODEL:
                    if (matrixModel == null) continue;
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
        return Objects.equals(position, that.position) && Objects.equals(color, that.color) && Objects.equals(texU, that.texU)
                && Objects.equals(texV, that.texV) && Objects.equals(lightmapUV, that.lightmapUV) && Objects.equals(normal, that.normal)
                && Objects.equals(matrixModel, that.matrixModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, color, texU, texV, lightmapUV, normal, matrixModel);
    }

    public VertAttrState copy() {
        VertAttrState clone = new VertAttrState();
        clone.position = this.position == null ? null : this.position.copy();
        clone.color = this.color;
        clone.texU = this.texU;
        clone.texV = this.texV;
        clone.lightmapUV = this.lightmapUV;
        clone.normal = this.normal == null ? null : this.normal.copy();
        clone.matrixModel = this.matrixModel == null ? null : this.matrixModel.copy();
        return clone;
    }
}
