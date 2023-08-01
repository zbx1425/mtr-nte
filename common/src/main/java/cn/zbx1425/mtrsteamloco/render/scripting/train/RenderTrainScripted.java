package cn.zbx1425.mtrsteamloco.render.scripting.train;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.PoseStackUtil;
import cn.zbx1425.sowcer.math.Vector3f;
import mtr.data.TrainClient;
import mtr.render.TrainRendererBase;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class RenderTrainScripted extends TrainRendererBase {

    private final ScriptHolder typeScripting;
    private final TrainClient train;
    private final TrainScriptContext trainScripting;

    public RenderTrainScripted(ScriptHolder typeScripting) {
        this.typeScripting = typeScripting;
        this.train = null;
        this.trainScripting = null;
    }

    private RenderTrainScripted(RenderTrainScripted base, TrainClient trainClient) {
        this.typeScripting = base.typeScripting;
        this.train = trainClient;
        this.trainScripting = new TrainScriptContext(trainClient);
    }

    @Override
    public TrainRendererBase createTrainInstance(TrainClient trainClient) {
        RenderTrainScripted result = new RenderTrainScripted(this, trainClient);
        return result;
    }

    @Override
    public void renderCar(int carIndex, double x, double y, double z, float yaw, float pitch, boolean doorLeftOpen, boolean doorRightOpen) {
        assert train != null && trainScripting != null;
        if (RenderUtil.shouldSkipRenderTrain(train)) return;
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
        if (carIndex == train.trainCars - 1) {
            trainScripting.extraFinished();
            trainScripting.tryCallRender(typeScripting);
        }
    }

    @Override
    public void renderConnection(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch) {
        assert train != null && trainScripting != null;
        if (RenderUtil.shouldSkipRenderTrain(train)) return;
        if (isTranslucentBatch) return;

        final BlockPos posAverage = applyAverageTransform(train.getViewOffset(), x, y, z);
        if (posAverage == null) return;
        matrices.translate(x, y, z);
        PoseStackUtil.rotY(matrices, (float) Math.PI + yaw);
        final boolean hasPitch = pitch < 0 ? train.transportMode.hasPitchAscending : train.transportMode.hasPitchDescending;
        PoseStackUtil.rotX(matrices, hasPitch ? pitch : 0);
        final int light = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, posAverage), world.getBrightness(LightLayer.SKY, posAverage));
        Matrix4f pose = new Matrix4f(matrices.last().pose());
        synchronized (trainScripting) {
            trainScripting.scriptResult.commitConn(0, MainClient.drawScheduler, pose, light);
        }

        matrices.popPose();
    }

    @Override
    public void renderBarrier(Vec3 vec3, Vec3 vec31, Vec3 vec32, Vec3 vec33, Vec3 vec34, Vec3 vec35, Vec3 vec36, Vec3 vec37, double v, double v1, double v2, float v3, float v4) {

    }
}
