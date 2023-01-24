package cn.zbx1425.sowcer.util;

import cn.zbx1425.sowcer.math.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class AttrUtil {

    public static final Matrix4f MAT_NO_TRANSFORM = new Matrix4f();

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

    public static void zeroTranslation(Matrix4f src) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(16 * 4);
        src.store(byteBuffer.asFloatBuffer());
        byteBuffer.position(12 * 4);
        byteBuffer.asFloatBuffer().put(new float[] {
                0,0,0,1
        });
        byteBuffer.clear();
        src.load(byteBuffer.asFloatBuffer());
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
