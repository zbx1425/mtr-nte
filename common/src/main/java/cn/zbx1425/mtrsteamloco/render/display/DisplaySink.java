package cn.zbx1425.mtrsteamloco.render.display;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.display.node.DisplayNode;
import cn.zbx1425.mtrsteamloco.render.display.node.DisplayNodeFactory;
import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.model.Mesh;
import cn.zbx1425.sowcer.object.IndexBuf;
import cn.zbx1425.sowcer.object.VertArray;
import cn.zbx1425.sowcer.object.VertBuf;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.integration.RawMeshBuilder;
import cn.zbx1425.sowcerext.reuse.ModelManager;
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
import java.util.HashMap;
import java.util.Map;

public class DisplaySink implements Closeable {

    private final Map<String, DisplaySlot> slots;

    private final MaterialProp materialProp;

    private final Mesh mesh;
    private final VertArray vertArray;

    private final RawMesh rawMesh;
    private final RawMeshBuilder vertexConsumer;

    private final DisplayNode logic;

    private final int imgWidth, imgHeight;

    public int currentCarNum;
    public boolean currentCarDoorLeftOpen, currentCarDoorRightOpen;

    public DisplaySink(ResourceManager resources, ResourceLocation basePath, JsonObject jsonObject, Map<String, DisplaySlot> slots) throws IOException {
        ResourceLocation textureLocation = ResourceUtil.resolveRelativePath(basePath, jsonObject.get("texture").getAsString(), ".png");

        materialProp = new MaterialProp();
        materialProp.texture = textureLocation;
        materialProp.shaderName = "rendertype_entity_cutout";
        materialProp.attrState.setLightmapUV(15 << 4 | 15 << 20);

        rawMesh = new RawMesh(materialProp);
        vertexConsumer = new RawMeshBuilder(rawMesh, VertexFormat.Mode.QUADS);
        mesh = new Mesh(new VertBuf(), new IndexBuf(0, GL11.GL_UNSIGNED_INT), materialProp);
        vertArray = new VertArray();
        vertArray.create(mesh, ModelManager.DEFAULT_MAPPING, null);

        JsonArray textureSize = jsonObject.get("texture_size").getAsJsonArray();
        imgWidth = textureSize.get(0).getAsInt(); imgHeight = textureSize.get(1).getAsInt();

        this.slots = slots;

        logic = DisplayNodeFactory.parse(resources, basePath, jsonObject.get("logic").getAsJsonObject());
    }

    public void addQuad(String slotName, int x1, int y1, int x2, int y2, float u1, float v1, float u2, float v2) {
        DisplaySlot slot = slots.get(slotName);
        for (DisplaySlot.SlotFace face : slot.faces) {
            vertexConsumer.vertex(face.getPositionAt(u1, v1)).uv((float)x1 / imgWidth, (float)y1 / imgHeight).endVertex();
            vertexConsumer.vertex(face.getPositionAt(u1, v2)).uv((float)x1 / imgWidth, (float)y2 / imgHeight).endVertex();
            vertexConsumer.vertex(face.getPositionAt(u2, v2)).uv((float)x2 / imgWidth, (float)y2 / imgHeight).endVertex();
            vertexConsumer.vertex(face.getPositionAt(u2, v1)).uv((float)x2 / imgWidth, (float)y1 / imgHeight).endVertex();
        }
    }

    public void update(TrainClient train, Matrix4f pose, int carNum, boolean doorLeftOpen, boolean doorRightOpen) {
        this.currentCarNum = carNum;
        this.currentCarDoorLeftOpen = doorLeftOpen;
        this.currentCarDoorRightOpen = doorRightOpen;
        logic.tick(this, train);
    }

    public void drawImmediate() {
        MainClient.drawScheduler.shaderManager.setupShaderBatchState(materialProp, ShaderProp.DEFAULT);

        rawMesh.upload(mesh, ModelManager.DEFAULT_MAPPING);
        RenderSystem.depthMask(false);
        vertArray.bind();
        vertArray.draw();
        rawMesh.clear();

        for (String slotName : slots.keySet()) {
            addQuad(slotName, 0, 0, imgWidth, imgHeight, 0, 0, 1, 1);
        }
        rawMesh.upload(mesh, ModelManager.DEFAULT_MAPPING);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        vertArray.bind();
        vertArray.draw();
        rawMesh.clear();

        RenderSystem.colorMask(true, true, true, true);
        MainClient.drawScheduler.shaderManager.cleanupShaderBatchState(materialProp, ShaderProp.DEFAULT);
    }

    @Override
    public void close() {
        mesh.close();
        vertArray.close();
    }
}
