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
import mtr.mappings.UtilitiesClient;
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
                && renderer.model != null && renderer.textureId != null) {
                try {
                    String textureName = FilenameUtils.getBaseName(renderer.textureId);
                    RawModel[] modelHead0 = TrainModelCapture.captureModels(
                            renderer.model, new ResourceLocation(renderer.textureId + ".png"),
                            1, 3);
                    ObjModelLoader.saveModels(nameModels(modelHead0),
                            outputDir.resolve(trainId + "_head0.obj"),
                            outputDir.resolve(textureName + ".mtl"), false);
                    RawModel[] modelHead1 = TrainModelCapture.captureModels(
                            renderer.model, new ResourceLocation(renderer.textureId + ".png"),
                            0, 3);
                    ObjModelLoader.saveModels(nameModels(modelHead1),
                            outputDir.resolve(trainId + "_head1.obj"),
                            outputDir.resolve(textureName + ".mtl"), false);
                    RawModel[] modelHead2 = TrainModelCapture.captureModels(
                            renderer.model, new ResourceLocation(renderer.textureId + ".png"),
                            2, 3);
                    ObjModelLoader.saveModels(nameModels(modelHead2),
                            outputDir.resolve(trainId + "_head2.obj"),
                            outputDir.resolve(textureName + ".mtl"), false);
                    RawModel[] modelHead12 = TrainModelCapture.captureModels(
                            renderer.model, new ResourceLocation(renderer.textureId + ".png"),
                            0, 1);
                    ObjModelLoader.saveModels(nameModels(modelHead12),
                            outputDir.resolve(trainId + "_head12.obj"),
                            outputDir.resolve(textureName + ".mtl"), false);

                    if (!Files.exists(outputDir.resolve(textureName + ".png"))) {
                        final List<Resource> resources = UtilitiesClient.getResources(Minecraft.getInstance().getResourceManager(),
                                new ResourceLocation(renderer.textureId + ".png"));
                        if (resources.size() > 0) {
                            try {
                                try (InputStream is = resources.get(0).open()) {
                                    Files.copy(is, outputDir.resolve(textureName + ".png"));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
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
            EyeCandyRegistry.register(key, new EyeCandyProperties(Text.literal(key), entry.getValue()));
        }
    }

    private static Map<String, RawModel> nameModels(RawModel[] capturedModels) {
        return Map.of(
                "body", capturedModels[0],
                "doorXNZN", capturedModels[1], "doorXNZP", capturedModels[2],
                "doorXPZN", capturedModels[3], "doorXPZP", capturedModels[4]
        );
    }
}
