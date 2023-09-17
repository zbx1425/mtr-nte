package cn.zbx1425.mtrsteamloco.render.scripting.train;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.PoseStackUtil;
import cn.zbx1425.sowcer.math.Vector3f;
import mtr.data.TrainClient;
import mtr.render.RenderTrains;
import mtr.render.TrainRendererBase;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class ScriptedTrainRenderer extends TrainRendererBase {

    private final ScriptHolder typeScripting;
    private final TrainClient train;
    private final TrainScriptContext trainScripting;

    public ScriptedTrainRenderer(ScriptHolder typeScripting) {
        this.typeScripting = typeScripting;
        this.train = null;
        this.trainScripting = null;
    }

    private ScriptedTrainRenderer(ScriptedTrainRenderer base, TrainClient trainClient) {
        this.typeScripting = base.typeScripting;
        this.train = trainClient;
        this.trainScripting = new TrainScriptContext(trainClient);
    }

    private static final WeakHashMap<TrainClient, ScriptedTrainRenderer> activeRenderers = new WeakHashMap<>();

    @Override
    public TrainRendererBase createTrainInstance(TrainClient trainClient) {
        synchronized (activeRenderers) {
            ScriptedTrainRenderer result = new ScriptedTrainRenderer(this, trainClient);
            activeRenderers.put(trainClient, result);
            return result;
        }
    }

    public static void reInitiateScripts() {
        synchronized (activeRenderers) {
            ScriptHolder.resetRunner();
            for (Map.Entry<TrainClient, ScriptedTrainRenderer> entry : activeRenderers.entrySet()) {
                entry.getValue().trainScripting.tryCallDispose(entry.getValue().typeScripting);
            }
        }
    }

    public static void disposeInactiveScripts() {
        synchronized (activeRenderers) {
            for (Iterator<Map.Entry<TrainClient, ScriptedTrainRenderer>> it = activeRenderers.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<TrainClient, ScriptedTrainRenderer> entry = it.next();
                if (entry.getKey().isRemoved) {
                    entry.getValue().trainScripting.tryCallDispose(entry.getValue().typeScripting);
                    it.remove();
                }
            }
        }
    }

    @Override
    public void renderCar(int carIndex, double x, double y, double z, float yaw, float pitch, boolean doorLeftOpen, boolean doorRightOpen) {
        assert train != null && trainScripting != null;
        if (RenderUtil.shouldSkipRenderTrain(train)) return;

        if (trainScripting.baseRenderer != null) {
            trainScripting.baseRenderer.renderCar(carIndex, x, y, z, yaw, pitch, doorLeftOpen, doorRightOpen);
        }

        if (isTranslucentBatch) return;

        final BlockPos posAverage = applyAverageTransform(train.getViewOffset(), x, y, z);
        if (posAverage == null) return;
        matrices.translate(x, y, z);
        PoseStackUtil.rotY(matrices, (float) Math.PI + yaw);
        final boolean hasPitch = pitch < 0 ? train.transportMode.hasPitchAscending : train.transportMode.hasPitchDescending;
        PoseStackUtil.rotX(matrices, hasPitch ? pitch : 0);
        final int light = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, posAverage), world.getBrightness(LightLayer.SKY, posAverage));
        Matrix4f drawPose = new Matrix4f(matrices.last().pose());

        Vector3f carPos = new Vector3f((float)x, (float)y, (float)z);
        Vec3 offset = train.vehicleRidingClient.getVehicleOffset();
        if (offset != null) {
            carPos.add((float)offset.x, (float)offset.y, (float)offset.z);
        }
        Matrix4f worldPose = new Matrix4f();
        worldPose.translate(carPos.x(), carPos.y(), carPos.z());
        worldPose.rotateY((float) Math.PI + yaw);
        worldPose.rotateX(hasPitch ? pitch : 0);

        synchronized (trainScripting) {
            trainScripting.scriptResult.commitCar(carIndex, MainClient.drawScheduler, drawPose, worldPose, light);
        }
        matrices.popPose();

        trainScripting.trainExtraWriting.doorLeftOpen[carIndex] = doorLeftOpen;
        trainScripting.trainExtraWriting.doorRightOpen[carIndex] = doorRightOpen;
        trainScripting.trainExtraWriting.lastWorldPose[carIndex] = worldPose;
        trainScripting.trainExtraWriting.isInDetailDistance |= posAverage.distSqr(camera.getBlockPosition()) <= RenderTrains.DETAIL_RADIUS_SQUARED;

        if (carIndex == train.trainCars - 1) {
            trainScripting.extraFinished();
            trainScripting.tryCallRender(typeScripting);
        }
    }

    @Override
    public void renderConnection(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch) {
        assert train != null && trainScripting != null;
        if (RenderUtil.shouldSkipRenderTrain(train)) return;

        if (trainScripting.baseRenderer != null) {
            trainScripting.baseRenderer.renderConnection(prevPos1, prevPos2, prevPos3, prevPos4, thisPos1, thisPos2, thisPos3, thisPos4, x, y, z, yaw, pitch);
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

        if (trainScripting.baseRenderer != null) {
            trainScripting.baseRenderer.renderConnection(prevPos1, prevPos2, prevPos3, prevPos4, thisPos1, thisPos2, thisPos3, thisPos4, x, y, z, yaw, pitch);
        }
    }

}
