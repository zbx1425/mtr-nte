package cn.zbx1425.mtrsteamloco.render.train;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcerext.multipart.MultipartContainer;
import cn.zbx1425.sowcerext.multipart.animated.AnimatedLoader;
import cn.zbx1425.sowcerext.multipart.mi.MiLoader;
import mtr.data.TrainClient;
import mtr.render.TrainRendererBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class RenderTrainDK3Mini extends RenderTrainDK3 {

    private static final MultipartContainer[] models = new MultipartContainer[4];

    @Override
    protected MultipartContainer getModel(int index) {
        return models[index];
    }

    public static void initGLModel(ResourceManager resourceManager) {
        try {
            MainClient.atlasManager.load(resourceManager, new ResourceLocation("mtrsteamloco:models/atlas/dk3.json"));
            models[MODEL_BODY_HEAD] = AnimatedLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/dk3/chmini.animated"));
            models[MODEL_BODY_TAIL] = AnimatedLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/dk3/cmini.animated"));
            models[MODEL_AUX_HEAD] = MiLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/alex/dk3auxheadmini.json"));
            models[MODEL_AUX_TAIL] = MiLoader.loadModel(resourceManager, MainClient.modelManager, MainClient.atlasManager,
                    new ResourceLocation("mtrsteamloco:models/alex/dk3auxtailmini.json"));
        } catch (IOException e) {
            Main.LOGGER.error("Failed loading model for DK3 Mini:", e);
        }
    }

    public RenderTrainDK3Mini(TrainClient train) {
        super(train);
    }

    @Override
    public TrainRendererBase createTrainInstance(TrainClient trainClient) {
        return new RenderTrainDK3Mini(trainClient);
    }

}
