package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.render.RenderTrainD51;
import cn.zbx1425.mtrsteamloco.render.RenderTrainDK3;
import cn.zbx1425.mtrsteamloco.sound.BveTrainSoundFix;
import mtr.sound.bve.BveTrainSound;
import mtr.sound.bve.BveTrainSoundConfig;
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

        int vaoPrev = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING);
        RenderTrainD51.initGLModel(resourceManager);
        RenderTrainDK3.initGLModel(resourceManager);
        GL33.glBindVertexArray(vaoPrev);
        Main.LOGGER.info("Models: " + MainClient.modelManager.loadedRawModels.size() + " models loaded, "
                + MainClient.modelManager.uploadedVertArraysCount + " VAOs uploaded.");

        mtr.client.TrainClientRegistry.register(
                "d51", "train_19_2", "D51+DK3", 0xFF0000,
                0.0F, 0F, false, false,
                new RenderTrainD51(null),
                new BveTrainSound(new BveTrainSoundConfig(resourceManager, "d51"))
        );
        mtr.client.TrainClientRegistry.register(
                "dk3", "train_19_2", "DK3", 0xFF0000,
                0.0F, 6F, false, false,
                new RenderTrainDK3(null),
                new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, "dk3"))
        );
    }
}
