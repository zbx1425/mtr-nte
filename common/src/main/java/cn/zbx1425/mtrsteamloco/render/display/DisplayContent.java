package cn.zbx1425.mtrsteamloco.render.display;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.display.node.DisplayNode;
import cn.zbx1425.mtrsteamloco.render.display.node.DisplayNodeFactory;
import cn.zbx1425.mtrsteamloco.render.display.template.DisplayTemplate;
import cn.zbx1425.mtrsteamloco.render.display.template.DisplayTemplateFactory;
import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.model.Mesh;
import cn.zbx1425.sowcer.object.IndexBuf;
import cn.zbx1425.sowcer.object.VertArray;
import cn.zbx1425.sowcer.object.VertBuf;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import mtr.data.TrainClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL11;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DisplayContent implements Closeable {

    private final Map<String, DisplaySlot> slots;

    private final MaterialProp colorMaterialProp;
    private final MaterialProp depthMaterialProp;

    private final Mesh immediateMesh;
    private final VertArray immediateVertArray;

    private final DisplayBufferBuilder colorVertexConsumer;
    private final DisplayBufferBuilder depthVertexConsumer;
    private final HashMap<ResourceLocation, DisplayBufferBuilder> textVertexConsumers = new HashMap<>();


    private final Map<String, DisplayTemplate> templates;
    private final DisplayNode logic;

    public final int imgWidth, imgHeight;
    public final float refUMax, refVMax;

    public Matrix4f currentPose;
    public int currentCarNum;
    public boolean currentCarDoorLeftOpen, currentCarDoorRightOpen;

    public DisplayContent(ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject, Map<String, DisplaySlot> slots) throws IOException {
        ResourceLocation textureLocation = ResourceUtil.resolveRelativePath(basePath, jsonObject.get("texture").getAsString(), ".png");
        colorMaterialProp = makeDisplayMaterial(textureLocation);
        colorVertexConsumer = new DisplayBufferBuilder();
        depthMaterialProp = makeDisplayMaterial(MaterialProp.WHITE_TEXTURE_LOCATION);
        depthVertexConsumer = new DisplayBufferBuilder();

        immediateMesh = new Mesh(new VertBuf(), new IndexBuf(0, GL11.GL_UNSIGNED_INT), null);
        immediateVertArray = new VertArray();
        immediateVertArray.create(immediateMesh, DisplayBufferBuilder.DISPLAY_MAPPING, null);

        JsonArray textureSize = jsonObject.get("texture_size").getAsJsonArray();
        imgWidth = textureSize.get(0).getAsInt(); imgHeight = textureSize.get(1).getAsInt();

        if (jsonObject.has("uv_ref_size")) {
            JsonArray uvRefSize = jsonObject.get("uv_ref_size").getAsJsonArray();
            refUMax = uvRefSize.get(0).getAsFloat(); refVMax = uvRefSize.get(1).getAsFloat();
        } else {
            refUMax = refVMax = 1;
        }

        this.slots = slots;

        templates = jsonObject.get("templates").getAsJsonObject().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toLowerCase(Locale.ROOT),
                        entry -> {
                            try {
                                return DisplayTemplateFactory.parse(resources, basePath, entry.getValue().getAsJsonObject());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }));
        logic = DisplayNodeFactory.parse(this, resources, basePath, jsonObject.get("logic").getAsJsonObject());
    }

    private MaterialProp makeDisplayMaterial(ResourceLocation texture) {
        MaterialProp result = new MaterialProp();
        result.texture = texture;
        result.shaderName = "rendertype_entity_cutout";
        result.attrState.setLightmapUV(15 << 4 | 15 << 20);
        result.attrState.setNormal(new Vector3f(0, 1, 0));
        result.attrState.setColor(-1);
        result.attrState.setOverlayUVNoOverlay();
        return result;
    }

    public void addQuad(String slotName, int x1, int y1, int x2, int y2, float u1, float v1, float u2, float v2, int color) {
        // if (opacity < 1) color = (color & 0x00FFFFFF) | ((int)((color >>> 24) * opacity) << 24);
        DisplaySlot slot = getSlot(slotName);
        for (DisplaySlot.SlotFace face : slot.faces) {
            colorVertexConsumer.vertex(currentPose, face.getPositionAt(u1 / refUMax, v1 / refVMax), color, (float)x1 / imgWidth, (float)y1 / imgHeight);
            colorVertexConsumer.vertex(currentPose, face.getPositionAt(u1 / refUMax, v2 / refVMax), color, (float)x1 / imgWidth, (float)y2 / imgHeight);
            colorVertexConsumer.vertex(currentPose, face.getPositionAt(u2 / refUMax, v2 / refVMax), color, (float)x2 / imgWidth, (float)y2 / imgHeight);
            colorVertexConsumer.vertex(currentPose, face.getPositionAt(u2 / refUMax, v1 / refVMax), color, (float)x2 / imgWidth, (float)y1 / imgHeight);
        }
    }

    public void addTextQuad(String slotName, ResourceLocation textTexture, float srcU1, float srcV1, float srcU2, float srcV2, float u1, float v1, float u2, float v2, int color) {
        // if (opacity < 1) color = (color & 0x00FFFFFF) | ((int)((color >>> 24) * opacity) << 24);
        DisplaySlot slot = getSlot(slotName);
        DisplayBufferBuilder bufferBuilder = textVertexConsumers.computeIfAbsent(textTexture, rl -> new DisplayBufferBuilder());
        for (DisplaySlot.SlotFace face : slot.faces) {
            bufferBuilder.vertex(currentPose, face.getPositionAt(u1 / refUMax, v1 / refVMax), color, srcU1, srcV1);
            bufferBuilder.vertex(currentPose, face.getPositionAt(u1 / refUMax, v2 / refVMax), color, srcU1, srcV2);
            bufferBuilder.vertex(currentPose, face.getPositionAt(u2 / refUMax, v2 / refVMax), color, srcU2, srcV2);
            bufferBuilder.vertex(currentPose, face.getPositionAt(u2 / refUMax, v1 / refVMax), color, srcU2, srcV1);
        }
    }

    public void addHAreaQuad(String slotName, int x1, int y1, int width, int height, float u1, float v1, float u2, float v2, int xl, int xr, int color) {
        addQuad(slotName, xl, y1, xr, y1 + height,
                u1 + (u2 - u1) * ((float)(xl - x1) / width), v1, u1 + (u2 - u1) * ((float)(xr - x1) / width), v2, color);
    }

    public DisplayTemplate getTemplate(String key) {
        DisplayTemplate result = templates.get(key);
        if (result == null) throw new IllegalArgumentException("No template: " + key);
        return result;
    }

    public DisplaySlot getSlot(String slotName) {
        DisplaySlot slot = slots.get(slotName);
        if (slot == null) throw new IllegalArgumentException("No slot: " + slotName);
        return slot;
    }

    public void handleCar(TrainClient train, Matrix4f pose, int carNum, boolean doorLeftOpen, boolean doorRightOpen) {
        this.currentCarNum = carNum;
        this.currentCarDoorLeftOpen = doorLeftOpen;
        this.currentCarDoorRightOpen = doorRightOpen;
        this.currentPose = pose;
        logic.draw(this, train);

        for (DisplaySlot slot : slots.values()) {
            for (DisplaySlot.SlotFace face : slot.faces) {
                depthVertexConsumer.vertex(pose, face.getPositionAt(0, 0), -1, 0, 0);
                depthVertexConsumer.vertex(pose, face.getPositionAt(0, 1), -1, 0, 1);
                depthVertexConsumer.vertex(pose, face.getPositionAt(1, 1), -1, 1, 1);
                depthVertexConsumer.vertex(pose, face.getPositionAt(1, 0), -1, 1, 0);
            }
        }
    }

    public void drawImmediate(Vector3f transformedNormal) {
        if (colorVertexConsumer.hasData()) {
            MainClient.drawScheduler.shaderManager.setupShaderBatchState(colorMaterialProp, ShaderProp.DEFAULT);
            colorVertexConsumer.upload(immediateMesh);
            RenderSystem.depthMask(false);
            immediateVertArray.bind();
            colorMaterialProp.attrState.setNormal(transformedNormal);
            colorMaterialProp.attrState.apply(immediateVertArray);
            immediateVertArray.draw();
            colorVertexConsumer.clear();
            MainClient.drawScheduler.shaderManager.cleanupShaderBatchState(colorMaterialProp, ShaderProp.DEFAULT);
        }

        // Then texts, as they are on different textures
        for (Map.Entry<ResourceLocation, DisplayBufferBuilder> entry : textVertexConsumers.entrySet()) {
            MaterialProp textMaterialProp = makeDisplayMaterial(entry.getKey());
            MainClient.drawScheduler.shaderManager.setupShaderBatchState(textMaterialProp, ShaderProp.DEFAULT);
            entry.getValue().upload(immediateMesh);
            RenderSystem.depthMask(false);
            immediateVertArray.bind();
            textMaterialProp.attrState.setNormal(transformedNormal);
            textMaterialProp.attrState.apply(immediateVertArray);
            immediateVertArray.draw();
            MainClient.drawScheduler.shaderManager.cleanupShaderBatchState(textMaterialProp, ShaderProp.DEFAULT);
            entry.getValue().close();
        }
        textVertexConsumers.clear();

        // Separately draw to depth buffer to avoid z-fighting between color faces
        if (depthVertexConsumer.hasData()) {
            MainClient.drawScheduler.shaderManager.setupShaderBatchState(depthMaterialProp, ShaderProp.DEFAULT);
            depthVertexConsumer.upload(immediateMesh);
            RenderSystem.depthMask(true);
            RenderSystem.colorMask(false, false, false, false);
            immediateVertArray.bind();
            depthMaterialProp.attrState.setNormal(transformedNormal);
            depthMaterialProp.attrState.apply(immediateVertArray);
            immediateVertArray.draw();
            depthVertexConsumer.clear();
            MainClient.drawScheduler.shaderManager.cleanupShaderBatchState(depthMaterialProp, ShaderProp.DEFAULT);
        }

        RenderSystem.colorMask(true, true, true, true);
    }

    @Override
    public void close() {
        immediateMesh.close();
        immediateVertArray.close();
    }

}
