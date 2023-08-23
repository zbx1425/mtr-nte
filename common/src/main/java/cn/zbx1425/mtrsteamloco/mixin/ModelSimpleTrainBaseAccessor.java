package cn.zbx1425.mtrsteamloco.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mtr.model.ModelSimpleTrainBase;
import mtr.model.ModelTrainBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelSimpleTrainBase.class)
public interface ModelSimpleTrainBaseAccessor {

    @Invoker
    void invokeRender(PoseStack matrices, VertexConsumer vertices, ModelTrainBase.RenderStage renderStage, int light, float doorLeftX, float doorRightX, float doorLeftZ, float doorRightZ, int currentCar, int trainCars, boolean head1IsFront, boolean renderDetails);

    @Invoker
    void invokeRenderWindowPositions(PoseStack matrices, VertexConsumer vertices, ModelTrainBase.RenderStage renderStage, int light, int position, boolean renderDetails, float doorLeftX, float doorRightX, float doorLeftZ, float doorRightZ, boolean isEnd1Head, boolean isEnd2Head);

    @Invoker
    void invokeRenderDoorPositions(PoseStack matrices, VertexConsumer vertices, ModelTrainBase.RenderStage renderStage, int light, int position, boolean renderDetails, float doorLeftX, float doorRightX, float doorLeftZ, float doorRightZ, boolean isEnd1Head, boolean isEnd2Head);

    @Invoker
    void invokeRenderHeadPosition1(PoseStack matrices, VertexConsumer vertices, ModelTrainBase.RenderStage renderStage, int light, int position, boolean renderDetails, float doorLeftX, float doorRightX, float doorLeftZ, float doorRightZ, boolean useHeadlights);

    @Invoker
    void invokeRenderHeadPosition2(PoseStack matrices, VertexConsumer vertices, ModelTrainBase.RenderStage renderStage, int light, int position, boolean renderDetails, float doorLeftX, float doorRightX, float doorLeftZ, float doorRightZ, boolean useHeadlights);

    @Invoker
    void invokeRenderEndPosition1(PoseStack matrices, VertexConsumer vertices, ModelTrainBase.RenderStage renderStage, int light, int position, boolean renderDetails, float doorLeftX, float doorRightX, float doorLeftZ, float doorRightZ);

    @Invoker
    void invokeRenderEndPosition2(PoseStack matrices, VertexConsumer vertices, ModelTrainBase.RenderStage renderStage, int light, int position, boolean renderDetails, float doorLeftX, float doorRightX, float doorLeftZ, float doorRightZ);

    @Invoker(remap = false)
    int[] invokeGetWindowPositions();

    @Invoker(remap = false)
    int[] invokeGetDoorPositions();

    @Invoker(remap = false)
    int[] invokeGetEndPositions();
}
