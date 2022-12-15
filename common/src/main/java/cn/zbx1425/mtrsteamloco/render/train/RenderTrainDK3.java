package cn.zbx1425.mtrsteamloco.render.train;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.PoseStackUtil;
import cn.zbx1425.sowcerext.multipart.MultipartContainer;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.animated.AnimatedLoader;
import cn.zbx1425.sowcerext.multipart.mi.MiLoader;
import cn.zbx1425.sowcerext.multipart.mi.MiScheduleHelper;
import com.google.common.collect.ImmutableSet;
import cn.zbx1425.sowcer.math.Vector3f;
import mtr.MTRClient;
import mtr.client.TrainClientRegistry;
import mtr.client.TrainProperties;
import mtr.data.TrainClient;
import mtr.model.ModelBogie;
import mtr.render.RenderTrains;
import mtr.render.TrainRendererBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.util.List;

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
            Main.LOGGER.error("Failed loading model for DK3 regular:", e);
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

    private float elapsedDwellTicks = 0;
    private float totalDwellTicks = 0;
    private float lastRenderedTick = 0;

    @Override
    public void renderCar(int carIndex, double x, double y, double z, float yaw, float pitch, boolean doorLeftOpen, boolean doorRightOpen) {
        if (RenderUtil.shouldSkipRenderTrain(train)) return;

        int carNum = !train.isReversed() ? carIndex : (train.trainCars - carIndex - 1);
        boolean isTail = (carNum % 2 != 0) || (carNum == train.trainCars - 1);
        if (train.spacing == 20 && carIndex == 2) isTail = true; // Pulled by D51, car 3

        // if (carNum > 1 && !(this instanceof RenderTrainDK3Mini)) return;

        if (isTranslucentBatch) {
            return;
        }

        final BlockPos posAverage = applyAverageTransform(train.getViewOffset(), x, y, z);
        if (posAverage == null) {
            return;
        }

        final float lastFrameDuration = MTRClient.getLastFrameDuration();
        final float ticksElapsed = Minecraft.getInstance().isPaused() || lastRenderedTick == MTRClient.getGameTick() ? 0 : lastFrameDuration;
        lastRenderedTick = MTRClient.getGameTick();
        elapsedDwellTicks += ticksElapsed;
        if (train.justOpening()) {
            elapsedDwellTicks = 0;
            totalDwellTicks = train.getTotalDwellTicks();
        }

        // Get door delay of the first sec off
        final float dwellTicks = totalDwellTicks - 20;
        final float stopTicks = elapsedDwellTicks;

        if (train.getDoorValue() == 0) {
            doorLeftOpen = false;
            doorRightOpen = false;
        }

        if (train.isReversed()) {
            boolean t = doorLeftOpen;
            doorLeftOpen = doorRightOpen;
            doorRightOpen = t;
        }

        if (doorLeftOpen || doorRightOpen) {
            if (stopTicks > dwellTicks - 12 * 20) {
                if (doorLeftOpen) {
                    scheduleHelper.play(6 + 12 - (dwellTicks - stopTicks) / 20, 26);
                } else {
                    scheduleHelper.play(34 + 12 - (dwellTicks - stopTicks) / 20, 54);
                }
            } else if (stopTicks < 6 * 20) {
                if (doorLeftOpen) {
                    scheduleHelper.play(stopTicks / 20, 6);
                } else {
                    scheduleHelper.play(28 + stopTicks / 20, 34);
                }
            } else {
                if (doorLeftOpen) {
                    scheduleHelper.play(6, 6);
                } else {
                    scheduleHelper.play(34, 34);
                }
            }
        } else if (stopTicks > dwellTicks + 8 * 20) {
            scheduleHelper.play(0, 0);
        }

        matrices.pushPose();
        matrices.translate(x, y - 1, z);
        PoseStackUtil.rotY(matrices, (float) Math.PI + yaw);
        final boolean hasPitch = pitch < 0 ? train.transportMode.hasPitchAscending : train.transportMode.hasPitchDescending;
        PoseStackUtil.rotX(matrices, hasPitch ? pitch : 0);

        final int light = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, posAverage), world.getBrightness(LightLayer.SKY, posAverage));

        updateProp.update(train, carIndex, !train.isReversed());
        updateProp.miKeyframeTime = scheduleHelper.currentFrameTime;
        if (train.isReversed()) {
            PoseStackUtil.rotY(matrices, (float) Math.PI);
        }
        if (!isTail) {
            PoseStackUtil.rotY(matrices, (float) Math.PI);
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

        if (!(this instanceof RenderTrainDK3Mini)) {
            matrices.translate(0, 0, 1);
            if (this.train.spacing == 20) {
                // Pulled by D51
                if (carIndex == 2) {
                    matrices.translate(0, 0, -1.5);
                } else {
                    matrices.translate(0, 0, -0.5);
                }
            }
        }

        Matrix4f pose = new Matrix4f(matrices.last().pose());
        if (!isTail) {
            getModel(MODEL_BODY_HEAD).updateAndEnqueueAll(MainClient.drawScheduler, updateProp, pose, light);
            getModel(MODEL_AUX_HEAD).updateAndEnqueueAll(MainClient.drawScheduler, updateProp, pose, light);
        } else {
            getModel(MODEL_BODY_TAIL).updateAndEnqueueAll(MainClient.drawScheduler, updateProp, pose, light);
            getModel(MODEL_AUX_TAIL).updateAndEnqueueAll(MainClient.drawScheduler, updateProp, pose, light);
        }

        if (!(this instanceof RenderTrainDK3Mini)) {
            TrainProperties trainProperties = TrainClientRegistry.getTrainProperties(train.trainId);
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
