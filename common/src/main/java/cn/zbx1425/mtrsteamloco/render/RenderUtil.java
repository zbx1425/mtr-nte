package cn.zbx1425.mtrsteamloco.render;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.MainClient;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.MTRClient;
import mtr.data.TrainClient;
import mtr.mappings.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RenderUtil {

    public static PoseStack commonPoseStack = null;
    public static MultiBufferSource commonVertexConsumers = null;

    public static double runningSeconds;
    private static float lastRenderedTick = 0;

    public static void updateElapsedTicks() {
        final float lastFrameDuration = MTRClient.getLastFrameDuration();
        final float ticksElapsed = Minecraft.getInstance().isPaused() || lastRenderedTick == MTRClient.getGameTick() ? 0 : lastFrameDuration;
        lastRenderedTick = MTRClient.getGameTick();
        runningSeconds += (double)ticksElapsed / 20.0;
    }

    public static boolean shouldSkipRenderTrain(TrainClient train) {
        if (!ClientConfig.enableTrainRender) return true;
        if (ClientConfig.hideRidingTrain) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                return train.isPlayerRiding(player);
            }
        }
        return false;
    }

    public static String getRenderStatusMessage() {
        return "\n=== NTE Rendering Status ===\n"
                + "Draw Calls: " + MainClient.profiler.drawCallCount
                + ", Batches: " + MainClient.profiler.batchCount
                + "\n"
                + "Faces: " + MainClient.profiler.singleFaceCount + " non-instanced"
                + ", " + MainClient.profiler.instancedFaceCount + " instanced"
                + ", " + (MainClient.profiler.singleFaceCount + MainClient.profiler.instancedFaceCount) + " total"
                + "\n"
                + "Faces via Blaze3D: " + MainClient.profiler.blazeFaceCount
                + "\n"
                + "Loaded Models: " + MainClient.modelManager.loadedRawModels.size()
                + ", Uploaded VAOs: " + MainClient.modelManager.uploadedVertArraysCount
                ;
    }

    public static void displayStatusMessage(String msg) {
#if DEBUG
        Minecraft.getInstance().player.displayClientMessage(Text.literal(
            String.format("[%s] %s", LocalTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_TIME), msg)
        ), false);
#endif
    }
}
