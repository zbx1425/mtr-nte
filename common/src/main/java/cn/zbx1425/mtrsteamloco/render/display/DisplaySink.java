package cn.zbx1425.mtrsteamloco.render.display;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.display.node.DisplayNode;
import cn.zbx1425.mtrsteamloco.render.display.node.DisplayNodeFactory;
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
import com.mojang.blaze3d.vertex.VertexFormat;
import mtr.data.TrainClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL11;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public class DisplaySink implements Closeable {

    private final Map<String, DisplaySlot> slots;

    private final MaterialProp colorMaterialProp;
    private final MaterialProp depthMaterialProp;

    private final Mesh colorMesh;
    private final VertArray colorVertArray;
    private final RawMesh colorRawMesh;
    private final RawMeshBuilder colorVertexConsumer;

    private final Mesh depthMesh;
    private final VertArray depthVertArray;
    private final RawMesh depthRawMesh;
    private final RawMeshBuilder depthVertexConsumer;

    private final DisplayNode logic;

    private final int imgWidth, imgHeight;

    private Matrix4f currentPose;
    public int currentCarNum;
    public boolean currentCarDoorLeftOpen, currentCarDoorRightOpen;

    public static final VertAttrMapping DISPLAY_MAPPING = new VertAttrMapping.Builder()
            .set(VertAttrType.POSITION, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.COLOR, VertAttrSrc.GLOBAL)
            .set(VertAttrType.UV_TEXTURE, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.UV_OVERLAY, VertAttrSrc.GLOBAL)
            .set(VertAttrType.UV_LIGHTMAP, VertAttrSrc.GLOBAL)
            .set(VertAttrType.NORMAL, VertAttrSrc.GLOBAL)
            .set(VertAttrType.MATRIX_MODEL, VertAttrSrc.GLOBAL)
            .build();


    public DisplaySink(ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject, Map<String, DisplaySlot> slots) throws IOException {
        ResourceLocation textureLocation = ResourceUtil.resolveRelativePath(basePath, jsonObject.get("texture").getAsString(), ".png");

        colorMaterialProp = new MaterialProp();
        colorMaterialProp.texture = textureLocation;
        colorMaterialProp.shaderName = "rendertype_entity_cutout";
        colorMaterialProp.attrState.setLightmapUV(15 << 4 | 15 << 20);
        colorMaterialProp.attrState.setNormal(new Vector3f(0, 1, 0));
        colorMaterialProp.attrState.setColor(-1);
        colorMaterialProp.attrState.setOverlayUVNoOverlay();

        colorRawMesh = new RawMesh(colorMaterialProp);
        colorVertexConsumer = new RawMeshBuilder(colorRawMesh, VertexFormat.Mode.QUADS);
        colorMesh = new Mesh(new VertBuf(), new IndexBuf(0, GL11.GL_UNSIGNED_INT), colorMaterialProp);
        colorVertArray = new VertArray();
        colorVertArray.create(colorMesh, DISPLAY_MAPPING, null);

        depthMaterialProp = new MaterialProp();
        depthMaterialProp.texture = MaterialProp.WHITE_TEXTURE_LOCATION;
        depthMaterialProp.shaderName = "rendertype_entity_cutout";
        depthMaterialProp.attrState.setLightmapUV(15 << 4 | 15 << 20);
        depthMaterialProp.attrState.setNormal(new Vector3f(0, 1, 0));
        depthMaterialProp.attrState.setColor(-1);
        depthMaterialProp.attrState.setOverlayUVNoOverlay();

        depthRawMesh = new RawMesh(depthMaterialProp);
        depthVertexConsumer = new RawMeshBuilder(depthRawMesh, VertexFormat.Mode.QUADS);
        depthMesh = new Mesh(new VertBuf(), new IndexBuf(0, GL11.GL_UNSIGNED_INT), depthMaterialProp);
        depthVertArray = new VertArray();
        depthVertArray.create(depthMesh, DISPLAY_MAPPING, null);

        JsonArray textureSize = jsonObject.get("texture_size").getAsJsonArray();
        imgWidth = textureSize.get(0).getAsInt(); imgHeight = textureSize.get(1).getAsInt();

        this.slots = slots;

        logic = DisplayNodeFactory.parse(resources, basePath, jsonObject.get("logic").getAsJsonObject());
    }

    public void addQuad(String slotName, int x1, int y1, int x2, int y2, float u1, float v1, float u2, float v2) {
        DisplaySlot slot = slots.get(slotName);
        if (slot == null) throw new IllegalArgumentException("No slot: " + slotName);
        for (DisplaySlot.SlotFace face : slot.faces) {
            colorVertexConsumer.vertex(transformed(face.getPositionAt(u1, v1))).uv((float)x1 / imgWidth, (float)y1 / imgHeight).endVertex();
            colorVertexConsumer.vertex(transformed(face.getPositionAt(u1, v2))).uv((float)x1 / imgWidth, (float)y2 / imgHeight).endVertex();
            colorVertexConsumer.vertex(transformed(face.getPositionAt(u2, v2))).uv((float)x2 / imgWidth, (float)y2 / imgHeight).endVertex();
            colorVertexConsumer.vertex(transformed(face.getPositionAt(u2, v1))).uv((float)x2 / imgWidth, (float)y1 / imgHeight).endVertex();
        }
    }

    private Vector3f transformed(Vector3f src) {
        return currentPose.transform(src);
    }

    public void handleCar(TrainClient train, Matrix4f pose, int carNum, boolean doorLeftOpen, boolean doorRightOpen) {
        this.currentCarNum = carNum;
        this.currentCarDoorLeftOpen = doorLeftOpen;
        this.currentCarDoorRightOpen = doorRightOpen;
        this.currentPose = pose;
        logic.tick(this, train);

        for (DisplaySlot slot : slots.values()) {
            for (DisplaySlot.SlotFace face : slot.faces) {
                depthVertexConsumer.vertex(transformed(face.getPositionAt(0, 0))).uv(0, 0).endVertex();
                depthVertexConsumer.vertex(transformed(face.getPositionAt(0, 1))).uv(0, 1).endVertex();
                depthVertexConsumer.vertex(transformed(face.getPositionAt(1, 1))).uv(1, 1).endVertex();
                depthVertexConsumer.vertex(transformed(face.getPositionAt(1, 0))).uv(1, 0).endVertex();
            }
        }
    }

    public void drawImmediate() {
        if (colorRawMesh.vertices.size() == 0) return;

        MainClient.drawScheduler.shaderManager.setupShaderBatchState(colorMaterialProp, ShaderProp.DEFAULT);
        colorRawMesh.upload(colorMesh, DISPLAY_MAPPING);
        RenderSystem.depthMask(false);
        colorVertArray.bind();
        colorMaterialProp.attrState.apply(colorVertArray);
        colorVertArray.draw();
        colorRawMesh.clear();
        MainClient.drawScheduler.shaderManager.cleanupShaderBatchState(colorMaterialProp, ShaderProp.DEFAULT);

        // Separately draw to depth buffer to avoid z-fighting between color faces
        MainClient.drawScheduler.shaderManager.setupShaderBatchState(depthMaterialProp, ShaderProp.DEFAULT);
        depthRawMesh.upload(depthMesh, DISPLAY_MAPPING);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        depthVertArray.bind();
        depthMaterialProp.attrState.apply(depthVertArray);
        depthVertArray.draw();
        depthRawMesh.clear();

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
