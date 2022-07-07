package cn.zbx1425.sowcer.vertex;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class VertAttrState {

    public Vector3f position = new Vector3f();
    public int color = 0xFFFFFFFF;
    public float texU = 0.0F, texV = 0.0F;
    public int lightmapUV = 0;
    public Vector3f normal = Vector3f.YP;
    public Matrix4f matrixModel = new Matrix4f();

    public VertAttrState() {
        matrixModel.setIdentity();
    }

    public void apply(VertAttrMapping mapping, VertAttrSrc target) {
        for (VertAttrType attr : VertAttrType.values()) {
            if (mapping.sources.get(attr) != target) continue;
            switch (attr) {
                case POSITION -> {
                    GL33.glVertexAttrib3f(attr.location, position.x(), position.y(), position.z());
                }
                case COLOR -> {
                    GL33.glVertexAttrib4Nub(attr.location, (byte)(color >>> 24), (byte)(color >>> 16), (byte)(color >>> 8), (byte)color);
                }
                case UV_TEXTURE -> {
                    GL33.glVertexAttrib2f(attr.location, texU, texV);
                }
                case UV_LIGHTMAP -> {
                    GL33.glVertexAttribI2i(attr.location, (short)(lightmapUV >>> 16), (short)lightmapUV);
                }
                case NORMAL -> {
                    GL33.glVertexAttrib3f(attr.location, normal.x(), normal.y(), normal.z());
                }
                case MATRIX_MODEL -> {
                    ByteBuffer byteBuf = ByteBuffer.allocate(64);
                    FloatBuffer floatBuf = byteBuf.asFloatBuffer();
                    matrixModel.store(floatBuf);
                    GL33.glVertexAttrib4f(attr.location, floatBuf.get(0), floatBuf.get(1), floatBuf.get(2), floatBuf.get(3));
                    GL33.glVertexAttrib4f(attr.location + 1, floatBuf.get(4), floatBuf.get(5), floatBuf.get(6), floatBuf.get(7));
                    GL33.glVertexAttrib4f(attr.location + 2, floatBuf.get(8), floatBuf.get(9), floatBuf.get(10), floatBuf.get(11));
                    GL33.glVertexAttrib4f(attr.location + 3, floatBuf.get(12), floatBuf.get(13), floatBuf.get(14), floatBuf.get(15));
                }
            }
        }
    }

    public static class Builder {

        private static final VertAttrState result = new VertAttrState();

        public Builder setPosition(Vector3f position) {
            result.position = position;
            return this;
        }

        public Builder setColor(byte r, byte g, byte b, byte a) {
            result.color = r << 24 | g << 16 | b << 8 | a;
            return this;
        }

        public Builder setColor(int rgba) {
            result.color = rgba;
            return this;
        }

        public Builder setTextureUV(float u, float v) {
            result.texU = u;
            result.texV = v;
            return this;
        }

        public Builder setLightmapUV(short u, short v) {
            result.lightmapUV = u << 16 | v;
            return this;
        }

        public Builder setLightmapUV(int uv) {
            result.lightmapUV = uv;
            return this;
        }

        public Builder setNormal(Vector3f position) {
            result.normal = position;
            return this;
        }

        public Builder setModelMatrix(Matrix4f matrix) {
            result.matrixModel = matrix;
            return this;
        }

        public VertAttrState build() {
            return result;
        }
    }
}
