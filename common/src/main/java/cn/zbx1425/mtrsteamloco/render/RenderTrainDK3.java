package cn.zbx1425.mtrsteamloco.render;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.mixin.TrainAccessor;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcerext.multipart.MultipartContainer;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.animated.AnimatedLoader;
import cn.zbx1425.sowcerext.multipart.mi.MiLoader;
import cn.zbx1425.sowcerext.multipart.mi.MiScheduleHelper;
import com.mojang.math.Vector3f;
import mtr.data.TrainClient;
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
import java.util.UUID;

public class RenderTrainDK3 extends TrainRendererBase {

    protected static MultipartContainer modelDK3;
    protected static MultipartContainer modelDK3AuxHead;
    protected static MultipartContainer modelDK3AuxTail;

    private final TrainClient train;
    private final MultipartUpdateProp updateProp = new MultipartUpdateProp();
    private final MiScheduleHelper scheduleHelper = new MiScheduleHelper();

    public static void initGLModel(ResourceManager resourceManager) {
        try {
            MainClient.atlasManager.load(resourceManager, new ResourceLocation("mtrsteamloco:models/atlas/dk3.json"));
            modelDK3 = AnimatedLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/dk3/c.animated"));
            modelDK3AuxHead = MiLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/alex/dk3auxhead.json"));
            modelDK3AuxTail = MiLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/alex/dk3auxtail.json"));
        } catch (IOException e) {
            Main.LOGGER.error(e);
            modelDK3 = null;
            modelDK3AuxTail = null;
        }
    }

    public RenderTrainDK3(TrainClient train) {
        this.train = train;
    }

    @Override
    public TrainRendererBase createTrainInstance(TrainClient trainClient) {
        return new RenderTrainDK3(trainClient);
    }

    @Override
    public void renderCar(int carIndex, double x, double y, double z, float yaw, float pitch, boolean isTranslucentBatch, float doorLeftValue, float doorRightValue, boolean opening, boolean head1IsFront) {
        if (isTranslucentBatch) {
            return;
        }

        final BlockPos posAverage = getPosAverage(train, x, y, z);
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

        updateProp.update(train, carIndex);
        updateProp.miKeyframeTime = scheduleHelper.currentFrameTime;
        int carNum = head1IsFront ? carIndex : (train.trainCars - carIndex - 1);
        if (!head1IsFront) {
            matrices.mulPose(Vector3f.YP.rotation((float) Math.PI));
        }
        if (carNum % 2 == 0) {
            matrices.mulPose(Vector3f.YP.rotation((float) Math.PI));
        }

        modelDK3.updateAndEnqueueAll(updateProp, MainClient.batchManager, matrices.last().pose(), light, ShaderProp.DEFAULT);

        if (carNum != train.trainCars - 1) {
            modelDK3AuxHead.updateAndEnqueueAll(updateProp, MainClient.batchManager, matrices.last().pose(), light, ShaderProp.DEFAULT);
        } else {
            modelDK3AuxTail.updateAndEnqueueAll(updateProp, MainClient.batchManager, matrices.last().pose(), light, ShaderProp.DEFAULT);
        }

        scheduleHelper.elapse();

        matrices.popPose();
        matrices.popPose();
    }

    @Override
    public void renderConnection(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch) {

    }

    @Override
    public void renderBarrier(Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4, Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, double x, double y, double z, float yaw, float pitch) {

    }

    @Override
    public void renderRidingPlayer(UUID playerId, Vec3 playerPositionOffset) {
        final BlockPos posAverage = getPosAverage(train, playerPositionOffset.x, playerPositionOffset.y, playerPositionOffset.z);
        if (posAverage == null) {
            return;
        }
        matrices.translate(0, RenderTrains.PLAYER_RENDER_OFFSET, 0);
        final Player renderPlayer = world.getPlayerByUUID(playerId);
        if (renderPlayer != null && (!playerId.equals(player.getUUID()) || camera.isDetached())) {
            entityRenderDispatcher.render(renderPlayer, playerPositionOffset.x, playerPositionOffset.y, playerPositionOffset.z, 0, 1, matrices, vertexConsumers, 0xF000F0);
        }
        matrices.popPose();
    }
}
