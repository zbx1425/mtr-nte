package cn.zbx1425.mtrsteamloco.render.train;

import mtr.data.TrainClient;
import mtr.render.TrainRendererBase;
import net.minecraft.world.phys.Vec3;

public class NoopTrainRenderer extends TrainRendererBase {

    public static final NoopTrainRenderer INSTANCE = new NoopTrainRenderer();

    @Override
    public TrainRendererBase createTrainInstance(TrainClient trainClient) {
        return this;
    }

    @Override
    public void renderCar(int i, double v, double v1, double v2, float v3, float v4, boolean b, boolean b1) {

    }

    @Override
    public void renderConnection(Vec3 vec3, Vec3 vec31, Vec3 vec32, Vec3 vec33, Vec3 vec34, Vec3 vec35, Vec3 vec36, Vec3 vec37, double v, double v1, double v2, float v3, float v4) {

    }

    @Override
    public void renderBarrier(Vec3 vec3, Vec3 vec31, Vec3 vec32, Vec3 vec33, Vec3 vec34, Vec3 vec35, Vec3 vec36, Vec3 vec37, double v, double v1, double v2, float v3, float v4) {

    }
}
