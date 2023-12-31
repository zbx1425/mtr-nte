package cn.zbx1425.sowcer.batch;

import cn.zbx1425.sowcer.shader.BlazeRenderType;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL33;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Properties regarding material. Set during model loading. Affects batching. */
public class MaterialProp {

    /** Name of the shader program. Must be loaded in ShaderManager. */
    public String shaderName;
    /** The texture to use. Null disables texture. */
    public ResourceLocation texture;

    /** The vertex attribute values to use for those specified with VertAttrSrc MATERIAL. */
    public VertAttrState attrState = new VertAttrState();

    /** If blending should be set up. True for entity_translucent_* and beacon_beam when translucent is true. */
    public boolean translucent = false;
    /** If depth buffer should be written to. False for beacon_beam when translucent is true, true for everything else. */
    public boolean writeDepthBuf = true;
    /** If the renderer should remove rotation components from model and view matrices.
     *  Results in faces on the XY plane always facing the camera. */
    public boolean billboard = false;

    public boolean cutoutHack = false;

    /** Count of rolling sign sub-textures horizontally. */
    public int sheetElementsU = 0;
    /** Count of rolling sign sub-textures vertically. */
    public int sheetElementsV = 0;

    public MaterialProp() {

    }
    public MaterialProp(String shaderName) {
        this.shaderName = shaderName;
    }

    public MaterialProp(DataInputStream dis) throws IOException {
        int len = dis.readInt();
        String content = new String(dis.readNBytes(len), StandardCharsets.UTF_8);
        JsonObject mtlObj = (JsonObject)new JsonParser().parse(content);
        this.shaderName = mtlObj.get("shaderName").getAsString();
        this.texture = mtlObj.get("texture").isJsonNull() ? null : new ResourceLocation(mtlObj.get("texture").getAsString());
        this.attrState.color = mtlObj.get("color").isJsonNull() ? null : mtlObj.get("color").getAsInt();
        this.attrState.lightmapUV = mtlObj.get("lightmapUV").isJsonNull() ? null : mtlObj.get("lightmapUV").getAsInt();
        this.translucent = mtlObj.has("translucent") && mtlObj.get("translucent").getAsBoolean();
        this.writeDepthBuf = mtlObj.has("writeDepthBuf") && mtlObj.get("writeDepthBuf").getAsBoolean();
        this.billboard = mtlObj.has("billboard") && mtlObj.get("billboard").getAsBoolean();
        this.cutoutHack = mtlObj.has("cutoutHack") && mtlObj.get("cutoutHack").getAsBoolean();
    }

    public static final ResourceLocation WHITE_TEXTURE_LOCATION = new ResourceLocation("minecraft:textures/misc/white.png");

    public void setupCompositeState() {
#if MC_VERSION <= "11903"
        RenderSystem.enableTexture();
#endif
        if (texture != null) {
            // TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            // textureManager.getTexture(texture).setFilter(false, false);
            RenderSystem.setShaderTexture(0, texture);
        } else {
            RenderSystem.setShaderTexture(0, WHITE_TEXTURE_LOCATION);
        }

        // HACK: To make cutout transparency on beacon_beam work
        if (translucent || cutoutHack) {
            RenderSystem.enableBlend(); // TransparentState
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        } else {
            RenderSystem.disableBlend();
        }
        RenderSystem.enableDepthTest(); // DepthTestState
        RenderSystem.depthFunc(GL33.GL_LEQUAL);
        RenderSystem.enableCull();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer(); // LightmapState
        Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor(); // OverlayState
        RenderSystem.depthMask(writeDepthBuf); // WriteMaskState
    }

    public RenderType getBlazeRenderType() {
        RenderType result;
        ResourceLocation textureToUse = texture == null ? WHITE_TEXTURE_LOCATION : texture;
        switch (shaderName) {
            case "rendertype_entity_cutout":
                result = BlazeRenderType.entityCutout(textureToUse);
                break;
            case "rendertype_entity_translucent_cull":
                result = BlazeRenderType.entityTranslucentCull(textureToUse);
                break;
            case "rendertype_beacon_beam":
                result = BlazeRenderType.beaconBeam(textureToUse, translucent);
                break;
            default:
                result = BlazeRenderType.entityCutout(textureToUse);
                break;
        }
        return result;
    }

    public MaterialProp copy() {
        MaterialProp result = new MaterialProp();
        result.copyFrom(this);
        return result;
    }

    public void copyFrom(MaterialProp other) {
        this.shaderName = other.shaderName;
        this.texture = other.texture;
        this.attrState = other.attrState.copy();
        this.translucent = other.translucent;
        this.writeDepthBuf = other.writeDepthBuf;
        this.billboard = other.billboard;
        this.sheetElementsU = other.sheetElementsU;
        this.sheetElementsV = other.sheetElementsV;
    }

    public void serializeTo(DataOutputStream dos) throws IOException {
        JsonObject mtlObj = new JsonObject();
        mtlObj.addProperty("version", 2);
        mtlObj.addProperty("shaderName", shaderName);
        if (texture == null) {
            mtlObj.add("texture", new JsonNull());
        } else {
            mtlObj.addProperty("texture", texture.toString());
        }
        if (this.attrState.color == null) {
            mtlObj.add("color", new JsonNull());
        } else {
            mtlObj.addProperty("color", this.attrState.color);
        }
        if (this.attrState.lightmapUV == null) {
            mtlObj.add("lightmapUV", new JsonNull());
        } else {
            mtlObj.addProperty("lightmapUV", this.attrState.lightmapUV);
        }
        mtlObj.addProperty("translucent", this.translucent);
        mtlObj.addProperty("writeDepthBuf", this.writeDepthBuf);
        mtlObj.addProperty("billboard", this.billboard);
        mtlObj.addProperty("cutoutHack", this.cutoutHack);
        String content = mtlObj.toString();
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        dos.writeInt(contentBytes.length);
        dos.write(contentBytes);
    }

    @Override
    public String toString() {
        return String.format("{%s: %s%s}",
                texture == null ? "null" : texture.toString(), translucent ? " T-" : "", shaderName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialProp that = (MaterialProp) o;
        return translucent == that.translucent && writeDepthBuf == that.writeDepthBuf
                && billboard == that.billboard && cutoutHack == that.cutoutHack
                && sheetElementsU == that.sheetElementsU && sheetElementsV == that.sheetElementsV
                && Objects.equals(shaderName, that.shaderName) && Objects.equals(texture, that.texture)
                && Objects.equals(attrState, that.attrState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shaderName, texture, attrState, translucent, writeDepthBuf, billboard,
                cutoutHack, sheetElementsU, sheetElementsV);
    }
}
