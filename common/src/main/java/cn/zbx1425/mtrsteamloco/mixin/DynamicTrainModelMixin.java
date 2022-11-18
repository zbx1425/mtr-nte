package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.mtrsteamloco.render.integration.SowcerModelAgent;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.loader.ObjModelLoader;
import cn.zbx1425.sowcerext.reuse.ModelManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.client.DoorAnimationType;
import mtr.client.DynamicTrainModel;
import mtr.client.IResourcePackCreatorProperties;
import mtr.data.EnumHelper;
import mtr.mappings.ModelMapper;
import mtr.model.ModelTrainBase;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Mixin(DynamicTrainModel.class)
public class DynamicTrainModelMixin {

    @Shadow(remap = false) @Final
    private Map<String, ModelMapper> parts;

    private static Map<String, RawModel> cachedModels;
    private static String cachedPath;
    private static long cachedPathMtime = 0;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void ctor(JsonObject model, JsonObject properties, DoorAnimationType doorAnimationType, CallbackInfo ci) {
        int bbDataType = MtrModelRegistryUtil.getDummyBbDataType(model);
        if (bbDataType != 0) {
            parts.clear();
            try {
                if (properties.has("atlasIndex")) {
                    MainClient.atlasManager.load(
                        MtrModelRegistryUtil.resourceManager,
                        new ResourceLocation(properties.get("atlasIndex").getAsString())
                    );
                }

                Map<String, RawModel> models;
                if (bbDataType == 1) {
                    models = ObjModelLoader.loadModels(
                            MtrModelRegistryUtil.resourceManager,
                            MtrModelRegistryUtil.getRlFromDummyBbData(model),
                            MainClient.atlasManager
                    );
                } else {
                    String path = MtrModelRegistryUtil.getPathFromDummyBbData(model);
                    if (cachedModels == null
                        || !path.equals(cachedPath) || new File(path).lastModified() != cachedPathMtime) {
                        MainClient.modelManager.clearNamespace("mtrsteamloco-external");
                        cachedModels = ObjModelLoader.loadExternalModels(
                                MtrModelRegistryUtil.getPathFromDummyBbData(model),
                                MainClient.atlasManager
                        );
                        cachedPath = path;
                        cachedPathMtime = new File(path).lastModified();
                    }
                    models = cachedModels;
                }

                JsonArray propertyParts = properties.getAsJsonArray(IResourcePackCreatorProperties.KEY_PROPERTIES_PARTS);
                propertyParts.forEach(jsonElement -> {
                    final JsonObject jsonObject = jsonElement.getAsJsonObject();
                    final String name = jsonObject.get("name").getAsString();
                    RawModel partModel = models.getOrDefault(name, null);

                    if (partModel != null) {
                        final ModelTrainBase.RenderStage renderStage = EnumHelper.valueOf(ModelTrainBase.RenderStage.EXTERIOR,
                                jsonObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_STAGE).getAsString());
                        switch (renderStage) {
                            case EXTERIOR:
                                partModel.setAllRenderType("exterior");
                            break;
                            case INTERIOR:
                                partModel.setAllRenderType("interior");
                            break;
                            case INTERIOR_TRANSLUCENT:
                                partModel.setAllRenderType("interiortranslucent");
                            break;
                            case LIGHTS:
                            case ALWAYS_ON_LIGHTS:
                                partModel.setAllRenderType("light");
                            break;
                        }
                    }
                });

                for (Map.Entry<String, RawModel> entry : models.entrySet()) {
                    parts.put(entry.getKey(), new SowcerModelAgent(entry.getValue()));
                }
            } catch (Exception e) {
                Main.LOGGER.error(e);
            }
        }
    }
}
