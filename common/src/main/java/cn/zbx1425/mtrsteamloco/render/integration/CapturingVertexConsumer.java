package cn.zbx1425.mtrsteamloco.render.integration;

import cn.zbx1425.mtrsteamloco.mixin.ModelPartAccessor;
import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.Face;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.Vertex;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mtr.model.ModelTrainBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.IntStream;

public class CapturingVertexConsumer implements VertexConsumer {

    RawModel[] models = new RawModel[5];
    RawMesh[] buildingMeshes = new RawMesh[5];
    Vertex buildingVertex = new Vertex();

    public CapturingVertexConsumer() {
        Arrays.setAll(models, ignored -> new RawModel());
    }

    public void captureModelPart(ModelPart modelPart) {
        dumpModelPartQuads(modelPart, new PoseStack(), this, 0, 0);
    }

    // Sodium mixins into ModelPart.compile, so cannot directly call that
    public static void dumpModelPartQuads(ModelPart modelPart, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        if (modelPart.visible) {
            if (!((ModelPartAccessor)(Object)modelPart).getCubes().isEmpty() || !((ModelPartAccessor)(Object)modelPart).getChildren().isEmpty()) {
                poseStack.pushPose();
                modelPart.translateAndRotate(poseStack);
                for (ModelPart.Cube cube : ((ModelPartAccessor)(Object)modelPart).getCubes()) {
                    cube.compile(poseStack.last(), vertexConsumer, packedLight,packedOverlay, 1, 1, 1, 1);
                }
                for (ModelPart child : ((ModelPartAccessor)(Object)modelPart).getChildren().values()) {
                    dumpModelPartQuads(child, poseStack, vertexConsumer, packedLight, packedOverlay);
                }
                poseStack.popPose();
            }
        }
    }

    public void beginStage(ResourceLocation texture, ModelTrainBase.RenderStage stage) {
        MaterialProp materialProp = new MaterialProp();
        materialProp.texture = texture;
        switch (stage) {
            case LIGHTS -> {
                materialProp.shaderName = "rendertype_beacon_beam";
                materialProp.cutoutHack = true;
            }
            case ALWAYS_ON_LIGHTS -> {
                materialProp.shaderName = "rendertype_beacon_beam";
                materialProp.translucent = true;
                materialProp.writeDepthBuf = false;
            }
            case EXTERIOR -> {
                materialProp.shaderName = "rendertype_entity_cutout";
            }
            case INTERIOR -> {
                materialProp.shaderName = "rendertype_entity_cutout";
                materialProp.attrState.setLightmapUV(15 << 4 | 15 << 20);
            }
            case INTERIOR_TRANSLUCENT -> {
                materialProp.shaderName = "rendertype_entity_translucent_cull";
                materialProp.translucent = true;
                materialProp.attrState.setLightmapUV(15 << 4 | 15 << 20);
            }
        }
        for (int i = 0; i < models.length; i++) {
            buildingMeshes[i] = models[i].meshList.computeIfAbsent(materialProp, RawMesh::new);
        }
    }

    public void reset() {
        Arrays.setAll(models, ignored -> new RawModel());
        Arrays.fill(buildingMeshes, null);
        buildingVertex = new Vertex();
    }

    @Override
    public @NotNull VertexConsumer vertex(double x, double y, double z) {
        buildingVertex.position = new Vector3f((float) x, (float) y, (float) z);
        return this;
    }

    @Override
    public @NotNull VertexConsumer color(int red, int green, int blue, int alpha) {
        // Unused
        return this;
    }

    @Override
    public @NotNull VertexConsumer uv(float u, float v) {
        buildingVertex.u = u;
        buildingVertex.v = v;
        return this;
    }

    @Override
    public @NotNull VertexConsumer overlayCoords(int u, int v) {
        // Unused
        return this;
    }

    @Override
    public @NotNull VertexConsumer uv2(int u, int v) {
        // Unused
        return this;
    }

    @Override
    public @NotNull VertexConsumer normal(float x, float y, float z) {
        buildingVertex.normal = new Vector3f(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        int meshToUse;
        if (Math.abs(buildingVertex.position.z()) > TrainModelCapture.DOOR_OFFSET / 2) {
            boolean xSign = buildingVertex.position.x() > 0;
            boolean zSign = buildingVertex.position.z() > 0;
            meshToUse = (xSign ? 0 : 2) + (zSign ? 0 : 1) + 1;
            buildingVertex.position.add(new Vector3f(0, 0,
                    zSign ? -TrainModelCapture.DOOR_OFFSET : TrainModelCapture.DOOR_OFFSET
            ));
        } else {
            meshToUse = 0;
        }
        RawMesh buildingMesh = buildingMeshes[meshToUse];
        buildingMesh.vertices.add(buildingVertex);
        if (buildingMesh.vertices.size() % 4 == 0) {
            buildingMesh.faces.add(new Face(
                    IntStream.range(buildingMesh.vertices.size() - 4, buildingMesh.vertices.size()).toArray()));
        }
        buildingVertex = new Vertex();
    }

    @Override
    public void defaultColor(int defaultR, int defaultG, int defaultB, int defaultA) {

    }

    @Override
    public void unsetDefaultColor() {

    }
}
