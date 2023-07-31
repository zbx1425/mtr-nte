package cn.zbx1425.mtrsteamloco.render.integration;

import cn.zbx1425.mtrsteamloco.mixin.ModelTrainBaseAccessor;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.RawModel;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.model.ModelTrainBase;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;

public class TrainModelCapture {

    public static final float DOOR_OFFSET = 2048;

    public static RawModel[] captureModels(ModelTrainBase mtrModel, ResourceLocation texture, int currentCar, int trainCars) {
        CapturingVertexConsumer vertexConsumer = new CapturingVertexConsumer();
        PoseStack emptyPose = new PoseStack();
        for (ModelTrainBase.RenderStage stage : ModelTrainBase.RenderStage.values()) {
            vertexConsumer.beginStage(texture, stage);
            ((ModelTrainBaseAccessor)mtrModel).invokeRender(
                    emptyPose, vertexConsumer, stage, LightTexture.FULL_BRIGHT,
                    0, 0, DOOR_OFFSET * 16, DOOR_OFFSET * 16,
                    currentCar, trainCars, true, true
            );
        }
        for (RawModel model : vertexConsumer.models) {
            model.applyRotation(Vector3f.XP, 180);
        }
        return vertexConsumer.models;
    }

    public static ModelTrainBase getBuiltinTrainModel(String modelName) throws ReflectiveOperationException {
        return (ModelTrainBase)Class.forName("mtr.model." + modelName).getConstructor().newInstance();
    }
}
