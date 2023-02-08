package cn.zbx1425.sowcer.object;

import cn.zbx1425.sowcer.util.GlStateTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;
import java.nio.ByteBuffer;

public class VertBuf implements Closeable {

    public int id;

    public static final int USAGE_STATIC_DRAW = GL33.GL_STATIC_DRAW;
    public static final int USAGE_DYNAMIC_DRAW = GL33.GL_DYNAMIC_DRAW;
    public static final int USAGE_STREAM_DRAW = GL33.GL_STREAM_DRAW;

    public VertBuf() {
        
        id = GL33.glGenBuffers();
    }

    public void bind(int target) {
        GlStateTracker.assertProtected();
        GL33.glBindBuffer(target, id);
    }

    public void upload(ByteBuffer buffer, int usage) {
        int vboPrev = GL33.glGetInteger(GL33.GL_ARRAY_BUFFER_BINDING);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, id);
        buffer.clear();
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, usage);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vboPrev);
    }

    public void upload(ByteBuffer buffer, int size, int usage) {
        int vboPrev = GL33.glGetInteger(GL33.GL_ARRAY_BUFFER_BINDING);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, id);
        buffer.clear();
        GL33.nglBufferData(GL33.GL_ARRAY_BUFFER, size, MemoryUtil.memAddress0(buffer), usage);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vboPrev);
    }

    @Override
    public void close() {
        if (RenderSystem.isOnRenderThread()) {
            GL33.glDeleteBuffers(id);
            id = 0;
        } else {
            RenderSystem.recordRenderCall(this::close);
        }
    }
}
