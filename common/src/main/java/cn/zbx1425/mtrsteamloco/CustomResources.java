package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.data.RailModelRegistry;
import cn.zbx1425.mtrsteamloco.render.train.RenderTrainD51;
import cn.zbx1425.mtrsteamloco.render.train.RenderTrainDK3;
import cn.zbx1425.mtrsteamloco.render.train.RenderTrainDK3Mini;
import cn.zbx1425.mtrsteamloco.sound.BveTrainSoundFix;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.util.GlStateTracker;
import cn.zbx1425.sowcerext.model.RawModel;
import mtr.client.TrainClientRegistry;
import mtr.client.TrainProperties;
import mtr.data.TransportMode;
import mtr.mappings.Text;
import mtr.sound.bve.BveTrainSoundConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;

public class CustomResources {

    public static void reset(ResourceManager resourceManager) {
        try {
            MainClient.drawScheduler.reloadShaders(resourceManager);
        } catch (IOException e) {
            Main.LOGGER.error("Failed loading shader:", e);
        }
        MainClient.modelManager.clear();
        MainClient.atlasManager.clear();
    }

    public static void init(ResourceManager resourceManager) {
        EyeCandyRegistry.reload(resourceManager);
        RailModelRegistry.reload(resourceManager);

        RenderTrainD51.initGLModel(resourceManager);
        RenderTrainDK3.initGLModel(resourceManager);
        RenderTrainDK3Mini.initGLModel(resourceManager);

        Main.LOGGER.info("MTR-NTE: " + MainClient.modelManager.loadedRawModels.size() + " models loaded, "
                + MainClient.modelManager.uploadedVertArraysCount + " VAOs uploaded.");

        mtr.client.TrainClientRegistry.register("dk3", new TrainProperties(
                "train_20_2", Text.translatable("train.mtrsteamloco.dk3"),
                Text.translatable("train.mtrsteamloco.dk3.description").getString(), "", 0x7090FF,
                0.0F, 0.0F, 6F, false, false,
                new RenderTrainDK3(null),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "mtrsteamloco:dk3"))
        ));
        mtr.client.TrainClientRegistry.register("dk3_mini", new TrainProperties(
                "train_9_2", Text.translatable("train.mtrsteamloco.dk3_mini"),
                Text.translatable("train.mtrsteamloco.dk3.description").getString(), "", 0x7090FF,
                0.0F, 0.0F, 2F, false, false,
                new RenderTrainDK3Mini(null),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "mtrsteamloco:dk3"))
        ));

        HashMap<String, TrainProperties> existingTrains19m = new HashMap<>();
        mtr.client.TrainClientRegistry.forEach(TransportMode.TRAIN, (key, prop) -> {
            if (prop.baseTrainType.equals("train_19_2") || key.equals("dk3")) {
                existingTrains19m.put(key, prop);
            }
        });

        mtr.client.TrainClientRegistry.register("d51", new TrainProperties(
                "train_19_2", Text.translatable("train.mtrsteamloco.d51"),
                Text.translatable("train.mtrsteamloco.d51.description").getString(), "", 0x808080,
                0.0F, 0.0F, 6F, false, false,
                new RenderTrainD51(null),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "mtrsteamloco:d51"))
        ));
        existingTrains19m.forEach((key, prop) -> TrainClientRegistry.register("d51_" + key, new TrainProperties(
                "train_19_2", Text.literal("D51 + " + prop.name.getString()),
                Text.translatable("train.mtrsteamloco.d51.description").getString()
                        + (prop.description != null ? "\n\n" + prop.description : ""), "", prop.color,
                0.0F, 0.0F, prop.bogiePosition, false, false,
                new RenderTrainD51(prop.renderer),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "mtrsteamloco:d51"))
        )));
    }
}
