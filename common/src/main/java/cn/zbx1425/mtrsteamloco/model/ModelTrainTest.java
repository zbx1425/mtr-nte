package cn.zbx1425.mtrsteamloco.model;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcerext.multipart.MultipartContainer;
import cn.zbx1425.sowcerext.multipart.MultipartUpdateProp;
import cn.zbx1425.sowcerext.multipart.animated.AnimatedLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import mtr.model.ModelTrainBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoField;

public class ModelTrainTest extends ModelTrainBase {

    public static MultipartContainer modelTest;

    public static void initGlModel(ResourceManager resourceManager) {
        try {
            modelTest = AnimatedLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/test.animated"));
        } catch (IOException e) {
            Main.LOGGER.error(e);
        }
    }

    @Override
    protected void render(PoseStack matrices, VertexConsumer vertices, ModelTrainBase.RenderStage renderStage, int light, float doorLeftX, float doorRightX, float doorLeftZ, float doorRightZ, int currentCar, int trainCars, boolean head1IsFront, boolean renderDetails) {
        if (modelTest == null) return;
        if (renderStage == ModelTrainBase.RenderStage.EXTERIOR) {
            final Matrix4f lastPose = matrices.last().pose().copy();
            lastPose.multiply(Vector3f.XP.rotation((float) Math.PI));
            if (!head1IsFront) lastPose.multiply(Vector3f.YP.rotation((float) Math.PI));
            lastPose.multiplyWithTranslation(0, -1, 0);

            MultipartUpdateProp updateProp = new MultipartUpdateProp();
            updateProp.systemTimeSecMidnight = LocalTime.now().get(ChronoField.SECOND_OF_DAY);
            updateProp.speed = 10;
            modelTest.update(updateProp);
            modelTest.enqueueAll(MainClient.batchManager, lastPose, light, ShaderProp.DEFAULT);
        }
    }

    @Override
    protected float getDoorAnimationX(float v, boolean b) {
        return 0;
    }

    @Override
    protected float getDoorAnimationZ(float v, boolean b) {
        return 0;
    }
}
