package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.data.EyeCandyProperties;
import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.render.integration.TrainModelCapture;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.loader.NmbModelLoader;
import cn.zbx1425.sowcerext.model.loader.ObjModelLoader;
import mtr.data.TransportMode;
import mtr.mappings.Text;
import mtr.mappings.Utilities;
import mtr.mappings.UtilitiesClient;
import mtr.model.ModelSimpleTrainBase;
import mtr.render.JonModelTrainRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.io.FilenameUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Debug {

    public static void saveAllBuiltinModels(Path outputDir) {
        mtr.client.TrainClientRegistry.forEach(TransportMode.TRAIN, (trainId, trainProperties) -> {
            if (trainProperties.renderer instanceof JonModelTrainRenderer renderer
                && renderer.model != null && renderer.textureId != null && renderer.model instanceof ModelSimpleTrainBase<?>) {
                try {
                    String textureName = FilenameUtils.getBaseName(renderer.textureId);
                    TrainModelCapture.CaptureResult result = TrainModelCapture.captureModels(
                            renderer.model, new ResourceLocation(renderer.textureId + ".png"));
                    result.getNamedModels().values().forEach(RawModel::distinct);
                    ObjModelLoader.saveModels(result.getNamedModels(),
                            outputDir.resolve(trainId + ".obj"),
                            outputDir.resolve(textureName + ".mtl"), false);

                    if (!Files.exists(outputDir.resolve(textureName + ".png"))) {
                        final List<Resource> resources = UtilitiesClient.getResources(Minecraft.getInstance().getResourceManager(),
                                new ResourceLocation(renderer.textureId + ".png"));
                        if (!resources.isEmpty()) {
                            try {
                                try (InputStream is = Utilities.getInputStream(resources.get(0))) {
                                    Files.copy(is, outputDir.resolve(textureName + ".png"));
                                }
                            } catch (IOException ex) {
                                Main.LOGGER.warn("Failed to save texture for " + trainId + ": ", ex);
                            }
                        }
                    }
                } catch (IOException ex) {
                    Main.LOGGER.warn("Failed to save model for " + trainId, ex);
                }
            }
        });
    }

    public static void saveAllLoadedModels(Path outputDir) {
        for (Map.Entry<ResourceLocation, RawModel> pair : MainClient.modelManager.loadedRawModels.entrySet()) {
            Path path = Paths.get(outputDir.toString(), pair.getKey().getNamespace(), pair.getKey().getPath());
            try {
                Files.createDirectories(path.getParent());
                FileOutputStream fos = new FileOutputStream(FilenameUtils.removeExtension(path.toString()) + ".nmb");
                NmbModelLoader.serializeModel(pair.getValue(), fos, false);
                fos.close();
            } catch (IOException e) {
                Main.LOGGER.error("Failed exporting models:", e);
            }
        }
    }

    public static void registerAllModelsAsEyeCandy() {
        for (Map.Entry<ResourceLocation, ModelCluster> entry : MainClient.modelManager.uploadedVertArrays.entrySet()) {
            String key = FilenameUtils.getBaseName(entry.getKey().getPath());
            EyeCandyRegistry.register(key, new EyeCandyProperties(Text.literal(key), entry.getValue(), null));
        }
    }
}
