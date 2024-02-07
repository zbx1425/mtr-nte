package cn.zbx1425.mtrsteamloco.render.scripting.train;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptContextManager;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.PoseStackUtil;
import cn.zbx1425.sowcer.math.Vector3f;
import mtr.client.ClientData;
import mtr.data.TrainClient;
import mtr.render.RenderTrains;
import mtr.render.TrainRendererBase;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ScriptedTrainRenderer extends TrainRendererBase {

    public final ScriptHolder typeScripting;
    public final TrainRendererBase baseRenderer;

    private final TrainClient train;
    private final TrainScriptContext trainScripting;

    public ScriptedTrainRenderer(ScriptHolder typeScripting, TrainRendererBase baseRenderer) {
        this.typeScripting = typeScripting;
        this.baseRenderer = baseRenderer;
        this.train = null;
        this.trainScripting = null;
    }

    private ScriptedTrainRenderer(ScriptedTrainRenderer base, TrainClient trainClient) {
        this.typeScripting = base.typeScripting;
        this.baseRenderer = base.baseRenderer == null ? null : base.baseRenderer.createTrainInstance(trainClient);
        this.train = trainClient;
        this.trainScripting = new TrainScriptContext(trainClient);
    }

    @Override
    public TrainRendererBase createTrainInstance(TrainClient trainClient) {
        ScriptedTrainRenderer result = new ScriptedTrainRenderer(this, trainClient);
        return result;
    }

    @Override
    public void renderCar(int carIndex, double x, double y, double z, float yaw, float pitch, boolean doorLeftOpen, boolean doorRightOpen) {
        assert train != null && trainScripting != null;
        boolean shouldRender = !RenderUtil.shouldSkipRenderTrain(train);

        if (shouldRender && baseRenderer != null) {
            baseRenderer.renderCar(carIndex, x, y, z, yaw, pitch, doorLeftOpen, doorRightOpen);
        }

        if (isTranslucentBatch) return;

        final BlockPos posAverage = applyAverageTransform(train.getViewOffset(), x, y, z);
        Vector3f carPos = new Vector3f((float)x, (float)y, (float)z);
        Vec3 offset = train.vehicleRidingClient.getVehicleOffset();
        if (offset != null) {
            carPos.add((float)offset.x, (float)offset.y, (float)offset.z);
        }
        final boolean hasPitch = pitch < 0 ? train.transportMode.hasPitchAscending : train.transportMode.hasPitchDescending;
        Matrix4f worldPose = new Matrix4f();
        worldPose.translate(carPos.x(), carPos.y(), carPos.z());
        worldPose.rotateY((float) Math.PI + yaw);
        worldPose.rotateX(hasPitch ? pitch : 0);
        trainScripting.trainExtraWriting.doorLeftOpen[carIndex] = doorLeftOpen;
        trainScripting.trainExtraWriting.doorRightOpen[carIndex] = doorRightOpen;
        trainScripting.trainExtraWriting.lastWorldPose[carIndex] = worldPose;
        trainScripting.trainExtraWriting.lastCarPosition[carIndex] = carPos.copy();
        trainScripting.trainExtraWriting.lastCarRotation[carIndex] = new Vector3f(hasPitch ? pitch : 0, (float) Math.PI + yaw, 0);
        trainScripting.trainExtraWriting.isInDetailDistance |= posAverage != null
                && posAverage.distSqr(camera.getBlockPosition()) <= RenderTrains.DETAIL_RADIUS_SQUARED;
        trainScripting.trainExtraWriting.shouldRender = shouldRender;

        if (posAverage == null) {
            if (carIndex == train.trainCars - 1) {
                // So it's outside visible range, but still need to call render function
                trainScripting.extraFinished();
                typeScripting.tryCallRenderFunctionAsync(trainScripting);
            }
            return;
        }

        matrices.translate(x, y, z);
        PoseStackUtil.rotY(matrices, (float) Math.PI + yaw);
        PoseStackUtil.rotX(matrices, hasPitch ? pitch : 0);
        final int light = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, posAverage), world.getBrightness(LightLayer.SKY, posAverage));
        Matrix4f drawPose = new Matrix4f(matrices.last().pose());
        if (shouldRender) {
            synchronized (trainScripting) {
                trainScripting.scriptResult.commitCar(carIndex, MainClient.drawScheduler, drawPose, worldPose, light);
            }
        }
        matrices.popPose();

        if (carIndex == train.trainCars - 1) {
            trainScripting.extraFinished();
            typeScripting.tryCallRenderFunctionAsync(trainScripting);
        }
    }

    @Override
    public void renderConnection(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch) {
        assert train != null && trainScripting != null;
        if (RenderUtil.shouldSkipRenderTrain(train)) return;

        if (baseRenderer != null) {
            baseRenderer.renderConnection(prevPos1, prevPos2, prevPos3, prevPos4, thisPos1, thisPos2, thisPos3, thisPos4, x, y, z, yaw, pitch);
        }

        if (isTranslucentBatch) return;

        final BlockPos posAverage = applyAverageTransform(train.getViewOffset(), x, y, z);
        if (posAverage == null) return;
        matrices.pushPose();
        matrices.translate(x, y, z);
        PoseStackUtil.rotY(matrices, (float) Math.PI + yaw);
        final boolean hasPitch = pitch < 0 ? train.transportMode.hasPitchAscending : train.transportMode.hasPitchDescending;
        PoseStackUtil.rotX(matrices, hasPitch ? pitch : 0);
        final int light = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, posAverage), world.getBrightness(LightLayer.SKY, posAverage));
        Matrix4f pose = new Matrix4f(matrices.last().pose());
        synchronized (trainScripting) {
            trainScripting.scriptResult.commitConn(0, MainClient.drawScheduler, pose, light);
            matrices.popPose();
            trainScripting.scriptResult.commitConnImmediate(0, matrices, vertexConsumers,
                    prevPos1, prevPos2, prevPos3, prevPos4, thisPos1, thisPos2, thisPos3, thisPos4, light);
        }

        matrices.popPose();
    }

    @Override
    public void renderBarrier(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch) {
        assert train != null && trainScripting != null;
        if (RenderUtil.shouldSkipRenderTrain(train)) return;

        if (baseRenderer != null) {
            baseRenderer.renderBarrier(prevPos1, prevPos2, prevPos3, prevPos4, thisPos1, thisPos2, thisPos3, thisPos4, x, y, z, yaw, pitch);
        }
    }

}
