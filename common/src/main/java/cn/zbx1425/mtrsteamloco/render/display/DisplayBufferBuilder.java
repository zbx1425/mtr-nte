package cn.zbx1425.mtrsteamloco.render.display;

import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.model.Mesh;
import cn.zbx1425.sowcer.object.VertBuf;
import cn.zbx1425.sowcer.util.OffHeapAllocator;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import com.mojang.blaze3d.platform.MemoryTracker;

import java.io.Closeable;
import java.nio.ByteBuffer;

public class DisplayBufferBuilder implements Closeable {

    public static final VertAttrMapping DISPLAY_MAPPING = new VertAttrMapping.Builder()
            .set(VertAttrType.POSITION, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.COLOR, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.UV_TEXTURE, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.UV_OVERLAY, VertAttrSrc.GLOBAL)
            .set(VertAttrType.UV_LIGHTMAP, VertAttrSrc.GLOBAL)
            .set(VertAttrType.NORMAL, VertAttrSrc.GLOBAL)
            .set(VertAttrType.MATRIX_MODEL, VertAttrSrc.GLOBAL)
            .build();

    private ByteBuffer vertBuffer;
    private ByteBuffer indexBuffer;

    private int vertexCount = 0;
    private int faceCount = 0;
    private int vertNextByte = 0;
    private int indexFilledBytes = 0;
    private int indexNextByte = 0;

    public DisplayBufferBuilder() {
        vertBuffer = OffHeapAllocator.allocate(256 * DISPLAY_MAPPING.strideVertex);
        indexBuffer = OffHeapAllocator.allocate(256 * 4);
    }

    private void expandVertBuf(int increaseAmount) {
        if (this.vertNextByte + increaseAmount <= this.vertBuffer.capacity()) {
            return;
        }
        int i = this.vertBuffer.capacity();
        int j = i + roundUp(increaseAmount);
        ByteBuffer byteBuffer = MemoryTracker.resize(this.vertBuffer, j);
        byteBuffer.rewind();
        this.vertBuffer = byteBuffer;
    }

    private void expandIndexBuf(int increaseAmount) {
        if (this.indexFilledBytes + increaseAmount <= this.indexBuffer.capacity()) {
            return;
        }
        int i = this.indexBuffer.capacity();
        int j = i + roundUp(increaseAmount);
        ByteBuffer byteBuffer = OffHeapAllocator.resize(this.indexBuffer, j);
        byteBuffer.rewind();
        this.indexBuffer = byteBuffer;
    }

    private static int roundUp(int x) {
        int j;
        int i = 0x200000;
        if (x == 0) return i;
        if (x < 0) i *= -1;
        if ((j = x % i) == 0) return x;
        return x + i - j;
    }

    public void vertex(Matrix4f pose, Vector3f position, int color, float u, float v) {
        Vector3f viewPosition = pose.transform(position);
        vertBuffer.position(vertNextByte);
        vertBuffer.putFloat(viewPosition.x());
        vertBuffer.putFloat(viewPosition.y());
        vertBuffer.putFloat(viewPosition.z());
        vertBuffer.putInt(color);
        vertBuffer.putFloat(u);
        vertBuffer.putFloat(v);
        if (DISPLAY_MAPPING.paddingVertex > 0) vertBuffer.put((byte)0);
        vertNextByte += DISPLAY_MAPPING.strideVertex;
        vertexCount++;
        expandVertBuf(DISPLAY_MAPPING.strideVertex);

        if (vertexCount % 4 == 0) {
            indexNextByte += 4 * 6;
            faceCount += 2;
            if (indexNextByte > indexFilledBytes) {
                indexBuffer.position(indexFilledBytes);
                indexBuffer.putInt(vertexCount - 4);
                indexBuffer.putInt(vertexCount - 3);
                indexBuffer.putInt(vertexCount - 2);
                indexBuffer.putInt(vertexCount - 4);
                indexBuffer.putInt(vertexCount - 2);
                indexBuffer.putInt(vertexCount - 1);
                indexFilledBytes += 4 * 6;
                expandIndexBuf(4 * 6);
            }
        }
    }

    public void upload(Mesh mesh) {
        mesh.vertBuf.upload(vertBuffer, vertNextByte, VertBuf.USAGE_STATIC_DRAW);
        mesh.indexBuf.upload(indexBuffer, indexNextByte, VertBuf.USAGE_STATIC_DRAW);
        mesh.indexBuf.setFaceCount(faceCount);
    }

    public boolean hasData() {
        return vertexCount > 0;
    }

    public void clear() {
        vertNextByte = 0;
        indexNextByte = 0;
        vertexCount = 0;
        faceCount = 0;
    }

    @Override
    public void close() {
        OffHeapAllocator.free(vertBuffer);
        OffHeapAllocator.free(indexBuffer);
    }
}
