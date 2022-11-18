package cn.zbx1425.sowcer.object;

import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcer.model.Mesh;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL33;

import java.io.Closeable;

public class VertArray implements Closeable {

    public int id;
    public MaterialProp materialProp;
    public IndexBuf indexBuf;
    public InstanceBuf instanceBuf;
    public VertAttrMapping mapping;

    public int faceCount;

    private static VertArray dummyVao;

    public VertArray() {
        id = GL33.glGenVertexArrays();
    }

    public static VertArray getDummyVao() {
        
        if (dummyVao == null) dummyVao = new VertArray();
        return dummyVao;
    }

    public void create(Mesh mesh, VertAttrMapping mapping, InstanceBuf instanceBuf) {
        this.materialProp = mesh.materialProp;
        this.indexBuf = mesh.indexBuf;
        this.instanceBuf = instanceBuf;
        this.mapping = mapping;
        GL33.glBindVertexArray(id);
        mapping.setupAttrsToVao(mesh.vertBuf, instanceBuf);
        mesh.indexBuf.bind(GL33.GL_ELEMENT_ARRAY_BUFFER);
        unbind();

        faceCount = indexBuf.faceCount * (instanceBuf == null ? 1 : instanceBuf.size);
    }

    public void bind() {
        GL33.glBindVertexArray(id);
    }

    public static void unbind() {
        GL33.glBindVertexArray(0);
    }

    public void draw() {
        if (instanceBuf == null) {
            GL33.glDrawElements(indexBuf.primitiveMode.glMode, indexBuf.vertexCount, indexBuf.indexType, 0L);
        } else {
            if (instanceBuf.size < 1) return;
            GL33.glDrawElementsInstanced(indexBuf.primitiveMode.glMode, indexBuf.vertexCount, indexBuf.indexType, 0L, instanceBuf.size);
        }
    }

    @Override
    public void close() {
        if (RenderSystem.isOnRenderThread()) {
            GL33.glDeleteVertexArrays(id);
            id = 0;
        } else {
            RenderSystem.recordRenderCall(this::close);
        }
    }
}
