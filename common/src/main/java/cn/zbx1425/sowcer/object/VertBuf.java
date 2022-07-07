package cn.zbx1425.sowcer.object;

import cn.zbx1425.mtrsteamloco.Main;
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
        Main.LOGGER.info("VBO bind: " + id + " as " + target);
        RenderSystem.assertOnRenderThread();
        GL33.glBindBuffer(target, id);
    }

    public void upload(ByteBuffer buffer) {
        RenderSystem.assertOnRenderThread();
        VertArray.getDummyVao().bind();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, id);
        buffer.clear();
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW);
    }

    @Override
    public void close() {
        Main.LOGGER.info("VBO closed: " + id);
        if (RenderSystem.isOnRenderThread()) {
            GL33.glDeleteBuffers(id);
            id = -1;
        } else {
            RenderSystem.recordRenderCall(this::close);
        }
    }
}
