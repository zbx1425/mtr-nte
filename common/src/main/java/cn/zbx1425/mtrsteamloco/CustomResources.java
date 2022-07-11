package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.model.ModelTrainD51;
import cn.zbx1425.mtrsteamloco.model.ModelTrainTest;
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

        mtr.client.TrainClientRegistry.register(
                "d51", "train_19_2", new ModelTrainD51(), "mtr:s_train", "D51", 0xFF0000,
                "", "", 0.0F, 7.5F, false, "d51", null
        );

        int vaoPrev = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING);
        ModelTrainD51.initGlModel(resourceManager);
        GL33.glBindVertexArray(vaoPrev);

        Main.LOGGER.info("Models: " + MainClient.modelManager.loadedRawModels.size() + " models loaded, "
                + MainClient.modelManager.uploadedVertArraysCount + " VAOs uploaded.");
    }
}
