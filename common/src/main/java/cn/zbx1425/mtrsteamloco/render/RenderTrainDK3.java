package cn.zbx1425.mtrsteamloco.render;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.mixin.TrainAccessor;
import cn.zbx1425.sowcerext.multipart.MultipartContainer;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.animated.AnimatedLoader;
import cn.zbx1425.sowcerext.multipart.mi.MiLoader;
import cn.zbx1425.sowcerext.multipart.mi.MiScheduleHelper;
import com.google.common.collect.ImmutableSet;
import com.mojang.math.Vector3f;
import mtr.client.TrainClientRegistry;
import mtr.data.TrainClient;
import mtr.model.ModelBogie;
import mtr.render.RenderTrains;
import mtr.render.TrainRendererBase;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class RenderTrainDK3 extends TrainRendererBase {

    private static final MultipartContainer[] models = new MultipartContainer[4];

    protected final TrainClient train;
    protected final MultipartUpdateProp updateProp = new MultipartUpdateProp();
    protected final MiScheduleHelper scheduleHelper = new MiScheduleHelper();

    protected static final int MODEL_BODY_HEAD = 0;
    protected static final int MODEL_BODY_TAIL = 1;
    protected static final int MODEL_AUX_HEAD = 2;
    protected static final int MODEL_AUX_TAIL = 3;

    private final ImmutableSet<String> HIDE_LIST_MIDDLE = ImmutableSet.<String>builder()
            .add("conductor", "driver", "cabdoorl", "cabdoorr").build();
    private final ImmutableSet<String> HIDE_LIST_TAIL = ImmutableSet.<String>builder()
            .add("cabdoorlnm", "cabdoorrnm").build();
    private final ImmutableSet<String> HIDE_LIST_HEAD = ImmutableSet.<String>builder()
            .add("cabdoorl", "cabdoorr").build();

    public static void initGLModel(ResourceManager resourceManager) {
        try {
            MainClient.atlasManager.load(resourceManager, new ResourceLocation("mtrsteamloco:models/atlas/dk3.json"));
            models[MODEL_BODY_HEAD] = AnimatedLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/dk3/ch.animated"));
            models[MODEL_BODY_TAIL] = AnimatedLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/dk3/c.animated"));
            models[MODEL_AUX_HEAD] = MiLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/alex/dk3auxhead.json"));
            models[MODEL_AUX_TAIL] = MiLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/alex/dk3auxtail.json"));
        } catch (IOException e) {
            Main.LOGGER.error(e);
        }
    }

    protected MultipartContainer getModel(int index) {
        return models[index];
    }

    public RenderTrainDK3(TrainClient train) {
        this.train = train;
    }

    @Override
    public TrainRendererBase createTrainInstance(TrainClient trainClient) {
        return new RenderTrainDK3(trainClient);
    }

    @Override
    public void renderCar(int carIndex, double x, double y, double z, float yaw, float pitch, boolean isTranslucentBatch, float doorLeftValue, float doorRightValue, boolean opening, boolean head1IsFront, int stopIndex, boolean atPlatform, List<Long> routeIds) {
        if (RenderUtil.shouldSkipRenderTrain(train)) return;

        int carNum = head1IsFront ? carIndex : (train.trainCars - carIndex - 1);

        if (carNum > 1 && !(this instanceof RenderTrainDK3Mini)) return;

        if (isTranslucentBatch) {
            return;
        }

        final BlockPos posAverage = getPosAverage(train.getViewOffset(), x, y, z);
        if (posAverage == null) {
            return;
        }

        TrainAccessor trainAccessor = (TrainAccessor) train;
        // Get door delay of the first sec off
        final int dwellTicks = trainAccessor.getPath().get(trainAccessor.getNextStoppingIndex()).dwellTime * 10 - 20;
        final float stopTicks = trainAccessor.getStopCounter() - 20;

        if (!head1IsFront) {
            float t = doorLeftValue;
            doorLeftValue = doorRightValue;
            doorRightValue = t;
        }

        if (doorLeftValue > 0 || doorRightValue > 0) {
            if (stopTicks > dwellTicks - 12 * 20) {
                if (doorLeftValue > 0) {
                    scheduleHelper.play(6 + 12 - (dwellTicks - stopTicks) / 20, 26);
                } else {
                    scheduleHelper.play(34 + 12 - (dwellTicks - stopTicks) / 20, 54);
                }
            } else if (stopTicks < 6 * 20) {
                if (doorLeftValue > 0) {
                    scheduleHelper.play(stopTicks / 20, 6);
                } else {
                    scheduleHelper.play(28 + stopTicks / 20, 34);
                }
            } else {
                if (doorLeftValue > 0) {
                    scheduleHelper.play(6, 6);
                } else {
                    scheduleHelper.play(34, 34);
                }
            }
        }

        matrices.pushPose();
        matrices.translate(x, y - 1, z);
        matrices.mulPose(Vector3f.YP.rotation((float) Math.PI + yaw));
        matrices.mulPose(Vector3f.XP.rotation(train.transportMode.hasPitch ? pitch : 0));

        final int light = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, posAverage), world.getBrightness(LightLayer.SKY, posAverage));

        updateProp.update(train, carIndex, head1IsFront);
        updateProp.miKeyframeTime = scheduleHelper.currentFrameTime;
        if (!head1IsFront) {
            matrices.mulPose(Vector3f.YP.rotation((float) Math.PI));
        }
        if (carNum % 2 == 0) {
            matrices.mulPose(Vector3f.YP.rotation((float) Math.PI));
        }

        if (train.isCurrentlyManual()) {
            updateProp.miHiddenParts = HIDE_LIST_MIDDLE;
        } else {
            if (carNum == train.trainCars - 1) {
                updateProp.miHiddenParts = HIDE_LIST_TAIL;
            } else if (carNum == 0) {
                updateProp.miHiddenParts = HIDE_LIST_HEAD;
            } else {
                updateProp.miHiddenParts = HIDE_LIST_MIDDLE;
            }
        }

        if (carNum % 2 == 0) {
            RenderUtil.updateAndEnqueueAll(getModel(MODEL_BODY_HEAD), updateProp, matrices.last().pose(), light, vertexConsumers);
            RenderUtil.updateAndEnqueueAll(getModel(MODEL_AUX_HEAD), updateProp, matrices.last().pose(), light, vertexConsumers);
        } else {
            RenderUtil.updateAndEnqueueAll(getModel(MODEL_BODY_TAIL), updateProp, matrices.last().pose(), light, vertexConsumers);
            RenderUtil.updateAndEnqueueAll(getModel(MODEL_AUX_TAIL), updateProp, matrices.last().pose(), light, vertexConsumers);
        }

        if (!(this instanceof RenderTrainDK3Mini)) {
            TrainClientRegistry.TrainProperties trainProperties = TrainClientRegistry.getTrainProperties(train.trainId);
            MODEL_BOGIE.render(matrices, vertexConsumers, light, (int)(trainProperties.bogiePosition * 16.0F));
            MODEL_BOGIE.render(matrices, vertexConsumers, light, -((int)(trainProperties.bogiePosition * 16.0F)));
        }

        scheduleHelper.elapse();

        matrices.popPose();
        matrices.popPose();
    }

    private static final ModelBogie MODEL_BOGIE = new ModelBogie();

    @Override
    public void renderConnection(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch) {

    }

    @Override
    public void renderBarrier(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch) {

    }

}
