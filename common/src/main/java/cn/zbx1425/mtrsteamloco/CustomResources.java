package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.render.RenderTrainD51;
import cn.zbx1425.mtrsteamloco.render.RenderTrainDK3;
import cn.zbx1425.mtrsteamloco.render.RenderTrainDK3Mini;
import cn.zbx1425.mtrsteamloco.sound.BveTrainSoundFix;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.util.GLStateCapture;
import mtr.sound.bve.BveTrainSound;
import mtr.sound.bve.BveTrainSoundConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL33;

import java.io.IOException;

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

        try {
            Model railModel = MainClient.modelManager.uploadModel(MainClient.modelManager.loadRawModel(
                    resourceManager, new ResourceLocation("mtrsteamloco:models/rail.csv"), MainClient.atlasManager));
            MainClient.railRenderDispatcher.setModel(railModel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mtr.client.TrainClientRegistry.register(
                "d51", "train_19_2", "D51 + DK3", 0xFF0000,
                0.0F, 6F, false, false,
                new RenderTrainD51(null),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "mtrsteamloco:d51"))
        );
        mtr.client.TrainClientRegistry.register(
                "dk3", "train_19_2", "DK3", 0xFF0000,
                0.0F, 6F, false, false,
                new RenderTrainDK3(null),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "mtrsteamloco:dk3"))
        );
        mtr.client.TrainClientRegistry.register(
                "dk3mini", "train_9_2", "DK3 (Mini)", 0xFF0000,
                0.0F, 2F, false, false,
                new RenderTrainDK3Mini(null),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "mtrsteamloco:dk3"))
        );
    }
}
