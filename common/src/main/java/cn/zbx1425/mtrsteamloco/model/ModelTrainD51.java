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
import mtr.model.ModelSTrainSmall;
import mtr.model.ModelTrainBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoField;

public class ModelTrainD51 extends ModelTrainBase {

    public static MultipartContainer modelD51;
    public static MultipartContainer modelOha35A;
    public static MultipartContainer modelOha35B;
    public static MultipartContainer modelOha35C;

    public static void initGlModel(ResourceManager resourceManager) {
        try {
            MainClient.atlasManager.load(resourceManager, new ResourceLocation("mtrsteamloco:models/atlas/d51.json"));
            MainClient.atlasManager.load(resourceManager, new ResourceLocation("mtrsteamloco:models/atlas/oha35.json"));
            modelD51 = AnimatedLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/d51/d51.animated"));
            modelOha35A = AnimatedLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/oha35/oha35a.animated"));
            modelOha35B = AnimatedLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/oha35/oha35b.animated"));
            modelOha35C = AnimatedLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/oha35/oha35c.animated"));
        } catch (IOException e) {
            Main.LOGGER.error(e);
        }
    }

    @Override
    protected void render(PoseStack matrices, VertexConsumer vertices, RenderStage renderStage, int light, float doorLeftX, float doorRightX, float doorLeftZ, float doorRightZ, int currentCar, int trainCars, boolean head1IsFront, boolean renderDetails) {
        if (modelD51 == null || modelOha35A == null || modelOha35B == null || modelOha35C == null) return;
        if (renderStage == RenderStage.EXTERIOR) {
            final Matrix4f lastPose = matrices.last().pose().copy();
            lastPose.multiply(Vector3f.XP.rotation((float) Math.PI));
            if (!head1IsFront) lastPose.multiply(Vector3f.YP.rotation((float) Math.PI));
            lastPose.multiplyWithTranslation(0, -1, 0);

            MultipartUpdateProp updateProp = new MultipartUpdateProp();
            updateProp.systemTimeSecMidnight = LocalTime.now().get(ChronoField.SECOND_OF_DAY);
            updateProp.speed = 10;
            if (currentCar == 0) {
                modelD51.update(updateProp);
                modelD51.enqueueAll(MainClient.batchManager, lastPose, light, ShaderProp.DEFAULT);
            } else if (currentCar == trainCars - 1) {
                modelOha35C.update(updateProp);
                modelOha35C.enqueueAll(MainClient.batchManager, lastPose, light, ShaderProp.DEFAULT);
            } else if (currentCar == 1) {
                modelOha35A.update(updateProp);
                modelOha35A.enqueueAll(MainClient.batchManager, lastPose, light, ShaderProp.DEFAULT);
            } else {
                modelOha35B.update(updateProp);
                modelOha35B.enqueueAll(MainClient.batchManager, lastPose, light, ShaderProp.DEFAULT);
            }
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
