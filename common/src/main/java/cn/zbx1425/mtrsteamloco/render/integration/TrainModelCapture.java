package cn.zbx1425.mtrsteamloco.render.integration;

import cn.zbx1425.mtrsteamloco.mixin.ModelSimpleTrainBaseAccessor;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.RawModel;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.model.ModelTrainBase;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Map;

public class TrainModelCapture {

    public static final float DOOR_OFFSET = 2048;

    public static CaptureResult captureModels(ModelTrainBase mtrModel, ResourceLocation texture) {
        CaptureResult result = new CaptureResult();

        CapturingVertexConsumer vertices = new CapturingVertexConsumer();
        PoseStack matrices = new PoseStack();
        int light = LightTexture.FULL_BRIGHT;
        float doorLeftX = 0, doorRightX = 0, doorLeftZ = DOOR_OFFSET * 16, doorRightZ = DOOR_OFFSET * 16;
        boolean renderDetails = true, isEnd1Head = false, isEnd2Head = false, head1IsFront = true;

        vertices.reset();
        for (ModelTrainBase.RenderStage renderStage : ModelTrainBase.RenderStage.values()) {
            vertices.beginStage(texture, renderStage);
            for (final int position : ((ModelSimpleTrainBaseAccessor) mtrModel).invokeGetWindowPositions()) {
                ((ModelSimpleTrainBaseAccessor) mtrModel).invokeRenderWindowPositions(matrices, vertices, renderStage, light, position, renderDetails, doorLeftX, doorRightX, doorLeftZ, doorRightZ, isEnd1Head, isEnd2Head);
            }
            for (final int position : ((ModelSimpleTrainBaseAccessor) mtrModel).invokeGetDoorPositions()) {
                ((ModelSimpleTrainBaseAccessor) mtrModel).invokeRenderDoorPositions(matrices, vertices, renderStage, light, position, renderDetails, doorLeftX, doorRightX, doorLeftZ, doorRightZ, isEnd1Head, isEnd2Head);
            }
        }
        for (RawModel model : vertices.models) model.applyRotation(Vector3f.XP, 180);
        result.modelBody = vertices.models[0];
        result.modelDoors = Arrays.copyOfRange(vertices.models, 1, 5);

        vertices.reset();
        for (ModelTrainBase.RenderStage renderStage : ModelTrainBase.RenderStage.values()) {
            if (renderStage == ModelTrainBase.RenderStage.ALWAYS_ON_LIGHTS) continue;
            vertices.beginStage(texture, renderStage);
            ((ModelSimpleTrainBaseAccessor)mtrModel).invokeRenderHeadPosition2(matrices, vertices, renderStage, light, ((ModelSimpleTrainBaseAccessor) mtrModel).invokeGetEndPositions()[1], renderDetails, doorLeftX, doorRightX, doorLeftZ, doorRightZ, head1IsFront);
        }
        for (RawModel model : vertices.models) model.applyRotation(Vector3f.XP, 180);
        result.modelHead = vertices.models[0];

        vertices.reset();
        for (ModelTrainBase.RenderStage renderStage : ModelTrainBase.RenderStage.values()) {
            if (renderStage == ModelTrainBase.RenderStage.ALWAYS_ON_LIGHTS) continue;
            vertices.beginStage(texture, renderStage);
            ((ModelSimpleTrainBaseAccessor)mtrModel).invokeRenderEndPosition1(matrices, vertices, renderStage, light, ((ModelSimpleTrainBaseAccessor) mtrModel).invokeGetEndPositions()[0], renderDetails, doorLeftX, doorRightX, doorLeftZ, doorRightZ);
        }
        for (RawModel model : vertices.models) model.applyRotation(Vector3f.XP, 180);
        result.modelEnd = vertices.models[0];

        vertices.reset();
        vertices.beginStage(texture, ModelTrainBase.RenderStage.ALWAYS_ON_LIGHTS);
        ((ModelSimpleTrainBaseAccessor)mtrModel).invokeRenderHeadPosition2(matrices, vertices, ModelTrainBase.RenderStage.ALWAYS_ON_LIGHTS, light, ((ModelSimpleTrainBaseAccessor) mtrModel).invokeGetEndPositions()[1], renderDetails, doorLeftX, doorRightX, doorLeftZ, doorRightZ, true);
        for (RawModel model : vertices.models) model.applyRotation(Vector3f.XP, 180);
        result.modelHeadlight = vertices.models[0];

        vertices.reset();
        vertices.beginStage(texture, ModelTrainBase.RenderStage.ALWAYS_ON_LIGHTS);
        ((ModelSimpleTrainBaseAccessor)mtrModel).invokeRenderHeadPosition2(matrices, vertices, ModelTrainBase.RenderStage.ALWAYS_ON_LIGHTS, light, ((ModelSimpleTrainBaseAccessor) mtrModel).invokeGetEndPositions()[1], renderDetails, doorLeftX, doorRightX, doorLeftZ, doorRightZ, false);
        for (RawModel model : vertices.models) model.applyRotation(Vector3f.XP, 180);
        result.modelTaillight = vertices.models[0];

        return result;
    }

    public static class CaptureResult {

        public RawModel modelBody;
        public RawModel[] modelDoors;
        public RawModel modelHead;
        public RawModel modelEnd;
        public RawModel modelHeadlight;
        public RawModel modelTaillight;

        public Map<String, RawModel> getNamedModels() {
            return Map.of(
                    "body", modelBody,
                    "doorXPZN", modelDoors[0], "doorXPZP", modelDoors[1],
                    "doorXNZN", modelDoors[2], "doorXNZP", modelDoors[3],
                    "head", modelHead, "end", modelEnd,
                    "headlight", modelHeadlight, "taillight", modelTaillight
            );
        }
    }
}
