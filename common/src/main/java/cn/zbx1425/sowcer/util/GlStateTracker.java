package cn.zbx1425.sowcer.util;

import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL33;

public class GlStateTracker {

    private static int vertArrayBinding;
    private static int arrayBufBinding;
    private static int elementBufBinding;

    public static boolean isStateProtected;

    public static void capture() {
        if (isStateProtected) throw new IllegalStateException("GlStateTracker: Nesting");
        vertArrayBinding = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING);
        arrayBufBinding = GL33.glGetInteger(GL33.GL_ARRAY_BUFFER_BINDING);
        elementBufBinding = GL33.glGetInteger(GL33.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        isStateProtected = true;
    }

    public static void restore() {
        if (!isStateProtected) throw new IllegalStateException("GlStateTracker: Not captured");
        GL33.glBindVertexArray(vertArrayBinding);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, arrayBufBinding);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, elementBufBinding);

        RenderSystem.applyModelViewMatrix();

        // TODO obtain original state from RenderSystem?
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);

        isStateProtected = false;
    }

    public static void assertProtected() {
        if (!isStateProtected) throw new IllegalStateException("GlStateTracker: Not protected");
    }

}
