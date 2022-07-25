package cn.zbx1425.sowcer.util;

import com.mojang.math.Matrix4f;

import java.nio.ByteBuffer;

public class AttrUtil {

    public static final Matrix4f MAT_NO_TRANSFORM = new Matrix4f();

    static {
        MAT_NO_TRANSFORM.setIdentity();
    }

    public static int exchangeLightmapUVBits(int light) {
        return (light >>> 16) | (((short) light) << 16);
    }

    public static void zeroRotation(Matrix4f src) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(16 * 4);
        src.store(byteBuffer.asFloatBuffer());
        byteBuffer.clear();
        byteBuffer.asFloatBuffer().put(new float[] {
            1,0,0,0,0,1,0,0,0,0,1,0
        });
        byteBuffer.clear();
        src.load(byteBuffer.asFloatBuffer());
    }
}
