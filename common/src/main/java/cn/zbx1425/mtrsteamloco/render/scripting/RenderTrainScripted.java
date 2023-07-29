package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.PoseStackUtil;
import mtr.data.TrainClient;
import mtr.render.TrainRendererBase;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class RenderTrainScripted extends TrainRendererBase {

    private final TrainTypeScriptContext typeScripting;
    private final TrainClient train;
    private final TrainScriptContext trainScripting;

    public RenderTrainScripted(TrainTypeScriptContext typeScripting) {
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
        result.trainScripting.callCreate(typeScripting);
        return result;
    }

    @Override
    public void renderCar(int carIndex, double x, double y, double z, float yaw, float pitch, boolean doorLeftOpen, boolean doorRightOpen) {
        assert train != null && trainScripting != null;
        if (RenderUtil.shouldSkipRenderTrain(train)) return;
        if (isTranslucentBatch) return;

        final BlockPos posAverage = applyAverageTransform(train.getViewOffset(), x, y, z);
        if (posAverage == null) return;
        matrices.translate(x, y - 1, z);
        PoseStackUtil.rotY(matrices, (float) Math.PI + yaw);
        final boolean hasPitch = pitch < 0 ? train.transportMode.hasPitchAscending : train.transportMode.hasPitchDescending;
        PoseStackUtil.rotX(matrices, hasPitch ? pitch : 0);
        if (train.isReversed()) PoseStackUtil.rotY(matrices, (float) Math.PI);

        final int light = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, posAverage), world.getBrightness(LightLayer.SKY, posAverage));
        Matrix4f pose = new Matrix4f(matrices.last().pose());
        synchronized (trainScripting) {
            trainScripting.scriptResult.commit(carIndex, MainClient.drawScheduler, pose, light);
        }
        matrices.popPose();

        if (carIndex == train.trainCars - 1) {
            trainScripting.tryCallRender(typeScripting);
        }
    }

    @Override
    public void renderConnection(Vec3 vec3, Vec3 vec31, Vec3 vec32, Vec3 vec33, Vec3 vec34, Vec3 vec35, Vec3 vec36, Vec3 vec37, double v, double v1, double v2, float v3, float v4) {

    }

    @Override
    public void renderBarrier(Vec3 vec3, Vec3 vec31, Vec3 vec32, Vec3 vec33, Vec3 vec34, Vec3 vec35, Vec3 vec36, Vec3 vec37, double v, double v1, double v2, float v3, float v4) {

    }
}
