package cn.zbx1425.sowcer.util;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class OffHeapAllocator {

    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);

    public static ByteBuffer allocate(int size) {
        long ptr = ALLOCATOR.malloc(size);
        if (ptr == 0) throw new OutOfMemoryError();
        return MemoryUtil.memByteBuffer(ptr, size);
    }

    public static void free(ByteBuffer buf) {
        long ptr = MemoryUtil.memAddress0(buf);
        ALLOCATOR.free(ptr);
    }
}
