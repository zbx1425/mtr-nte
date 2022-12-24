package cn.zbx1425.sowcer.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL33;

public class GLStateCapture {

    private int vertArrayBinding;
    private int arrayBufBinding;
    private int elementBufBinding;

    private ShaderInstance currentShaderInstance;
    private int currentProgram;

    public void capture() {
        vertArrayBinding = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING);
        arrayBufBinding = GL33.glGetInteger(GL33.GL_ARRAY_BUFFER_BINDING);
        elementBufBinding = GL33.glGetInteger(GL33.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        currentShaderInstance = ShaderInstance.lastAppliedShader;
        currentProgram = GL33.glGetInteger(GL33.GL_CURRENT_PROGRAM);
    }

    public void restore() {
        GL33.glBindVertexArray(vertArrayBinding);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, arrayBufBinding);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, elementBufBinding);
        GL33.glUseProgram(currentProgram);
        ShaderInstance.lastAppliedShader = currentShaderInstance;
        ShaderInstance.lastProgramId = currentProgram;

        // TODO obtain original state from RenderSystem?
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
    }
}
