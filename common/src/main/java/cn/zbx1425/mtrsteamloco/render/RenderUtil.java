package cn.zbx1425.mtrsteamloco.render;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.MainClient;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.MTRClient;
import mtr.data.TrainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

public class RenderUtil {

    public static PoseStack commonPoseStack = null;
    public static MultiBufferSource commonVertexConsumers = null;

    public static double runningSeconds;
    public static double frameSeconds;
    private static float lastRenderedTick = 0;

    public static void updateElapsedTicks() {
        final float lastFrameDuration = MTRClient.getLastFrameDuration();
        final float ticksElapsed = Minecraft.getInstance().isPaused() || lastRenderedTick == MTRClient.getGameTick() ? 0 : lastFrameDuration;
        lastRenderedTick = MTRClient.getGameTick();
        frameSeconds = (double)ticksElapsed / 20.0;
        runningSeconds += frameSeconds;
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
                + "Draw Calls: " + MainClient.drawContext.drawCallCount
                + ", Batches: " + MainClient.drawContext.batchCount
                + "\n"
                + "Faces: " + MainClient.drawContext.singleFaceCount + " non-instanced"
                + ", " + MainClient.drawContext.instancedFaceCount + " instanced"
                + ", " + (MainClient.drawContext.singleFaceCount + MainClient.drawContext.instancedFaceCount) + " total"
                + "\n"
                + "Faces via Blaze3D: " + MainClient.drawContext.blazeFaceCount
                + "\n"
                + "Uploaded Models: " + MainClient.modelManager.uploadedVertArrays.size()
                + " (" + MainClient.modelManager.vaoCount + " VAOs, "
                + MainClient.modelManager.vboCount + " VBOs)"
                + "\n"
                + String.join("\n", MainClient.drawContext.debugInfo)
                ;
    }

    public static int parseHexColor(String src) {
        if (src.length() > 6) {
            return Integer.reverseBytes(Integer.parseInt(src, 16));
        } else {
            return Integer.reverseBytes((Integer.parseInt(src, 16) << 8 | 0xFF));
        }
    }

    public static void displayStatusMessage(String msg) {
#if DEBUG
        Minecraft.getInstance().player.displayClientMessage(Text.literal(
            String.format("[%s] %s", LocalTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_TIME), msg)
        ), false);
#endif
    }
}
