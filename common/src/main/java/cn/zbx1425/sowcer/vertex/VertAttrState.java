package cn.zbx1425.sowcer.vertex;

import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcer.util.AttrUtil;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

public class VertAttrState {

    public static final VertAttrState EMPTY;
    static {
        EMPTY = new VertAttrState().setPosition(new Vector3f()).setColor(0xFFFFFFFF).setTextureUV(0.0F, 0.0F)
                .setLightmapUV(15 << 4 | 15 << 20).setNormal(Vector3f.YP);
    }

    public Vector3f position;
    public Integer color;
    public Float texU, texV;
    public Integer lightmapUV;
    public Vector3f normal;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertAttrState that = (VertAttrState) o;
        return Objects.equals(position, that.position) && Objects.equals(color, that.color) && Objects.equals(texU, that.texU)
                && Objects.equals(texV, that.texV) && Objects.equals(lightmapUV, that.lightmapUV) && Objects.equals(normal, that.normal)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, color, texU, texV, lightmapUV, normal);
    }
}
