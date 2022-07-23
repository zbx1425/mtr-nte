package cn.zbx1425.sowcer.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL33;

public class GLStateCapture {

    private int idVao;
    private ShaderInstance shaderInstance;

    public void capture() {
        idVao = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING);
        // shaderInstance = RenderSystem.getShader();
    }

    public void restore() {
        GL33.glBindVertexArray(idVao);

        // TODO obtain original state from RenderSystem?
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        // RenderSystem.setShader(() -> shaderInstance);
        // shaderInstance.apply();
    }
}
