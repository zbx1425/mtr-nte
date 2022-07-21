package cn.zbx1425.sowcer.object;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL33;

import java.io.Closeable;
import java.nio.ByteBuffer;

public class VertBuf implements Closeable {

    public int id;

    public VertBuf() {
        RenderSystem.assertOnRenderThread();
        id = GL33.glGenBuffers();
    }

    public void bind(int target) {
        RenderSystem.assertOnRenderThread();
        GL33.glBindBuffer(target, id);
    }

    public void upload(ByteBuffer buffer) {
        RenderSystem.assertOnRenderThread();
        int vboPrev = GL33.glGetInteger(GL33.GL_ARRAY_BUFFER_BINDING);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, id);
        buffer.clear();
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW);
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
