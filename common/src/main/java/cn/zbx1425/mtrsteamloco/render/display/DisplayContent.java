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
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.integration.RawMeshBuilder;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
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

    private final Mesh colorMesh;
    private final VertArray colorVertArray;
    private final DisplayBufferBuilder colorVertexConsumer;

    private final Mesh depthMesh;
    private final VertArray depthVertArray;
    private final DisplayBufferBuilder depthVertexConsumer;

    private final Map<String, DisplayTemplate> templates;
    private final DisplayNode logic;

    private final int imgWidth, imgHeight;

    private Matrix4f currentPose;
    public int currentCarNum;
    public boolean currentCarDoorLeftOpen, currentCarDoorRightOpen;

    public DisplayContent(ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject, Map<String, DisplaySlot> slots) throws IOException {
        ResourceLocation textureLocation = ResourceUtil.resolveRelativePath(basePath, jsonObject.get("texture").getAsString(), ".png");

        colorMaterialProp = new MaterialProp();
        colorMaterialProp.texture = textureLocation;
        colorMaterialProp.shaderName = "rendertype_entity_cutout";
        colorMaterialProp.attrState.setLightmapUV(15 << 4 | 15 << 20);
        colorMaterialProp.attrState.setNormal(new Vector3f(0, 1, 0));
        colorMaterialProp.attrState.setColor(-1);
        colorMaterialProp.attrState.setOverlayUVNoOverlay();

        colorVertexConsumer = new DisplayBufferBuilder();
        colorMesh = new Mesh(new VertBuf(), new IndexBuf(0, GL11.GL_UNSIGNED_INT), colorMaterialProp);
        colorVertArray = new VertArray();
        colorVertArray.create(colorMesh, DisplayBufferBuilder.DISPLAY_MAPPING, null);

        depthMaterialProp = new MaterialProp();
        depthMaterialProp.texture = MaterialProp.WHITE_TEXTURE_LOCATION;
        depthMaterialProp.shaderName = "rendertype_entity_cutout";
        depthMaterialProp.attrState.setLightmapUV(15 << 4 | 15 << 20);
        depthMaterialProp.attrState.setNormal(new Vector3f(0, 1, 0));
        depthMaterialProp.attrState.setColor(-1);
        depthMaterialProp.attrState.setOverlayUVNoOverlay();

        depthVertexConsumer = new DisplayBufferBuilder();
        depthMesh = new Mesh(new VertBuf(), new IndexBuf(0, GL11.GL_UNSIGNED_INT), depthMaterialProp);
        depthVertArray = new VertArray();
        depthVertArray.create(depthMesh, DisplayBufferBuilder.DISPLAY_MAPPING, null);

        JsonArray textureSize = jsonObject.get("texture_size").getAsJsonArray();
        imgWidth = textureSize.get(0).getAsInt(); imgHeight = textureSize.get(1).getAsInt();

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
        logic = DisplayNodeFactory.parse(resources, basePath, jsonObject.get("logic").getAsJsonObject());
    }

    public void addQuad(String slotName, int x1, int y1, int x2, int y2, float u1, float v1, float u2, float v2, int color) {
        DisplaySlot slot = slots.get(slotName);
        if (slot == null) throw new IllegalArgumentException("No slot: " + slotName);
        for (DisplaySlot.SlotFace face : slot.faces) {
            colorVertexConsumer.vertex(currentPose, face.getPositionAt(u1, v1), color, (float)x1 / imgWidth, (float)y1 / imgHeight);
            colorVertexConsumer.vertex(currentPose, face.getPositionAt(u1, v2), color, (float)x1 / imgWidth, (float)y2 / imgHeight);
            colorVertexConsumer.vertex(currentPose, face.getPositionAt(u2, v2), color, (float)x2 / imgWidth, (float)y2 / imgHeight);
            colorVertexConsumer.vertex(currentPose, face.getPositionAt(u2, v1), color, (float)x2 / imgWidth, (float)y1 / imgHeight);
        }
    }

    public void addHAreaQuad(String slotName, int x1, int y1, int width, int height, float u1, float v1, float u2, float v2, int xl, int xr) {
        addQuad(slotName, xl, y1, xr, y1 + height,
                u1 + (u2 - u1) * ((float)(xl - x1) / width), v1, u1 + (u2 - u1) * ((float)(xr - x1) / width), v2, -1);
    }

    private Vector3f transformed(Vector3f src) {
        return currentPose.transform(src);
    }

    public DisplayTemplate getTemplate(String key) {
        DisplayTemplate result = templates.get(key);
        if (result == null) throw new IllegalArgumentException("No template: " + key);
        return result;
    }

    public void handleCar(TrainClient train, Matrix4f pose, int carNum, boolean doorLeftOpen, boolean doorRightOpen) {
        this.currentCarNum = carNum;
        this.currentCarDoorLeftOpen = doorLeftOpen;
        this.currentCarDoorRightOpen = doorRightOpen;
        this.currentPose = pose;
        logic.tick(this, train);

        for (DisplaySlot slot : slots.values()) {
            for (DisplaySlot.SlotFace face : slot.faces) {
                depthVertexConsumer.vertex(pose, face.getPositionAt(0, 0), -1, 0, 0);
                depthVertexConsumer.vertex(pose, face.getPositionAt(0, 1), -1, 0, 1);
                depthVertexConsumer.vertex(pose, face.getPositionAt(1, 1), -1, 1, 1);
                depthVertexConsumer.vertex(pose, face.getPositionAt(1, 0), -1, 1, 0);
            }
        }
    }

    public void drawImmediate() {
        if (!colorVertexConsumer.hasData()) return;

        MainClient.drawScheduler.shaderManager.setupShaderBatchState(colorMaterialProp, ShaderProp.DEFAULT);
        colorVertexConsumer.upload(colorMesh);
        RenderSystem.depthMask(false);
        colorVertArray.bind();
        colorMaterialProp.attrState.apply(colorVertArray);
        colorVertArray.draw();
        colorVertexConsumer.clear();
        MainClient.drawScheduler.shaderManager.cleanupShaderBatchState(colorMaterialProp, ShaderProp.DEFAULT);

        // Separately draw to depth buffer to avoid z-fighting between color faces
        MainClient.drawScheduler.shaderManager.setupShaderBatchState(depthMaterialProp, ShaderProp.DEFAULT);
        depthVertexConsumer.upload(depthMesh);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        depthVertArray.bind();
        depthMaterialProp.attrState.apply(depthVertArray);
        depthVertArray.draw();
        depthVertexConsumer.clear();

        RenderSystem.colorMask(true, true, true, true);
        MainClient.drawScheduler.shaderManager.cleanupShaderBatchState(depthMaterialProp, ShaderProp.DEFAULT);
    }

    @Override
    public void close() {
        colorMesh.close();
        colorVertArray.close();
        depthMesh.close();
        depthVertArray.close();
    }

}
