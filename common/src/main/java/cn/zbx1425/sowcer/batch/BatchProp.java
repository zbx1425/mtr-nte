package cn.zbx1425.sowcer.batch;

import cn.zbx1425.sowcer.vertex.VertAttrState;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL33;

public class BatchProp {

    /** Name of the shader program. Must be loaded in ShaderManager. */
    public String shaderName;
    /** The texture to use. Null disables texture. */
    public ResourceLocation texture;

    /** The vertex attribute values to use for those specified with VertAttrSrc BATCH */
    public VertAttrState attrState;

    /** If blending should be set up. True for entity_translucent_* and beacon_beam when translucent is true. */
    public boolean translucent = false;
    /** If depth buffer should be written to. False for beacon_beam when translucent is true, true for everything else. */
    public boolean writeDepthBuf = true;
    /** If face culling is enabled. False makes everything effectively double-sided. */
    public boolean cull = true;

    /**
     * If true: You need to supply the camera position and rotation transform in MATRIX_MODEL
     *   (Vanilla PoseStack behavior; instancing cannot be used since matrix depends on camera pose)
     * If false: MATRIX_MODEL is in world space, ModelViewMat shader uniform is altered to include camera transform.
     *   (Must not use PoseStack passed into EntityRenderer by Minecraft, need creating a new one; instancing possible)
     */
    public boolean eyeTransformInModelMatrix = true;
    /**
     * If true: You need to multiply the normal vector with camera rotation
     *   (Vanilla PoseStack behavior; normal cannot be in any buffer since it depends on camera angle)
     * If false: normal is in world space, Light*_Direction shader uniforms are altered to be also in world space
     *   (Must not multiply the PoseStack passed into EntityRenderer by Minecraft with vertex normal)
     */
    public boolean eyeTransformInNormal = true;

    public BatchProp(String shaderName, ResourceLocation texture) {
        this.shaderName = shaderName;
        this.texture = texture;
    }

    public void setupCompositeState() {
        if (texture != null) {
            RenderSystem.enableTexture();
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            textureManager.getTexture(texture).setFilter(false, false);
            RenderSystem.setShaderTexture(0, texture);
        } else {
            RenderSystem.disableTexture();
        }

        if (translucent) {
            RenderSystem.enableBlend(); // TransparentState
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        } else {
            RenderSystem.disableBlend();
        }
        RenderSystem.enableDepthTest(); // DepthTestState
        RenderSystem.depthFunc(GL33.GL_LEQUAL);
        if (cull) {
            RenderSystem.enableCull(); // CullState
        } else {
            RenderSystem.disableCull();
        }
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer(); // LightmapState
        Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor(); // OverlayState
        RenderSystem.depthMask(writeDepthBuf); // WriteMaskState
    }
}
