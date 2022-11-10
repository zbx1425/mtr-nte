package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.render.RenderTrainD51;
import cn.zbx1425.mtrsteamloco.render.RenderTrainDK3;
import cn.zbx1425.mtrsteamloco.render.RenderTrainDK3Mini;
import cn.zbx1425.mtrsteamloco.sound.BveTrainSoundFix;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.util.GLStateCapture;
import mtr.client.TrainClientRegistry;
import mtr.data.TransportMode;
import mtr.sound.bve.BveTrainSound;
import mtr.sound.bve.BveTrainSoundConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL33;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomResources {

    public static void init(ResourceManager resourceManager) {
        try {
            MainClient.shaderManager.reloadShaders(resourceManager);
        } catch (IOException e) {
            Main.LOGGER.error("Failed to load shader:", e);
        }
        MainClient.modelManager.clear();
        MainClient.atlasManager.clear();

        GLStateCapture stateCapture = new GLStateCapture();
        stateCapture.capture();

        RenderTrainD51.initGLModel(resourceManager);
        RenderTrainDK3.initGLModel(resourceManager);
        RenderTrainDK3Mini.initGLModel(resourceManager);

        stateCapture.restore();
        Main.LOGGER.info("Models: " + MainClient.modelManager.loadedRawModels.size() + " models loaded, "
                + MainClient.modelManager.uploadedVertArraysCount + " VAOs uploaded.");

        mtr.client.TrainClientRegistry.register(
                "dk3", "train_19_2", "train.mtrsteamloco.dk3", 0x7090FF,
                0.0F, 6F, false, false,
                new RenderTrainDK3(null),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "mtrsteamloco:dk3"))
        );
        mtr.client.TrainClientRegistry.register(
                "dk3_mini", "train_9_2", "train.mtrsteamloco.dk3_mini", 0x7090FF,
                0.0F, 2F, false, false,
                new RenderTrainDK3Mini(null),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "mtrsteamloco:dk3"))
        );

        HashMap<String, TrainClientRegistry.TrainProperties> existingTrains19m = new HashMap<>();
        mtr.client.TrainClientRegistry.forEach(TransportMode.TRAIN, (key, prop) -> {
            if (prop.baseTrainType.equals("train_19_2")) {
                existingTrains19m.put(key, prop);
            }
        });

        mtr.client.TrainClientRegistry.register(
                "d51", "train_19_2", "train.mtrsteamloco.d51", 0x808080,
                0.0F, 6F, false, false,
                new RenderTrainD51(null),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "mtrsteamloco:d51"))
        );
        existingTrains19m.forEach((key, prop) -> TrainClientRegistry.register(
                "d51_" + key, "train_19_2", "D51 + " + prop.name.getString(), prop.color,
                0.0F, prop.bogiePosition, false, false,
                new RenderTrainD51(prop.renderer),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "mtrsteamloco:d51"))
        ));
    }
}
