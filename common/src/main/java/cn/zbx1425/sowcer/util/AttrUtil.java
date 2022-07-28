package cn.zbx1425.sowcer.util;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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

    public static Matrix3f getRotationPart(Matrix4f src) {
        float[] srcValues = new float[16];
        FloatBuffer srcFloatBuffer = FloatBuffer.wrap(srcValues);
        src.store(srcFloatBuffer);
        ByteBuffer dstBuffer = ByteBuffer.allocate(9 * 4);
        FloatBuffer dstFloatBuffer = dstBuffer.asFloatBuffer();
        dstFloatBuffer.put(srcValues, 0, 3);
        dstFloatBuffer.put(srcValues, 4, 3);
        dstFloatBuffer.put(srcValues, 8, 3);
        Matrix3f result = new Matrix3f();
        result.load(dstFloatBuffer);
        return result;
    }

    public static int argbToBgr(int color) {
        final int a = 0xFF;
        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = color & 0xFF;
        return a << 24 | b << 16 | g << 8 | r;
    }

    public static int rgbaToArgb(int color) {
        final int r = (color >> 24) & 0xFF;
        final int g = (color >> 16) & 0xFF;
        final int b = (color >> 8) & 0xFF;
        final int a = color & 0xFF;
        return a << 24 | r << 16 | g << 8 | b;
    }
}
